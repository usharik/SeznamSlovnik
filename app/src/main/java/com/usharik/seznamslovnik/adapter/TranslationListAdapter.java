package com.usharik.seznamslovnik.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Vibrator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.usharik.seznamslovnik.R;
import com.usharik.seznamslovnik.action.Action;
import com.usharik.seznamslovnik.action.OpenUrlInBrowserAction;
import com.usharik.seznamslovnik.action.ShowToastAction;
import com.usharik.seznamslovnik.service.TranslationService;
import com.usharik.seznamslovnik.widget.TranslationTextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.PublishSubject;

public class TranslationListAdapter extends RecyclerView.Adapter<TranslationListAdapter.ViewHolder> {

    private final List<String> suggestList;
    private final Map<Integer, List<String>> translations;
    private final TranslationService translationService;
    private final ClipboardManager clipboardManager;
    private final Vibrator vibrator;
    private final PublishSubject<Action> executeActionSubject;
    private final Resources resources;
    private final String langFrom;
    private final String langTo;

    class ViewHolder extends RecyclerView.ViewHolder {

        View view;

        ViewHolder(View v) {
            super(v);
            view = v;
        }
    }

    public TranslationListAdapter(final List<String> suggestList,
                                  final TranslationService translationService,
                                  final ClipboardManager clipboardManager,
                                  final Vibrator vibrator,
                                  final PublishSubject<Action> executeActionSubject,
                                  final Resources resources,
                                  final String langFrom,
                                  final String langTo) {
        this.suggestList = suggestList;
        this.translationService = translationService;
        this.clipboardManager = clipboardManager;
        this.vibrator = vibrator;
        this.executeActionSubject = executeActionSubject;
        this.resources = resources;
        this.langFrom = langFrom;
        this.langTo = langTo;
        this.translations = new HashMap<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_layout, parent, false);
        v.setOnClickListener(this::onClickListener);
        v.setOnLongClickListener(this::onLongClickListener);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.view.setTag(position);
        TextView tvWord = holder.view.findViewById(R.id.word);
        TranslationTextView tvTranslation = holder.view.findViewById(R.id.translations);
        tvWord.setText(suggestList.get(position));
        TextView optionsMenuButton = holder.view.findViewById(R.id.optionsMenuButton);
        optionsMenuButton.setOnClickListener(this::onOptionsMenuClick);

        if (translations.containsKey(position)) {
            tvTranslation.setTranslations(translations.get(position));
            return;
        }
        translationService.translate(suggestList.get(position), langFrom, langTo)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        pair -> {
                            translations.put(position, pair.second);
                            suggestList.set(position, pair.first);
                            tvWord.setText(pair.first);
                            tvTranslation.setTranslations(pair.second);
                        },
                        thr -> {
                            Log.e(getClass().getName(), thr.getLocalizedMessage());
                            executeActionSubject.onNext(new ShowToastAction(thr.getLocalizedMessage()));
                        });
    }

    @Override
    public int getItemCount() {
        return suggestList.size();
    }

    private void onClickListener(View view) {
        TranslationTextView tvTranslation = view.findViewById(R.id.translations);
        String word = tvTranslation.selectNextWord();
        if (word.isEmpty()) {
            return;
        }
        clipboardManager.setPrimaryClip(ClipData.newPlainText("translation", word));
        vibrator.vibrate(100);
        executeActionSubject.onNext(new ShowToastAction(resources.getString(R.string.translation_is_copied, word)));
    }

    private boolean onLongClickListener(View view) {
        TextView tvWord = view.findViewById(R.id.word);
        String word = tvWord.getText().toString();
        clipboardManager.setPrimaryClip(ClipData.newPlainText("translation", word));
        vibrator.vibrate(100);
        executeActionSubject.onNext(new ShowToastAction(resources.getString(R.string.translation_is_copied, word)));
        return true;
    }

    private void onOptionsMenuClick(View view) {
        Context wrapper = new ContextThemeWrapper(view.getContext(), R.style.PopupMenu);
        PopupMenu popup = new PopupMenu(wrapper, view.findViewById(R.id.optionsMenuButton));
        popup.inflate(R.menu.translation_options_menu);
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.openInBrowser:
                    int position = (Integer) ((View) view.getParent()).getTag();
                    executeActionSubject.onNext(new OpenUrlInBrowserAction(String.format("https://slovnik.seznam.cz/%s-%s?q=%s", langFrom, langTo, suggestList.get(position))));
                    break;
                case R.id.copyAllToClipboard:
                    break;
                case R.id.editTranslation:
                    break;
            }
            return false;
        });
        popup.show();
    }
}
