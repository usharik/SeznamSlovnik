package com.usharik.seznamslovnik.framework;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.AndroidSupportInjection;
import dagger.android.support.HasSupportFragmentInjector;

import javax.inject.Inject;

public abstract class ViewFragment<T extends ViewModel> extends Fragment implements HasSupportFragmentInjector {

    private T viewModel;

    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;

    @Inject
    ViewModelProvider.Factory appViewModelFactory;

    protected abstract Class<T> getViewModelClass();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        AndroidSupportInjection.inject(this);
        viewModel = ViewModelProviders
                .of(this, appViewModelFactory)
                .get(getViewModelClass());
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    public T getViewModel() {
        return viewModel;
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return dispatchingAndroidInjector;
    }
}

