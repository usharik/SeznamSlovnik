package com.usharik.seznamslovnik.util;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.usharik.seznamslovnik.R;

import io.reactivex.CompletableTransformer;
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
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            return dialog;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.wait_dialog_fragment, container, false);
        }
    }

    public static <T> ObservableTransformer<T, T> showForObservable(FragmentManager fragmentManager) {
        ActionConsumer actionConsumer = new ActionConsumer(WaitDialogFragment.newInstance(), fragmentManager);
        return observable -> observable
                .doOnSubscribe(actionConsumer)
                .doOnComplete(actionConsumer)
                .doOnTerminate(actionConsumer)
                .doOnDispose(actionConsumer);
    }

    public static CompletableTransformer showForCompletable(FragmentManager fragmentManager) {
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
