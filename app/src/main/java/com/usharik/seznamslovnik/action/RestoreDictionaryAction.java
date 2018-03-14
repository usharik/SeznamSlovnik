package com.usharik.seznamslovnik.action;

import android.app.Activity;

/**
 * Created by macbook on 13.03.2018.
 */

public class RestoreDictionaryAction implements Action {
    public static final int RESTORE_DICTIONARY_ACTION = 4;

    private Activity activity;

    public RestoreDictionaryAction(Activity activity) {
        this.activity = activity;
    }

    @Override
    public int getType() {
        return RESTORE_DICTIONARY_ACTION;
    }

    public Activity getActivity() {
        return activity;
    }
}
