package com.usharik.seznamslovnik.service;

import com.google.gson.Gson;
import com.usharik.seznamslovnik.AppState;
import com.usharik.seznamslovnik.action.Action;
import com.usharik.seznamslovnik.adapter.TranslationResult;
import com.usharik.seznamslovnik.dao.DatabaseManager;
import com.usharik.seznamslovnik.dao.TranslationStorageDao;
import com.usharik.seznamslovnik.dao.entity.Word;

import com.usharik.seznamslovnik.model.json.slovnik.Answer;
import com.usharik.seznamslovnik.model.json.slovnik.Grp;
import com.usharik.seznamslovnik.model.json.slovnik.Sen;
import com.usharik.seznamslovnik.model.json.slovnik.Translate;
import com.usharik.seznamslovnik.model.json.suggest.Result;
import com.usharik.seznamslovnik.model.json.suggest.Text;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import retrofit2.Retrofit;

/**
 * Created by macbook on 09.03.2018.
 */

public class TranslationService {

    private final DatabaseManager databaseManager;
    private final AppState appState;
    private final Retrofit seznamRetrofit;
    private final Retrofit seznamSuggestRetrofit;
    private final NetworkService networkService;
    private final PublishSubject<Action> executeActionSubject;
    private final Gson gson = new Gson();

    public TranslationService(final DatabaseManager databaseManager,
                              final AppState appState,
                              final Retrofit seznamRetrofit,
                              final Retrofit seznamSuggestRetrofit,
                              final NetworkService networkService,
                              final PublishSubject<Action> executeActionSubject) {
        this.databaseManager = databaseManager;
        this.appState = appState;
        this.seznamRetrofit = seznamRetrofit;
        this.seznamSuggestRetrofit = seznamSuggestRetrofit;
        this.networkService = networkService;
        this.executeActionSubject = executeActionSubject;
    }

    private TranslationStorageDao getDao() {
        return databaseManager.getActiveDbInstance().translationStorageDao();
    }

    public Maybe<List<String>> getSuggestions(String template, String langFrom, String langTo, int limit) {
        if (!networkService.isNetworkConnected() || appState.isOfflineMode) {
            return getOfflineSuggestions(template, langFrom, langTo, 100);
        } else {
            return getOnlineSuggestions(template, langFrom, langTo, limit);
        }
    }

    private Maybe<List<String>> getOfflineSuggestions(String template, String langFrom, String langTo, int limit) {
        String trimmed = template.trim();
        return getDao().getSuggestions(StringUtils.stripAccents(trimmed), trimmed, langFrom, langTo, limit);
    }

    Maybe<List<String>> getOnlineSuggestions(String template, String langFrom, String langTo, int limit) {
        return seznamSuggestRetrofit.create(SeznamSuggestionInterface.class).doGetSuggestions(
                langFrom,
                langTo,
                template,
                "json-2",
                1,
                limit)
                .flatMap(suggest -> {
                    List<String> sgList = new ArrayList<>();
                    sgList.add(template);
                    for (Result res : suggest.getResult()) {
                        StringBuilder sb = new StringBuilder();
                        for (Text text : res.getText()) {
                            sb.append(text.getText());
                        }
                        sgList.add(sb.toString());
                    }
                    return Observable.just(sgList);
                })
                .onErrorResumeNext(getOfflineSuggestions(template, langFrom, langTo, limit).toObservable())
                .firstElement();
    }

    public Maybe<TranslationResult> translate(String question, String langFrom, String langTo) {
        if (question == null || question.length() == 0) {
            return Maybe.empty();
        }

        return getActualOfflineTranslation(question, langFrom, langTo)
                .subscribeOn(Schedulers.io())
                .switchIfEmpty(getOnlineTranslation(question, langFrom, langTo));
    }

    private Maybe<TranslationResult> getActualOfflineTranslation(String question, String langFrom, String langTo) {
        return getDao().getWord(question, langFrom)
                .flatMap(word -> {
                    if (isOldTranslation(word) && !appState.isOfflineMode && networkService.isNetworkConnected()) {
                        return Maybe.empty();
                    } else {
                        return getDao()
                                .getTranslations(question, langFrom, langTo, 1000)
                                .flatMap(list -> {
                                    if (list.size() == 0) {
                                        return Maybe.empty();
                                    }
                                    return Maybe.just(new TranslationResult(
                                            question,
                                            list,
                                            getDao().getWordGender(word.getId())));
                                });
                    }
                });
    }

    private boolean isOldTranslation(Word word) {
        Date curr = Calendar.getInstance().getTime();
        return TimeUnit.DAYS.convert(curr.getTime() - word.getLoadDate().getTime(), TimeUnit.MILLISECONDS) >= 30;
    }

    private long storeTranslation(String word, String langFrom, List<String> translations, String langTo, String json) {
        return getDao().insertTranslationsForWord(word, langFrom, translations, langTo, json);
    }

    Maybe<TranslationResult> getOnlineTranslation(String question, String langFrom, String langTo) {
        if (appState.isOfflineMode) {
            return Maybe.empty();
        }
        return seznamRetrofit.create(SeznamRestInterface.class)
                .doTranslate(langFrom + "_" + langTo, question)
                .flatMap(response -> parseTranslationJson(response, question, langFrom, langTo))
                .firstElement();
    }

    private Observable<TranslationResult> parseTranslationJson(ResponseBody response, String question, String langFrom, String langTo) throws IOException {
        String jsonAnswer = response.string();
        Answer answer = gson.fromJson(jsonAnswer, Answer.class);
        String phrs = question;
        List<String> translations = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (Translate trn : answer.getTranslate()) {
            phrs = trn.getHead().getPhrs();
            for (Grp grp : trn.getGrps()) {
                for (Sen sen : grp.getSens()) {
                    for (List<String> trns : sen.getTrans()) {
                        sb = new StringBuilder();
                        for (String str : trns) {
                            String word = Jsoup.parse(str).text();
                            if (word.equals(",")) {
                                if (sb.length() > 0) {
                                    translations.add(sb.toString().trim());
                                    sb = new StringBuilder();
                                }
                                continue;
                            }
                            if (!word.isEmpty()) {
                                sb.append(word).append(" ");
                            }
                        }
                    }
                    if (sb.length() > 0) {
                        translations.add(sb.toString().trim());
                    }
                }
            }
        }
        TranslationResult translation = new TranslationResult(phrs, translations, jsonAnswer, answer);
        if (translation.getTranslations().size() > 0) {
            long wordId = storeTranslation(translation.getWord(), langFrom, translation.getTranslations(), langTo, jsonAnswer);
            translation.setGender(getDao().getWordGender(wordId));
        }
        return Observable.just(translation);
    }
}
