package com.usharik.seznamslovnik;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.usharik.seznamslovnik.action.Action;
import com.usharik.seznamslovnik.action.ShowToastAction;
import com.usharik.seznamslovnik.databinding.ActivityDeclensionBinding;
import com.usharik.seznamslovnik.dialog.ProxyDialog;
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
        disposable = updateWordForms();
        binding.linkToSource.setText(getViewModel().getLink());
    }

    private Disposable updateWordForms() {
        return getViewModel().getAdapter()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(WaitDialogManager.showForObservable(getSupportFragmentManager()))
                .subscribe(binding.declensionList::setAdapter, this::onError);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dec_options, menu);
        return true;
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
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("proxy_dialog");
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
