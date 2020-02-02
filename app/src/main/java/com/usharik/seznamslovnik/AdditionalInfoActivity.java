package com.usharik.seznamslovnik;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.TabHost;
import androidx.databinding.DataBindingUtil;
import com.usharik.seznamslovnik.databinding.ActivityAdditionalInfoBinding;
import com.usharik.seznamslovnik.framework.ViewActivity;

public class AdditionalInfoActivity extends ViewActivity<AdditionalInfoViewModel> {

    private ActivityAdditionalInfoBinding binding;

    @Override
    protected Class<AdditionalInfoViewModel> getViewModelClass() {
        return AdditionalInfoViewModel.class;
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_additional_info);
        binding.setViewModel(getViewModel());

        binding.tabHost.setup();
        TabHost.TabSpec tabSpec = binding.tabHost.newTabSpec("tag1");
        tabSpec.setIndicator(getResources().getString(R.string.declension_tab));
        tabSpec.setContent(binding.tab1.getId());
        binding.tabHost.addTab(tabSpec);

        tabSpec = binding.tabHost.newTabSpec("tag2");
        tabSpec.setIndicator(getResources().getString(R.string.add_info_tab));
        tabSpec.setContent(binding.tab2.getId());
        binding.tabHost.addTab(tabSpec);

        binding.tabHost.setOnTabChangedListener(this::tabChangeListener);
    }

    private void tabChangeListener(String tabTag) {
        Log.e(getClass().getName(),"Tab change " + tabTag);
        if (tabTag.equals("tag2")) {
            binding.rvJson.bindJson(getViewModel().getWordJson());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.dec_options, menu);
        return true;
    }
}
