package com.usharik.seznamslovnik.di;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.usharik.seznamslovnik.AdditionalInfoViewModel;
import com.usharik.seznamslovnik.DeclensionViewModel;
import com.usharik.seznamslovnik.MainViewModel;
import com.usharik.seznamslovnik.dialog.ProxyDialogViewModel;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;

/**
 * Created by macbook on 09.02.18.
 */

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel.class)
    abstract ViewModel bindMainViewModel(MainViewModel userViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(DeclensionViewModel.class)
    abstract ViewModel bindDeclensionViewModel(DeclensionViewModel userViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(AdditionalInfoViewModel.class)
    abstract ViewModel bindAdditionalInfoViewModel(AdditionalInfoViewModel userViewModel);

    @Binds
    @IntoMap
    @ViewModelKey(ProxyDialogViewModel.class)
    abstract ViewModel bindProxyDialogViewModel(ProxyDialogViewModel userViewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(AppViewModelFactory factory);
}
