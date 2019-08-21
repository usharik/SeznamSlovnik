package com.usharik.seznamslovnik;

import android.os.Bundle;
import android.view.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.util.Log;

import com.usharik.seznamslovnik.action.Action;
import com.usharik.seznamslovnik.action.ShowToastAction;
import com.usharik.seznamslovnik.databinding.FragmentDeclensionBinding;
import com.usharik.seznamslovnik.dialog.ProxyDialog;
import com.usharik.seznamslovnik.framework.ViewFragment;
import com.usharik.seznamslovnik.util.WaitDialogManager;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class DeclensionFragment extends ViewFragment<DeclensionViewModel> {

    private FragmentDeclensionBinding binding;
    private Disposable disposable;

    @Inject
    PublishSubject<Action> executeActionSubject;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_declension, container, false);
        binding.setViewModel(getViewModel());
        binding.declensionList.setLayoutManager(new LinearLayoutManager(this.getContext()));
        disposable = updateWordForms();
        binding.linkToSource.setText(getViewModel().getLink());
        setHasOptionsMenu(true);
        return binding.getRoot();
    }

    private Disposable updateWordForms() {
        return getViewModel().getAdapter()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(WaitDialogManager.showForObservable(getFragmentManager()))
                .subscribe(binding.declensionList::setAdapter, this::onError);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.proxyDialog:
                showProxyDialog();
                return true;
            case R.id.updateInfo:
                disposable = updateWordForms();
                return true;
            default:
                return true;
        }
    }

    private void showProxyDialog() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("proxy_dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        ProxyDialog proxyDialog = new ProxyDialog();
        proxyDialog.show(ft, "proxy_dialog");
    }

    public void onError(Throwable thr) {
        executeActionSubject.onNext(new ShowToastAction(thr.getLocalizedMessage() != null ? thr.getLocalizedMessage() : thr.getClass().getName()));
        Log.e(getClass().getName(), thr.getLocalizedMessage(), thr);
    }

    @Override
    protected Class<DeclensionViewModel> getViewModelClass() {
        return DeclensionViewModel.class;
    }
}
