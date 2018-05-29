package com.usharik.seznamslovnik;

import com.usharik.seznamslovnik.dialog.ProxyInfo;

public class AppState {
    public int fromLanguageIx = 0;
    public int toLanguageIx = 1;
    public int suggestionCount = 50;
    public boolean isOfflineMode = false;
    public String word;
    public ProxyInfo proxyInfo = ProxyInfo.DIRECT_PROXY;
}
