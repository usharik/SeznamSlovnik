package com.usharik.seznamslovnik.service.message;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.usharik.seznamslovnik.action.Action;
import com.usharik.seznamslovnik.action.ShowToastAction;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import io.reactivex.subjects.PublishSubject;

public class SlovnikFirebaseMessagingService extends FirebaseMessagingService {

    @Inject
    PublishSubject<Action> executeActionSubject;

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidInjection.inject(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.i(getClass().getSimpleName(), "From " + remoteMessage.getFrom());
        Log.i(getClass().getSimpleName(), "Message " + remoteMessage.getNotification().getBody());
        executeActionSubject.onNext(new ShowToastAction(remoteMessage.getNotification().getBody()));
    }
}
