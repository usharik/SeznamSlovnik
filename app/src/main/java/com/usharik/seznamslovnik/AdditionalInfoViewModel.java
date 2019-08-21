package com.usharik.seznamslovnik;

import androidx.databinding.Bindable;
import com.usharik.seznamslovnik.framework.ViewModelObservable;

import javax.inject.Inject;

public class AdditionalInfoViewModel extends ViewModelObservable {

    private final AppState appState;

    @Inject
    public AdditionalInfoViewModel(final AppState appState) {
        this.appState = appState;
    }

    @Bindable
    public int getCurrentTab() {
        return appState.getCurrentTab();
    }

    public void setCurrentTab(int currentTab) {
        appState.setCurrentTab(currentTab);
    }
}
