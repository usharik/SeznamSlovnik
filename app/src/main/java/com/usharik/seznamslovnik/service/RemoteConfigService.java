package com.usharik.seznamslovnik.service;

import android.app.Activity;
import android.util.Log;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;


public class RemoteConfigService {

    private static final VersionInfo EMPTY = new VersionInfo();

    private final FirebaseRemoteConfig firebaseRemoteConfig;

    public RemoteConfigService(FirebaseRemoteConfig firebaseRemoteConfig) {
        this.firebaseRemoteConfig = firebaseRemoteConfig;
    }

    public void fetchConfiguration(Activity activity) {
        firebaseRemoteConfig
                .fetch(120)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        firebaseRemoteConfig.activateFetched();
                    } else {
                        Log.e(getClass().getName(), "Can't fetch latest configuration from Firebase.");
                    }
                });
    }

    public VersionInfo getVersionInfo() {
        String latestVersion = firebaseRemoteConfig.getString("latest_version");
        if (latestVersion == null || latestVersion.isEmpty()) {
            return EMPTY;
        } else {
            Gson gson = new Gson();
            try {
                return gson.fromJson(latestVersion, VersionInfo.class);
            } catch (JsonSyntaxException ex) {
                return EMPTY;
            }
        }
    }

    public static class VersionInfo {
        public String version;
        public Integer versionCode;
        public String versionApkUrl;
        public String versionDictionaryUrl;
    }
}
