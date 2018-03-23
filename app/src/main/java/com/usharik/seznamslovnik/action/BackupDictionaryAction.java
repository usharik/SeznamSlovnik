package com.usharik.seznamslovnik.action;

/**
 * Created by macbook on 13.03.2018.
 */

public class BackupDictionaryAction implements Action {

    public static final int BACKUP_DICTIONARY_ACTION = 3;

    @Override
    public int getType() {
        return BACKUP_DICTIONARY_ACTION;
    }
}
