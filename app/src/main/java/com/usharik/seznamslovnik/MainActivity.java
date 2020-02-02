package com.usharik.seznamslovnik;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.databinding.DataBindingUtil;
import android.net.Uri;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.usharik.seznamslovnik.action.Action;
import com.usharik.seznamslovnik.action.BackupDictionaryAction;
import com.usharik.seznamslovnik.action.PrevWordAction;
import com.usharik.seznamslovnik.action.RestoreDictionaryAction;
import com.usharik.seznamslovnik.action.ShowToastAction;
import com.usharik.seznamslovnik.dao.DatabaseManager;
import com.usharik.seznamslovnik.databinding.ActivityMainBinding;
import com.usharik.seznamslovnik.framework.ViewActivity;
import com.usharik.seznamslovnik.service.RemoteConfigService;
import com.usharik.seznamslovnik.util.WaitDialogManager;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import static com.usharik.seznamslovnik.util.IOUtils.downloadFromUrl;

public class MainActivity extends ViewActivity<MainViewModel> {

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Inject
    PublishSubject<Action> executeActionSubject;

    @Inject
    DatabaseManager databaseManager;

    @Inject
    RemoteConfigService remoteConfigService;

    private ActivityMainBinding binding;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private PublishSubject<Boolean> permissionRequestSubject;

    private int fromLanguageIx = 0;
    private int toLanguageIx = 1;

    private volatile boolean isDictLoadingInProgress = false;

    public final static int[] LANG_ORDER = {R.drawable.cz, R.drawable.ru, R.drawable.gb};
    public final static String[] LANG_ORDER_STR = {"cz", "ru", "en"};
    public final static int CZ_INDEX = 0;

    @Override
    protected void onResume() {
        super.onResume();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setViewModel(getViewModel());
        binding.myRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.myRecyclerView.setAdapter(getViewModel().getAdapter());
        fromLanguageIx = getViewModel().getFromLanguageIx();
        toLanguageIx = getViewModel().getToLanguageIx();
        binding.btFrom.setImageDrawable(getResources().getDrawable(LANG_ORDER[fromLanguageIx], null));
        binding.btTo.setImageDrawable(getResources().getDrawable(LANG_ORDER[toLanguageIx], null));
        binding.myRecyclerView.setOnTouchListener((view, event) -> {
            hideSoftKeyboard(this);
            return view.performClick();
        });

        compositeDisposable.add(getViewModel().getAnswerPublishSubject().subscribe(adapter -> {
            binding.myRecyclerView.setAdapter(adapter);
            binding.myRecyclerView.getLayoutManager().scrollToPosition(getViewModel().getScrollPosition());
        }));
        if (!isDictLoadingInProgress) {
            checkPermissionAndExecute(this::loadDictionaryFromUrl);
        }

        remoteConfigService.fetchConfiguration(this);

        getViewModel().refreshSuggestion();
    }

    @SuppressLint("CheckResult")
    private void loadDictionaryFromUrl() {
        databaseManager.getActiveDbInstance().translationStorageDao().getWordCount()
                .filter(val -> val == 0)
                .flatMapCompletable(val -> {
                    isDictLoadingInProgress = true;
                    executeActionSubject.onNext(new ShowToastAction("Downloading dictionary file"));
                    databaseManager.restoreFromUrl();
                    executeActionSubject.onNext(new ShowToastAction("Dictionary file successfully downloaded"));
                    return Completable.complete();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(WaitDialogManager.showForCompletable(getSupportFragmentManager()))
                .subscribe(() -> {
                }, this::onError);
    }

    @Override
    protected void onPause() {
        compositeDisposable.clear();
        int position = ((LinearLayoutManager) binding.myRecyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
        getViewModel().setScrollPosition(position);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        MenuItem item = menu.findItem(R.id.onlineMode);
        CheckBox checkBox = (CheckBox) item.getActionView();
        checkBox.setOnCheckedChangeListener(this::onCheckedChanged);
        checkBox.setChecked(!getViewModel().isOfflineMode());

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem register = menu.findItem(R.id.new_version);
        RemoteConfigService.VersionInfo versionInfo = remoteConfigService.getVersionInfo();
        register.setVisible(versionInfo.versionCode != null && versionInfo.versionCode > BuildConfig.VERSION_CODE);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.backup:
                checkPermissionAndExecute(() -> executeActionSubject.onNext(new BackupDictionaryAction()));
                return true;
            case R.id.restore:
                checkPermissionAndExecute(() -> executeActionSubject.onNext(new RestoreDictionaryAction()));
                return true;
            case R.id.new_version:
                RemoteConfigService.VersionInfo versionInfo = remoteConfigService.getVersionInfo();
                File file;
                try {
                    file = File.createTempFile("new_version", ".apk", getExternalCacheDir());
                } catch (IOException e) {
                    Log.e(getClass().getName(), "Can't create temp file to download apk", e);
                    return true;
                }
                Uri apkURI = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);

                Completable.fromAction(() -> downloadFromUrl(versionInfo.versionApkUrl, file))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .andThen(Completable.fromAction(() -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(apkURI, "application/vnd.android.package-archive");
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(intent);
                        }))
                        .subscribe(
                                () -> Log.i(getClass().getName(), "Completed"),
                                thr -> Log.e(getClass().getName(), "Error", thr));

                return true;
            default:
                return true;
        }
    }

    @Override
    public void onBackPressed() {
        executeActionSubject.onNext(new PrevWordAction());
    }

    private void checkPermissionAndExecute(io.reactivex.functions.Action action) {
        if (isExternalStoragePermitted()) {
            try {
                action.run();
                return;
            } catch (Exception ex) {
                onError(ex);
                return;
            }
        }
        permissionRequestSubject = PublishSubject.create();
        permissionRequestSubject.subscribe(
                allowed -> {
                    if (allowed) {
                        action.run();
                    }
                },
                this::onError);
        requestStoragePermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grants) {
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            for (int permission : grants) {
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    permissionRequestSubject.onNext(false);
                    permissionRequestSubject.onComplete();
                    return;
                }
            }
            permissionRequestSubject.onNext(true);
            permissionRequestSubject.onComplete();
        }
    }

    private boolean isExternalStoragePermitted() {
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermissions() {
        ActivityCompat.requestPermissions(
                this,
                PERMISSIONS_STORAGE,
                REQUEST_EXTERNAL_STORAGE
        );
    }

    private void updateTitle() {
        setTitle(getViewModel().getActivityTitleResId());
    }

    public void onFromClick(View v) {
        if (fromLanguageIx == LANG_ORDER.length - 1) {
            fromLanguageIx = 0;
        } else {
            fromLanguageIx++;
        }
        refreshFromButton();

        int ix = fromLanguageIx != 0 && toLanguageIx != 0 ? 0 : toLanguageIx;
        ix = fromLanguageIx == 0 && toLanguageIx == 0 ? 1 : ix;
        if (toLanguageIx != ix) {
            toLanguageIx = ix;
            refreshToButton();
        }
        getViewModel().setTranslationMode(fromLanguageIx, toLanguageIx);
        getViewModel().refreshSuggestion();
    }

    public void onToClick(View v) {
        if (toLanguageIx == LANG_ORDER.length - 1) {
            toLanguageIx = 0;
        } else {
            toLanguageIx++;
        }
        refreshToButton();

        int ix = fromLanguageIx != 0 && toLanguageIx != 0 ? 0 : fromLanguageIx;
        ix = fromLanguageIx == 0 && toLanguageIx == 0 ? 1 : ix;
        if (fromLanguageIx != ix) {
            fromLanguageIx = ix;
            refreshFromButton();
        }
        getViewModel().setTranslationMode(fromLanguageIx, toLanguageIx);
        getViewModel().refreshSuggestion();
    }

    public void onSwapClick(View v) {
        int tmp = fromLanguageIx;
        fromLanguageIx = toLanguageIx;
        toLanguageIx = tmp;
        refreshFromButton();
        refreshToButton();
        getViewModel().setTranslationMode(fromLanguageIx, toLanguageIx);
        getViewModel().refreshSuggestion();
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        getViewModel().setOfflineMode(!isChecked);
        updateTitle();
    }

    private void refreshFromButton() {
        binding.btFrom.setImageDrawable(getResources().getDrawable(LANG_ORDER[fromLanguageIx], null));
    }

    private void refreshToButton() {
        binding.btTo.setImageDrawable(getResources().getDrawable(LANG_ORDER[toLanguageIx], null));
    }

    public static void hideSoftKeyboard(Activity activity) {
        if (activity.getCurrentFocus() == null) {
            return;
        }
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager == null) {
            return;
        }
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    public void onError(Throwable thr) {
        executeActionSubject.onNext(new ShowToastAction(thr.getLocalizedMessage() != null ? thr.getLocalizedMessage() : thr.getClass().getName()));
        Log.e(getClass().getName(), thr.getLocalizedMessage(), thr);
    }

    @Override
    protected Class<MainViewModel> getViewModelClass() {
        return MainViewModel.class;
    }
}
