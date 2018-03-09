package com.usharik.seznamslovnik;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.usharik.seznamslovnik.databinding.ActivityMainBinding;
import com.usharik.seznamslovnik.framework.ViewActivity;

public class MainActivity extends ViewActivity<MainViewModel> {

    private ActivityMainBinding binding;

    private int fromLanguageIx = 0;
    private int toLanguageIx = 1;

    public final static int[] LANG_ORDER = {R.drawable.cz, R.drawable.ru, R.drawable.gb};
    public final static String[] LANG_ORDER_STR = {"cz", "ru", "en"};

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
        updateTitle();

        getViewModel().getAnswerPublishSubject().subscribe((adapter) -> {
            binding.myRecyclerView.setAdapter(adapter);
        });

        getViewModel().getToastShowSubject().subscribe((msg) -> Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()) {
            case R.id.offlineMode:
                item.setChecked(!item.isChecked());
                getViewModel().setOfflineMode(item.isChecked());
                updateTitle();
                return true;
            default:
                return true;
        }
    }

    private void updateTitle() {
        setTitle(getViewModel().getActivityTitleResId());
    }

    public void onFromClick(View v) {
        if (fromLanguageIx == LANG_ORDER.length-1) {
            fromLanguageIx=0;
        } else {
            fromLanguageIx++;
        }
        binding.btFrom.setImageDrawable(getResources().getDrawable(LANG_ORDER[fromLanguageIx], null));
        getViewModel().setFromLanguageIx(fromLanguageIx);

        int ix = fromLanguageIx != 0 && toLanguageIx != 0 ? 0 : toLanguageIx;
        ix = fromLanguageIx == 0 && toLanguageIx == 0 ? 1 : ix;
        if (toLanguageIx != ix) {
            toLanguageIx = ix;
            binding.btTo.setImageDrawable(getResources().getDrawable(LANG_ORDER[toLanguageIx], null));
            getViewModel().setToLanguageIx(toLanguageIx);
        }
        getViewModel().refreshSuggestion();
    }

    public void onToClick(View v) {
        if (toLanguageIx == LANG_ORDER.length-1) {
            toLanguageIx=0;
        } else {
            toLanguageIx++;
        }
        binding.btTo.setImageDrawable(getResources().getDrawable(LANG_ORDER[toLanguageIx], null));
        getViewModel().setToLanguageIx(toLanguageIx);

        int ix = fromLanguageIx != 0 && toLanguageIx != 0 ? 0 : fromLanguageIx;
        ix = fromLanguageIx == 0 && toLanguageIx == 0 ? 1 : ix;
        if (fromLanguageIx != ix) {
            fromLanguageIx = ix;
            binding.btFrom.setImageDrawable(getResources().getDrawable(LANG_ORDER[fromLanguageIx], null));
            getViewModel().setFromLanguageIx(fromLanguageIx);
        }
        getViewModel().refreshSuggestion();
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

    @Override
    protected Class<MainViewModel> getViewModelClass() {
        return MainViewModel.class;
    }
}
