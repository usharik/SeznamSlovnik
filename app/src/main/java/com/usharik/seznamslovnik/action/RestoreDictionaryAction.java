package com.usharik.seznamslovnik.action;

/**
 * Created by macbook on 13.03.2018.
 */

public class RestoreDictionaryAction implements Action {

    public static final int RESTORE_DICTIONARY_ACTION = 4;

    @Override
    public int getType() {
        return RESTORE_DICTIONARY_ACTION;
    }
}
