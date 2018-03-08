package com.usharik.seznamslovnik;

import android.databinding.Bindable;
import android.util.Log;

import com.usharik.seznamslovnik.adapter.MyAdapter;
import com.usharik.seznamslovnik.framework.ViewModelObservable;
import com.usharik.seznamslovnik.model.Answer;
import com.usharik.seznamslovnik.service.APIInterface;

import java.util.Collections;

import javax.inject.Inject;

import io.reactivex.Observable;
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

    private String text;
    private String word;
    private MyAdapter adapter;
    private int fromLanguageIx = 0;
    private int toLanguageIx = 1;

    private PublishSubject<MyAdapter> answerPublishSubject = PublishSubject.create();
    private PublishSubject<String> toastShowSubject = PublishSubject.create();

    @Inject
    public MainViewModel(final AppState appState,
                         final Retrofit retrofit) {
        this.appState = appState;
        this.retrofit = retrofit;
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

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        APIInterface apiInterface = retrofit.create(APIInterface.class);
        Call<Answer> call = apiInterface.doGetSuggestions(
                LANG_ORDER_STR[fromLanguageIx],
                LANG_ORDER_STR[toLanguageIx],
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
                adapter = new MyAdapter(response.body().result.get(0).suggest, retrofit, appState, toastShowSubject);
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

    public MyAdapter getEmptyAdapter() {
        return new MyAdapter(Collections.EMPTY_LIST, retrofit, appState, toastShowSubject);
    }
}