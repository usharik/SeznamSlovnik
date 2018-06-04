package com.usharik.seznamslovnik;

import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.databinding.Bindable;
import android.os.Vibrator;
import android.util.Log;

import com.usharik.seznamslovnik.action.ShowToastAction;
import com.usharik.seznamslovnik.adapter.TranslationListAdapter;
import com.usharik.seznamslovnik.service.TranslationService;
import com.usharik.seznamslovnik.framework.ViewModelObservable;
import com.usharik.seznamslovnik.service.NetworkService;

import java.util.Collections;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
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
    private final PublishSubject<com.usharik.seznamslovnik.action.Action> executeActionSubject;
    private final Resources resources;
    private final ClipboardManager clipboardManager;
    private final Vibrator vibrator;
    private final SharedPreferences sharedPreferences;

    private TranslationListAdapter adapter;
    private int scrollPosition;
    private CompositeDisposable disposables = new CompositeDisposable();

    private PublishSubject<TranslationListAdapter> answerPublishSubject = PublishSubject.create();

    @Inject
    public MainViewModel(final AppState appState,
                         final TranslationService translationService,
                         final NetworkService networkService,
                         final PublishSubject<com.usharik.seznamslovnik.action.Action> executeActionSubject,
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
    public String getWord() {
        return appState.getWord();
    }

    @Bindable
    public void setWord(String word) {
        appState.setWord(word);
        notifyPropertyChanged(BR.word);
    }

    public int getFromLanguageIx() {
        return appState.getFromLanguageIx();
    }

    public void setTranslationMode(int fromLanguageIx, int toLanguageIx) {
        appState.setTranslationMode(appState.getWord(), fromLanguageIx, toLanguageIx);
    }

    public int getToLanguageIx() {
        return appState.getToLanguageIx();
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
        if (!networkService.isNetworkConnected()) {
            return R.string.app_name_no_internet;
        }
        return isOfflineMode() ? R.string.app_name_offline : R.string.app_name_online;
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s == null) {
            return;
        }
        appState.setWord(s.toString().trim());
        if (appState.getWord().isEmpty()) {
            return;
        }
        String langFrom = LANG_ORDER_STR[appState.getFromLanguageIx()];
        String langTo = LANG_ORDER_STR[appState.getToLanguageIx()];

        disposables.clear();
        disposables.add(translationService.getSuggestions(appState.getWord(),
                langFrom,
                langTo,
                appState.suggestionCount,
                !networkService.isNetworkConnected() || isOfflineMode()
        )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        strings -> {
                            scrollPosition = 0;
                            adapter = new TranslationListAdapter(strings, translationService, clipboardManager, vibrator,
                                    executeActionSubject, resources, appState.getFromLanguageIx(), appState.getToLanguageIx());
                            answerPublishSubject.onNext(adapter);
                        },
                        thr -> {
                            Log.e(getClass().getName(), thr.getLocalizedMessage(), thr);
                            executeActionSubject.onNext(new ShowToastAction(thr.getLocalizedMessage()));
                        })
        );
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
        return new TranslationListAdapter(Collections.EMPTY_LIST, translationService, clipboardManager,
                vibrator, executeActionSubject, resources, appState.getFromLanguageIx(), appState.getToLanguageIx());
    }

    @Override
    public void onCleared() {
        super.onCleared();
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean(OFFLINE_MODE_PREF_KEY, appState.isOfflineMode);
        edit.apply();
    }
}