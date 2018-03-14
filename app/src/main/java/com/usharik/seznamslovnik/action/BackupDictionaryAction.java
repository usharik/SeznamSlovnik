package com.usharik.seznamslovnik.action;


import android.app.Activity;

/**
 * Created by macbook on 13.03.2018.
 */

public class BackupDictionaryAction implements Action {

    public static final int BACKUP_DICTIONARY_ACTION = 3;

    private Activity activity;

    public BackupDictionaryAction(Activity activity) {
        this.activity = activity;
    }

    @Override
    public int getType() {
        return BACKUP_DICTIONARY_ACTION;
    }

    public Activity getActivity() {
        return activity;
    }
}
