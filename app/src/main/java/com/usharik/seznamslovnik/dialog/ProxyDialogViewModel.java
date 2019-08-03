package com.usharik.seznamslovnik.dialog;

import android.content.DialogInterface;
import androidx.databinding.Bindable;

import com.usharik.seznamslovnik.AppState;
import com.usharik.seznamslovnik.BR;
import com.usharik.seznamslovnik.framework.ViewModelObservable;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class ProxyDialogViewModel extends ViewModelObservable {

    static List<ProxyInfo> proxyList = buildProxyList();

    private static List<ProxyInfo> buildProxyList() {
        List<ProxyInfo> proxyList = new ArrayList<>();

        proxyList.add(new ProxyInfo("217.61.14.215", 3128));
        proxyList.add(new ProxyInfo("179.43.141.201",3128));
        proxyList.add(new ProxyInfo("109.248.156.65",8080));
        proxyList.add(new ProxyInfo("174.138.54.49", 3128));
        proxyList.add(new ProxyInfo("175.106.14.138", 8181));
        proxyList.add(new ProxyInfo("165.16.81.144", 8080));
        proxyList.add(new ProxyInfo("188.166.83.17", 3128));
        proxyList.add(new ProxyInfo("195.201.97.32", 8888));
        return proxyList;
    }

    private AppState appState;
    private int selectedProxyIndex;
    private boolean useProxy;

    @Inject
    ProxyDialogViewModel(AppState appState) {
        this.appState = appState;
        useProxy = appState.proxyInfo.getProxy() != Proxy.NO_PROXY;
        selectedProxyIndex = proxyList.indexOf(appState.proxyInfo);
    }

    @Bindable
    public int getSelectedProxyIndex() {
        return selectedProxyIndex;
    }

    public void setSelectedProxyIndex(int ix) {
        this.selectedProxyIndex = ix;
        notifyPropertyChanged(BR.selectedProxyIndex);
    }

    @Bindable
    public boolean isUseProxy() {
        return useProxy;
    }

    public void setUseProxy(boolean useProxy) {
        this.useProxy = useProxy;
    }

    public void onProxyDialogOk(DialogInterface dialogInterface, int i) {
        appState.proxyInfo = useProxy ? proxyList.get(selectedProxyIndex) : ProxyInfo.DIRECT_PROXY;
    }
}
