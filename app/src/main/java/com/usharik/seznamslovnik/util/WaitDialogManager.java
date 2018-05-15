package com.usharik.seznamslovnik.util;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.usharik.seznamslovnik.R;

import io.reactivex.ObservableTransformer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class WaitDialogManager {
    public static class WaitDialogFragment extends DialogFragment {

        public static WaitDialogFragment newInstance() {
            return new WaitDialogFragment();
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = super.onCreateDialog(savedInstanceState);
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            return dialog;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.wait_dialog_fragment, container, false);
        }
    }

    public static <T> ObservableTransformer<T, T> showWaitDialog(FragmentManager fragmentManager) {
        ActionConsumer actionConsumer = new ActionConsumer(WaitDialogFragment.newInstance(), fragmentManager);
        return observable -> observable
                .doOnSubscribe(actionConsumer)
                .doOnComplete(actionConsumer)
                .doOnTerminate(actionConsumer)
                .doOnDispose(actionConsumer);
    }

    private static class ActionConsumer implements Action, Consumer<Disposable> {

        private WaitDialogFragment dialogFragment;
        private FragmentManager fragmentManager;

        ActionConsumer(WaitDialogFragment dialogFragment, FragmentManager fragmentManager) {
            this.dialogFragment = dialogFragment;
            this.fragmentManager = fragmentManager;
        }

        @Override
        public void accept(Disposable disposable) {
            dialogFragment.show(fragmentManager, "wait_dialog");
        }

        @Override
        public void run() {
            dialogFragment.dismiss();
        }
    }
}
