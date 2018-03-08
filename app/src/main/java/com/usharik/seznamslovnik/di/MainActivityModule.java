package com.usharik.seznamslovnik.di;

import com.usharik.seznamslovnik.MainActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Created by macbook on 10.02.18.
 */

@Module
public abstract class MainActivityModule {
    @ContributesAndroidInjector
    abstract MainActivity contributeMainActivity();
}
