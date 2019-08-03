package com.usharik.seznamslovnik.dialog;

import android.app.Dialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;

import com.usharik.seznamslovnik.R;
import com.usharik.seznamslovnik.databinding.ProxyDialogBinding;
import com.usharik.seznamslovnik.dialog.adapter.ProxySpinnerAdapter;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.AndroidSupportInjection;
import dagger.android.support.HasSupportFragmentInjector;

public class ProxyDialog extends DialogFragment implements HasSupportFragmentInjector {

    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;

    @Inject
    ViewModelProvider.Factory appViewModelFactory;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AndroidSupportInjection.inject(this);
        ProxyDialogViewModel viewModel = ViewModelProviders
                .of(this, appViewModelFactory)
                .get(getClass().getName(), ProxyDialogViewModel.class);

        ProxyDialogBinding binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.proxy_dialog, null, false);
        binding.setViewModel(viewModel);
        binding.proxies.setAdapter(new ProxySpinnerAdapter(getContext(), android.R.layout.simple_spinner_item, ProxyDialogViewModel.proxyList));

        return new AlertDialog.Builder(getActivity())
                .setView(binding.getRoot())
                .setTitle("Proxy settings")
                .setPositiveButton(R.string.OK, viewModel::onProxyDialogOk)
                .create();
    }

    @Override
    public AndroidInjector<Fragment> supportFragmentInjector() {
        return dispatchingAndroidInjector;
    }
}
