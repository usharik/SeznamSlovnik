package com.usharik.seznamslovnik;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import com.usharik.seznamslovnik.action.Action;
import com.usharik.seznamslovnik.action.BackupDictionaryAction;
import com.usharik.seznamslovnik.action.AdditionalInfoAction;
import com.usharik.seznamslovnik.action.OpenUrlInBrowserAction;
import com.usharik.seznamslovnik.action.PrevWordAction;
import com.usharik.seznamslovnik.action.RestoreDictionaryAction;
import com.usharik.seznamslovnik.action.ShowToastAction;
import com.usharik.seznamslovnik.action.TranslateWordAction;
import com.usharik.seznamslovnik.dao.DatabaseManager;
import com.usharik.seznamslovnik.di.DaggerAppComponent;

import dagger.android.*;
import dagger.android.support.HasSupportFragmentInjector;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by macbook on 08.02.18.
 */

public class App extends Application implements HasActivityInjector, HasServiceInjector, HasSupportFragmentInjector {

    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;

    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidFragmentInjector;

    @Inject
    DispatchingAndroidInjector<Service> dispatchingServiceInjector;

    @Inject
    PublishSubject<Action> executeActionSubject;

    @Inject
    DatabaseManager databaseManager;

    @Inject
    AppState appState;

    @Override
    public void onCreate() {
        super.onCreate();
        DaggerAppComponent.builder().create(this).inject(this);
        databaseManager.getActiveDbInstance();
        executeActionSubject.flatMap(this::handleActions)
                .window(1500, TimeUnit.MILLISECONDS)
                .subscribe(this::showAggregatedToast, this::handleError);
    }

    private Observable<String> handleActions(Action action) {
        switch (action.getType()) {
            case ShowToastAction.SHOW_TOAST:
                return Observable.just(((ShowToastAction) action).getMessage());

            case OpenUrlInBrowserAction.OPEN_URL_IN_BROWSER: {
                String url = ((OpenUrlInBrowserAction) action).getUrl();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(browserIntent);
                return Observable.empty();
            }

            case BackupDictionaryAction.BACKUP_DICTIONARY_ACTION: {
                String message;
                try {
                    databaseManager.backup();
                    message = "Dictionary backup completed";
                } catch (Exception ex) {
                    message = ex.getLocalizedMessage();
                }
                return Observable.just(message);
            }

            case RestoreDictionaryAction.RESTORE_DICTIONARY_ACTION: {
                String message;
                try {
                    databaseManager.restore();
                    message = "Dictionary restore completed";
                } catch (Exception ex) {
                    message = ex.getLocalizedMessage();
                }
                return Observable.just(message);
            }

            case AdditionalInfoAction.ADDITIONAL_INFO_ACTION: {
                appState.wordForDeclension = ((AdditionalInfoAction) action).getWord();
                Intent intent = new Intent(this, AdditionalInfoActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return Observable.empty();
            }

            case TranslateWordAction.TRANSLATE_WORD: {
                TranslateWordAction ac = (TranslateWordAction) action;
                appState.setTranslationMode(StringUtils.stripAccents(ac.getWord()), ac.getLangFrom(), ac.getLangTo());
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return Observable.empty();
            }

            case PrevWordAction.PREV_WORD_ACTION: {
                appState.stateBack();
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return Observable.empty();
            }

            default:
                return Observable.empty();
        }
    }

    @SuppressLint("CheckResult")
    private void showAggregatedToast(Observable<String> message) {
        message.toList()
                .map(HashSet::new)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(set -> {
                            if (set.isEmpty()) {
                                return;
                            }
                            Iterator<String> iterator = set.iterator();
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < set.size(); i++) {
                                String template = i < set.size() - 1 ? "%s%n" : "%s";
                                sb.append(String.format(template, iterator.next()));
                            }
                            Toast.makeText(getApplicationContext(), sb, Toast.LENGTH_SHORT).show();
                        },
                        this::handleError);
    }

    private void handleError(Throwable thr) {
        String msg = thr.getLocalizedMessage() != null ? thr.getLocalizedMessage() : thr.getClass().getName();
        Log.e(getClass().getName(), "Exception", thr);
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public DispatchingAndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidInjector;
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return dispatchingAndroidFragmentInjector;
    }

    @Override
    public AndroidInjector<Service> serviceInjector() {
        return dispatchingServiceInjector;
    }
}
