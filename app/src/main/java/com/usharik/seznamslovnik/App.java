package com.usharik.seznamslovnik;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.usharik.seznamslovnik.action.Action;
import com.usharik.seznamslovnik.action.BackupDictionaryAction;
import com.usharik.seznamslovnik.action.DeclensionAction;
import com.usharik.seznamslovnik.action.OpenUrlInBrowserAction;
import com.usharik.seznamslovnik.action.RestoreDictionaryAction;
import com.usharik.seznamslovnik.action.ShowToastAction;
import com.usharik.seznamslovnik.dao.DatabaseManager;
import com.usharik.seznamslovnik.di.DaggerAppComponent;

import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by macbook on 08.02.18.
 */

public class App extends Application implements HasActivityInjector {

    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;

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
                .subscribe(this::showAggregatedToast);
    }

    private Observable<String> handleActions(Action action) {
        switch (action.getType()) {
            case ShowToastAction.SHOW_TOAST:
                return Observable.just(((ShowToastAction) action).getMessage());

            case OpenUrlInBrowserAction.OPEN_URL_IN_BROWSER:
                String url = ((OpenUrlInBrowserAction) action).getUrl();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(browserIntent);
                return Observable.empty();

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

            case DeclensionAction.DECLENSION_ACTION_ACTION:
                appState.word = ((DeclensionAction) action).getWord();
                Intent intent = new Intent(this, DeclensionActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return Observable.empty();

            default:
                return Observable.empty();
        }
    }

    private void showAggregatedToast(Observable<String> message) {
        message.toList()
                .map(HashSet::new)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((set) -> {
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
                });
    }

    @Override
    public DispatchingAndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidInjector;
    }
}
