package com.usharik.seznamslovnik.service;

import android.util.Log;
import android.util.Pair;

import com.usharik.seznamslovnik.AppState;
import com.usharik.seznamslovnik.action.Action;
import com.usharik.seznamslovnik.action.ShowToastAction;
import com.usharik.seznamslovnik.dao.DatabaseManager;
import com.usharik.seznamslovnik.dao.TranslationStorageDao;
import com.usharik.seznamslovnik.dao.Word;
import com.usharik.seznamslovnik.model.Answer;
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
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static java.net.HttpURLConnection.HTTP_OK;

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
                .subscribe((wrp) -> {
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
        PublishSubject<List<String>> publishSubject = PublishSubject.create();
        APIInterface apiInterface = retrofit.create(APIInterface.class);
        Call<Answer> call = apiInterface.doGetSuggestions(
                langFrom,
                langTo,
                template,
                "json",
                1,
                limit);
        call.enqueue(new Callback<Answer>() {
            @Override
            public void onResponse(Call<Answer> call, Response<Answer> response) {
                if (response.code() != HTTP_OK) {
                    String message = response.raw().request().url().url() + " Http error " + response.code();
                    Log.e(getClass().getName(), message);
                    executeActionSubject.onNext(new ShowToastAction(message));
                    return;
                }
                if (response.body() == null ||
                        response.body().result == null ||
                        response.body().result.size() == 0) {
                    Log.e(getClass().getName(), "Null answer");
                    executeActionSubject.onNext(new ShowToastAction("Null answer"));
                    return;
                }
                List<Suggest> suggest = response.body().result.get(0).suggest;
                List<String> sgList = new ArrayList<>();
                sgList.add(template);
                for (Suggest sg : suggest) {
                    sgList.add(sg.value);
                }
                publishSubject.onNext(sgList);
                publishSubject.onComplete();
            }

            @Override
            public void onFailure(Call<Answer> call, Throwable t) {
                Log.e(getClass().getName(), t.getLocalizedMessage());
                executeActionSubject.onNext(new ShowToastAction(t.getLocalizedMessage()));
                publishSubject.onNext(EMPTY_STR_LIST);
                publishSubject.onComplete();
            }
        });
        return publishSubject.firstElement();
    }

    public Single<Pair<String, List<String>>> translate(String question, String langFrom, String langTo) {
        if (question == null || question.length() == 0) {
            return Single.just(Pair.create("", EMPTY_STR_LIST));
        }

        return existsActualTranslation(question, langFrom, langTo)
                .subscribeOn(Schedulers.io())
                .flatMap((exists) -> {
                    if (exists) {
                        return getDao().getTranslations(question, langFrom, langTo, 1000)
                                .flatMap((list) -> Maybe.just(Pair.create(question, list)))
                                .toSingle();
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
                .flatMap((word) -> {
                    if (word == Word.NULL_WORD || (isOldTranslation(word) && !appState.isOfflineMode)) {
                        return Single.just(EMPTY_STR_LIST);
                    } else {
                        return getDao().getTranslations(question, langFrom, langTo, 1)
                                .switchIfEmpty(Single.just(EMPTY_STR_LIST));
                    }
                })
                .flatMap((list) -> Single.just(!list.isEmpty()));
    }

    private boolean isOldTranslation(Word word) {
        Date curr = Calendar.getInstance().getTime();
        return TimeUnit.DAYS.convert(curr.getTime() - word.getLoadDate().getTime(), TimeUnit.MILLISECONDS) >= 7;
    }

    private void storeTranslation(String word, String langFrom, List<String> translations, String langTo) {
        storeSubject.onNext(new Wrapper(word, langFrom, translations, langTo));
    }

    private Single<Pair<String, List<String>>> runOnlineTranslation(String question, String langFrom, String langTo) {
        PublishSubject<Pair<String, List<String>>> translationPublisher = PublishSubject.create();
        APIInterface apiInterface = retrofit.create(APIInterface.class);

        Call<ResponseBody> call = apiInterface.doTranslate(
                langFrom,
                langTo,
                question);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() != HTTP_OK) {
                    Log.e(getClass().getName(), "Http error " + response.code());
                    executeActionSubject.onNext(new ShowToastAction("Http error " + response.code()));
                    return;
                }
                try {
                    Document html = Jsoup.parse(response.body().string());

                    String word = question;
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
                    if (transList.size() > 0) {
                        storeTranslation(word, langFrom, transList, langTo);
                    }
                    translationPublisher.onNext(Pair.create(word, transList));
                } catch (Exception e) {
                    Log.e(getClass().getName(), e.getLocalizedMessage());
                    executeActionSubject.onNext(new ShowToastAction(e.getLocalizedMessage()));
                    translationPublisher.onNext(Pair.create(question, EMPTY_STR_LIST));
                } finally {
                    translationPublisher.onComplete();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(getClass().getName(), t.getLocalizedMessage());
                executeActionSubject.onNext(new ShowToastAction(t.getLocalizedMessage()));
                translationPublisher.onNext(Pair.create(question, EMPTY_STR_LIST));
                translationPublisher.onComplete();
            }
        });
        return translationPublisher.singleOrError();
    }

    private static List<String> extractTranslations(Elements translations) {
        List<String> result = new ArrayList<>();
        StringBuilder word = new StringBuilder();
        for (Element el : translations) {
            if (el.tag().getName().equals("br") || (el.tag().getName().equals("span") && el.hasClass("comma"))) {
                if (word.length() > 0) {
                    if (word.length() > 0) {
                        word.delete(word.length()-1, word.length());
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
