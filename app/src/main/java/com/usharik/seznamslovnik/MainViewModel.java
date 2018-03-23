package com.usharik.seznamslovnik;

import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.databinding.Bindable;
import android.os.Vibrator;

import com.usharik.seznamslovnik.action.Action;
import com.usharik.seznamslovnik.adapter.TranslationListAdapter;
import com.usharik.seznamslovnik.service.TranslationService;
import com.usharik.seznamslovnik.framework.ViewModelObservable;
import com.usharik.seznamslovnik.service.NetworkService;

import java.util.Collections;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import static com.usharik.seznamslovnik.MainActivity.LANG_ORDER_STR;

/**
 * MainViewModel
 */

public class MainViewModel extends ViewModelObservable {

    private static final String OFFLINE_MODE_PREF_KEY = "isOffline";

    private final AppState appState;
    private final TranslationService translationService;
    private final NetworkService networkService;
    private final PublishSubject<Action> executeActionSubject;
    private final Resources resources;
    private final ClipboardManager clipboardManager;
    private final Vibrator vibrator;
    private final SharedPreferences sharedPreferences;

    private String text;
    private String word;
    private TranslationListAdapter adapter;
    private int scrollPosition;
    private int fromLanguageIx = 0;
    private int toLanguageIx = 1;

    private PublishSubject<TranslationListAdapter> answerPublishSubject = PublishSubject.create();

    @Inject
    public MainViewModel(final AppState appState,
                         final TranslationService translationService,
                         final NetworkService networkService,
                         final PublishSubject<Action> executeActionSubject,
                         final Resources resources,
                         final ClipboardManager clipboardManager,
                         final Vibrator vibrator,
                         final SharedPreferences sharedPreferences) {
        this.appState = appState;
        this.translationService = translationService;
        this.networkService = networkService;
        this.executeActionSubject = executeActionSubject;
        this.resources = resources;
        this.clipboardManager = clipboardManager;
        this.vibrator = vibrator;
        this.sharedPreferences = sharedPreferences;
        this.adapter = getEmptyAdapter();
        this.scrollPosition = 0;
        this.appState.isOfflineMode = this.sharedPreferences.getBoolean(OFFLINE_MODE_PREF_KEY, false);
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

    public Observable<TranslationListAdapter> getAnswerPublishSubject() {
        return answerPublishSubject;
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
        if (s == null) {
            return;
        }
        String input = s.toString().trim();
        if (input.isEmpty()) {
            return;
        }
        String langFrom = LANG_ORDER_STR[fromLanguageIx];
        String langTo = LANG_ORDER_STR[toLanguageIx];

        translationService.getSuggestions(input,
                langFrom,
                langTo,
                appState.suggestionCount,
                !networkService.isNetworkConnected() || isOfflineMode()
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(strings -> {
                    scrollPosition = 0;
                    adapter = new TranslationListAdapter(strings, translationService, clipboardManager, vibrator, executeActionSubject, resources, langFrom, langTo);
                    answerPublishSubject.onNext(adapter);
                });
    }

    public TranslationListAdapter getAdapter() {
        return adapter;
    }

    public int getScrollPosition() {
        return scrollPosition;
    }

    public void setScrollPosition(int scrollPosition) {
        this.scrollPosition = scrollPosition;
    }

    private TranslationListAdapter getEmptyAdapter() {
        String langFrom = LANG_ORDER_STR[fromLanguageIx];
        String langTo = LANG_ORDER_STR[toLanguageIx];
        return new TranslationListAdapter(Collections.EMPTY_LIST, translationService, clipboardManager, vibrator, executeActionSubject, resources, langFrom, langTo);
    }

    @Override
    public void onCleared() {
        super.onCleared();
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean(OFFLINE_MODE_PREF_KEY, appState.isOfflineMode);
        edit.apply();
    }
}