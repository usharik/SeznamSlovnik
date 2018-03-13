package com.usharik.seznamslovnik.action;

/**
 * Created by au185034 on 13/03/2018.
 */

public class OpenUrlInBrowserAction implements Action {

    public static final int OPEN_URL_IN_BROWSER = 2;

    private String url;

    public OpenUrlInBrowserAction(String url) {
        this.url = url;
    }

    @Override
    public int getType() {
        return OPEN_URL_IN_BROWSER;
    }

    public String getUrl() {
        return url;
    }
}
