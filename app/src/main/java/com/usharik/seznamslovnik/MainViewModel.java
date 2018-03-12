package com.usharik.seznamslovnik;

import android.content.ClipboardManager;
import android.content.res.Resources;
import android.databinding.Bindable;
import android.os.Vibrator;
import android.util.Log;

import com.usharik.seznamslovnik.adapter.MyAdapter;
import com.usharik.seznamslovnik.service.TranslationService;
import com.usharik.seznamslovnik.framework.ViewModelObservable;
import com.usharik.seznamslovnik.model.Answer;
import com.usharik.seznamslovnik.model.Suggest;
import com.usharik.seznamslovnik.service.APIInterface;
import com.usharik.seznamslovnik.service.NetworkService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.usharik.seznamslovnik.MainActivity.LANG_ORDER_STR;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * MainViewModel
 */

public class MainViewModel extends ViewModelObservable {

    private final AppState appState;
    private final Retrofit retrofit;
    private final TranslationService translationService;
    private final NetworkService networkService;
    private final PublishSubject<String> toastShowSubject;
    private final Resources resources;
    private final ClipboardManager clipboardManager;
    private final Vibrator vibrator;

    private String text;
    private String word;
    private MyAdapter adapter;
    private int fromLanguageIx = 0;
    private int toLanguageIx = 1;

    private PublishSubject<MyAdapter> answerPublishSubject = PublishSubject.create();

    @Inject
    public MainViewModel(final AppState appState,
                         final Retrofit retrofit,
                         final TranslationService translationService,
                         final NetworkService networkService,
                         final PublishSubject<String> toastShowSubject,
                         final Resources resources,
                         final ClipboardManager clipboardManager,
                         final Vibrator vibrator) {
        this.appState = appState;
        this.retrofit = retrofit;
        this.translationService = translationService;
        this.networkService = networkService;
        this.toastShowSubject = toastShowSubject;
        this.resources = resources;
        this.clipboardManager = clipboardManager;
        this.vibrator = vibrator;
        this.adapter = getEmptyAdapter();
    }

    @Bindable
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        notifyPropertyChanged(BR.text);
    }

    @Bindable
    public String getWord() {
        return word;
    }

    @Bindable
    public void setWord(String word) {
        this.word = word;
        notifyPropertyChanged(BR.word);
    }

    public int getFromLanguageIx() {
        return fromLanguageIx;
    }

    public void setFromLanguageIx(int fromLanguageIx) {
        this.fromLanguageIx = fromLanguageIx;
        appState.fromLanguageIx = fromLanguageIx;
    }

    public int getToLanguageIx() {
        return toLanguageIx;
    }

    public void setToLanguageIx(int toLanguageIx) {
        this.toLanguageIx = toLanguageIx;
        appState.toLanguageIx = toLanguageIx;
    }

    public void refreshSuggestion() {
        onTextChanged(getWord(), 0, 0, 0);
    }

    public Observable<MyAdapter> getAnswerPublishSubject() {
        return answerPublishSubject;
    }

    public Observable<String> getToastShowSubject() {
        return toastShowSubject;
    }

    public boolean isOfflineMode() {
        return appState.isOfflineMode;
    }

    public void setOfflineMode(boolean offlineMode) {
        appState.isOfflineMode = offlineMode;
    }

    public int getActivityTitleResId() {
        return !networkService.isNetworkConnected() || isOfflineMode() ? R.string.app_name_offline : R.string.app_name;
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s == null || s.length() == 0) {
            return;
        }
        String langFrom = LANG_ORDER_STR[fromLanguageIx];
        String langTo = LANG_ORDER_STR[toLanguageIx];
        if (!networkService.isNetworkConnected() || isOfflineMode()) {
            List<String> strings = translationService.getSuggestions(s.toString(), langFrom, appState.suggestionCount)
                    .subscribeOn(Schedulers.io())
                    .firstElement()
                    .blockingGet();

            adapter = new MyAdapter(strings, translationService, clipboardManager, vibrator, toastShowSubject, resources, langFrom, langTo);
            answerPublishSubject.onNext(adapter);
            return;
        }

        APIInterface apiInterface = retrofit.create(APIInterface.class);
        Call<Answer> call = apiInterface.doGetSuggestions(
                langFrom,
                langTo,
                s.toString(),
                "json",
                1,
                appState.suggestionCount);
        call.enqueue(new Callback<Answer>() {
            @Override
            public void onResponse(Call<Answer> call, Response<Answer> response) {
                if (response.code() != HTTP_OK) {
                    Log.e(getClass().getName(), response.raw().request().url().url() + " Http error " + response.code());
                    toastShowSubject.onNext(response.raw().request().url().url() + " Http error " + response.code());
                    return;
                }
                if (response.body() == null ||
                        response.body().result == null ||
                        response.body().result.size() == 0) {
                    Log.e(getClass().getName(), "Null answer");
                    toastShowSubject.onNext("Null answer");
                    return;
                }
                List<Suggest> suggest = response.body().result.get(0).suggest;
                List<String> sgList = new ArrayList<>();
                sgList.add(s.toString());
                for (Suggest sg : suggest) {
                    sgList.add(sg.value);
                }
                adapter = new MyAdapter(sgList, translationService, clipboardManager, vibrator, toastShowSubject, resources, langFrom, langTo);
                answerPublishSubject.onNext(adapter);
            }

            @Override
            public void onFailure(Call<Answer> call, Throwable t) {
                Log.e(getClass().getName(), t.getLocalizedMessage());
                toastShowSubject.onNext(t.getLocalizedMessage());
            }
        });
    }

    public MyAdapter getAdapter() {
        return adapter;
    }

    private MyAdapter getEmptyAdapter() {
        String langFrom = LANG_ORDER_STR[fromLanguageIx];
        String langTo = LANG_ORDER_STR[toLanguageIx];
        return new MyAdapter(Collections.EMPTY_LIST, translationService, clipboardManager, vibrator, toastShowSubject, resources, langFrom, langTo);
    }
}