package com.usharik.seznamslovnik;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.LinearLayoutManager;

import com.usharik.seznamslovnik.databinding.ActivityDeclensionBinding;
import com.usharik.seznamslovnik.framework.ViewActivity;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DeclensionActivity extends ViewActivity<DeclensionViewModel> {

    private ActivityDeclensionBinding binding;
    private Disposable disposable;

    @Override
    protected void onResume() {
        super.onResume();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_declension);
        binding.setViewModel(getViewModel());
        binding.declensionList.setLayoutManager(new LinearLayoutManager(this));
        disposable = getViewModel().getAdapter()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(binding.declensionList::setAdapter);
        binding.linkToSource.setText(getViewModel().getLink());
    }

    @Override
    protected Class<DeclensionViewModel> getViewModelClass() {
        return DeclensionViewModel.class;
    }
}
