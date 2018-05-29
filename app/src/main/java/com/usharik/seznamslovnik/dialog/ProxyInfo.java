package com.usharik.seznamslovnik.dialog;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Objects;

public class ProxyInfo {
    public static final ProxyInfo DIRECT_PROXY = new ProxyInfo("", 0);

    private String description;
    private Proxy proxy;

    public ProxyInfo(String ip, int port) {
        if (ip.isEmpty()) {
            description = "DIRECT PROXY";
            proxy = Proxy.NO_PROXY;
            return;
        }
        proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
        this.description = String.format("%s:%d", ip, port);
    }

    public String getDescription() {
        return description;
    }

    public Proxy getProxy() {
        return proxy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProxyInfo proxyInfo = (ProxyInfo) o;
        return Objects.equals(description, proxyInfo.description) &&
                Objects.equals(proxy, proxyInfo.proxy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, proxy);
    }
}
