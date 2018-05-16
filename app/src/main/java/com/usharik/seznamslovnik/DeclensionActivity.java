package com.usharik.seznamslovnik;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;

import com.usharik.seznamslovnik.action.Action;
import com.usharik.seznamslovnik.action.ShowToastAction;
import com.usharik.seznamslovnik.databinding.ActivityDeclensionBinding;
import com.usharik.seznamslovnik.framework.ViewActivity;
import com.usharik.seznamslovnik.util.WaitDialogManager;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class DeclensionActivity extends ViewActivity<DeclensionViewModel> {

    private ActivityDeclensionBinding binding;
    private Disposable disposable;

    @Inject
    PublishSubject<Action> executeActionSubject;

    @Override
    protected void onResume() {
        super.onResume();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_declension);
        binding.setViewModel(getViewModel());
        binding.declensionList.setLayoutManager(new LinearLayoutManager(this));
        disposable = getViewModel().getAdapter()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(WaitDialogManager.showForObservable(getSupportFragmentManager()))
                .subscribe(binding.declensionList::setAdapter, this::onError);
        binding.linkToSource.setText(getViewModel().getLink());
   }

    public void onError(Throwable thr) {
        executeActionSubject.onNext(new ShowToastAction(thr.getLocalizedMessage() != null ? thr.getLocalizedMessage() : "null"));
        Log.e(getClass().getName(), thr.getLocalizedMessage(), thr);
    }

    @Override
    protected Class<DeclensionViewModel> getViewModelClass() {
        return DeclensionViewModel.class;
    }
}
