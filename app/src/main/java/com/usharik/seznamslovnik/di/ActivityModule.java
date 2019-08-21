package com.usharik.seznamslovnik.di;

import com.usharik.seznamslovnik.AdditionalInfoActivity;
import com.usharik.seznamslovnik.DeclensionFragment;
import com.usharik.seznamslovnik.MainActivity;
import com.usharik.seznamslovnik.dialog.ProxyDialog;
import com.usharik.seznamslovnik.service.message.SlovnikFirebaseMessagingService;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Created by macbook on 10.02.18.
 */

@Module
public abstract class ActivityModule {
    @ContributesAndroidInjector
    abstract MainActivity contributeMainActivity();

    @ContributesAndroidInjector
    abstract AdditionalInfoActivity contributeAdditionalInfoActivity();

    @ContributesAndroidInjector
    abstract DeclensionFragment contributeDeclensionFragment();

    @ContributesAndroidInjector
    abstract ProxyDialog contributeProxyDialog();

    @ContributesAndroidInjector
    abstract SlovnikFirebaseMessagingService slovnikFirebaseMessagingService();
}
