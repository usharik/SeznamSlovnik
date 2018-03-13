package com.usharik.seznamslovnik.action;

/**
 * Created by au185034 on 13/03/2018.
 */

public class ShowToastAction implements Action {

    public static final int SHOW_TOAST = 1;

    private String message;

    public ShowToastAction(String message) {
        this.message = message;
    }

    @Override
    public int getType() {
        return SHOW_TOAST;
    }

    public String getMessage() {
        return message;
    }
}
