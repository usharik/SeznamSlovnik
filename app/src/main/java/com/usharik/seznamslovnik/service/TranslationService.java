package com.usharik.seznamslovnik.service;

import android.util.Log;
import android.util.Pair;

import com.usharik.seznamslovnik.AppState;
import com.usharik.seznamslovnik.action.Action;
import com.usharik.seznamslovnik.action.ShowToastAction;
import com.usharik.seznamslovnik.dao.DatabaseManager;
import com.usharik.seznamslovnik.dao.TranslationStorageDao;
import com.usharik.seznamslovnik.dao.entity.Word;
import com.usharik.seznamslovnik.model.Suggest;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import retrofit2.Retrofit;

/**
 * Created by macbook on 09.03.2018.
 */

public class TranslationService {

    private static final List<String> EMPTY_STR_LIST = Collections.unmodifiableList(new ArrayList<>());

    private final PublishSubject<Wrapper> storeSubject = PublishSubject.create();
    private final DatabaseManager databaseManager;
    private final AppState appState;
    private final Retrofit retrofit;
    private final PublishSubject<Action> executeActionSubject;

    public TranslationService(final DatabaseManager databaseManager,
                              final AppState appState,
                              final Retrofit retrofit,
                              final PublishSubject<Action> executeActionSubject) {
        this.databaseManager = databaseManager;
        this.appState = appState;
        this.retrofit = retrofit;
        this.executeActionSubject = executeActionSubject;
        storeSubject.observeOn(Schedulers.io())
                .subscribe(wrp -> {
                    try {
                        getDao().insertTranslationsForWord(wrp.word, wrp.langFrom, wrp.translations, wrp.langTo);
                    } catch (Exception ex) {
                        Log.e(getClass().getName(), ex.getLocalizedMessage());
                    }
                });
    }

    private TranslationStorageDao getDao() {
        return databaseManager.getActiveDbInstance().translationStorageDao();
    }

    public Maybe<List<String>> getSuggestions(String template, String langFrom, String langTo, int limit, boolean isOffline) {
        if (isOffline) {
            return getOfflineSuggestions(template, langFrom, 1000);
        } else {
            return getOnlineSuggestions(template, langFrom, langTo, limit);
        }
    }

    private Maybe<List<String>> getOfflineSuggestions(String template, String langFrom, int limit) {
        String trimmed = template.trim();
        return getDao().getSuggestions(StringUtils.stripAccents(trimmed), trimmed, langFrom, limit);
    }

    private Maybe<List<String>> getOnlineSuggestions(String template, String langFrom, String langTo, int limit) {
        return retrofit.create(APIInterface.class).doGetSuggestions(
                langFrom,
                langTo,
                template,
                "json",
                1,
                limit)
                .flatMap(answer -> {
                    List<String> sgList = new ArrayList<>();
                    sgList.add(template);
                    for (Suggest sg : answer.result.get(0).suggest) {
                        sgList.add(sg.value);
                    }
                    return Observable.just(sgList);
                })
                .firstElement();
    }

    public Single<Pair<String, List<String>>> translate(String question, String langFrom, String langTo) {
        if (question == null || question.length() == 0) {
            return Single.just(Pair.create("", EMPTY_STR_LIST));
        }

        return existsActualTranslation(question, langFrom, langTo)
                .subscribeOn(Schedulers.io())
                .flatMap(exists -> {
                    if (exists) {
                        return getDao()
                                .getTranslations(question, langFrom, langTo, 1000)
                                .flatMapSingle(list -> Single.just(Pair.create(question, list)));
                    } else if (!appState.isOfflineMode) {
                        return runOnlineTranslation(question, langFrom, langTo);
                    } else {
                        return Single.just(Pair.create(question, EMPTY_STR_LIST));
                    }
                });
    }

    private Single<Boolean> existsActualTranslation(String question, String langFrom, String langTo) {
        return getDao().getWord(question, langFrom)
                .switchIfEmpty(Single.just(Word.NULL_WORD))
                .flatMap(word -> {
                    if (word == Word.NULL_WORD || (isOldTranslation(word) && !appState.isOfflineMode)) {
                        return Single.just(EMPTY_STR_LIST);
                    } else {
                        return getDao().getTranslations(question, langFrom, langTo, 1)
                                .switchIfEmpty(Single.just(EMPTY_STR_LIST));
                    }
                })
                .flatMap(list -> Single.just(!list.isEmpty()));
    }

    private boolean isOldTranslation(Word word) {
        Date curr = Calendar.getInstance().getTime();
        return TimeUnit.DAYS.convert(curr.getTime() - word.getLoadDate().getTime(), TimeUnit.MILLISECONDS) >= 7;
    }

    private void storeTranslation(String word, String langFrom, List<String> translations, String langTo) {
        storeSubject.onNext(new Wrapper(word, langFrom, translations, langTo));
    }

    private Single<Pair<String, List<String>>> runOnlineTranslation(String question, String langFrom, String langTo) {
        return retrofit.create(APIInterface.class).doTranslate(
                langFrom,
                langTo,
                question)
                .flatMap(response -> {
                    Pair<String, List<String>> translation = parseResponse(question, response.string());
                    if (translation.second.size() > 0) {
                        storeTranslation(translation.first, langFrom, translation.second, langTo);
                    }
                    return Observable.just(translation);
                })
                .firstOrError();
    }

    private Pair<String, List<String>> parseResponse(String word, String responseText) {
        try {
            Document html = Jsoup.parse(responseText);

            Elements elements1 = html.body().select("div.hgroup > h1");
            if (elements1.size() > 0) {
                word = elements1.get(0).text();
            }

            Elements elements = html.body().select("div#fastMeanings");
            List<String> transList = EMPTY_STR_LIST;
            if (elements.size() > 0) {
                transList = extractTranslations(elements.get(0).children());
            }
            if (transList.isEmpty()) {
                elements = html.body().select("span.arrow");
                if (elements.size() > 0) {
                    transList = new ArrayList<>();
                    for (Element el : elements) {
                        Node node = el.nextSibling();
                        if (node == null) {
                            continue;
                        }
                        String text = node.toString();
                        if (text == null) {
                            continue;
                        }
                        if (text.length() > 150) {
                            text = text.substring(0, 150);
                        }
                        transList.add(text.trim());
                    }
                }
            }
            return Pair.create(word, transList);
        } catch (Exception e) {
            Log.e(getClass().getName(), e.getLocalizedMessage());
            executeActionSubject.onNext(new ShowToastAction(e.getLocalizedMessage()));
            return Pair.create(word, EMPTY_STR_LIST);
        }
    }

    private static List<String> extractTranslations(Elements translations) {
        List<String> result = new ArrayList<>();
        StringBuilder word = new StringBuilder();
        for (Element el : translations) {
            if (el.tag().getName().equals("br") || (el.tag().getName().equals("span") && el.hasClass("comma"))) {
                if (word.length() > 0) {
                    if (word.length() > 0) {
                        word.delete(word.length() - 1, word.length());
                    }
                    result.add(word.toString());
                }
                word = new StringBuilder();
                continue;
            }
            word.append(el.text());
            word.append(" ");
        }
        return result;
    }

    private static class Wrapper {
        final String word;
        final String langFrom;
        final List<String> translations;
        final String langTo;

        Wrapper(String word, String langFrom, List<String> translations, String langTo) {
            this.word = word;
            this.langFrom = langFrom;
            this.langTo = langTo;
            this.translations = translations;
        }
    }
}
