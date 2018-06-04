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

import com.usharik.seznamslovnik.MainActivity;
import com.usharik.seznamslovnik.R;
import com.usharik.seznamslovnik.UrlRepository;
import com.usharik.seznamslovnik.action.Action;
import com.usharik.seznamslovnik.action.DeclensionAction;
import com.usharik.seznamslovnik.action.OpenUrlInBrowserAction;
import com.usharik.seznamslovnik.action.ShowToastAction;
import com.usharik.seznamslovnik.action.TranslateWordAction;
import com.usharik.seznamslovnik.service.TranslationService;
import com.usharik.seznamslovnik.widget.TranslationTextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.PublishSubject;

public class TranslationListAdapter extends RecyclerView.Adapter<TranslationListAdapter.ViewHolder> {

    private final List<String> suggestList;
    private final Map<Integer, TranslationResult> translations;
    private final TranslationService translationService;
    private final ClipboardManager clipboardManager;
    private final Vibrator vibrator;
    private final PublishSubject<Action> executeActionSubject;
    private final Resources resources;
    private final int langFromIx;
    private final int langToIx;

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
                                  final int langFromIx,
                                  final int langToIx) {
        this.suggestList = suggestList;
        this.translationService = translationService;
        this.clipboardManager = clipboardManager;
        this.vibrator = vibrator;
        this.executeActionSubject = executeActionSubject;
        this.resources = resources;
        this.langFromIx = langFromIx;
        this.langToIx = langToIx;
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
        TextView tvGender = holder.view.findViewById(R.id.gender);
        tvWord.setText(suggestList.get(position));
        TextView optionsMenuButton = holder.view.findViewById(R.id.optionsMenuButton);
        optionsMenuButton.setOnClickListener(this::onOptionsMenuClick);

        if (translations.containsKey(position)) {
            TranslationResult result = translations.get(position);
            tvTranslation.setTranslations(result.getTranslations());
            tvGender.setText(result.getGender());
            return;
        }
        translationService.translate(suggestList.get(position),
                MainActivity.LANG_ORDER_STR[langFromIx],
                MainActivity.LANG_ORDER_STR[langToIx])
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            translations.put(position, result);
                            suggestList.set(position, result.getWord());
                            tvWord.setText(result.getWord());
                            tvTranslation.setTranslations(result.getTranslations());
                            tvGender.setText(result.getGender());
                        },
                        thr -> {
                            Log.e(getClass().getName(), thr.getLocalizedMessage(), thr);
                            executeActionSubject.onNext(new ShowToastAction(thr.getLocalizedMessage()));
                        },
                        () -> { });
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
        popup.getMenu().findItem(R.id.additionalInfo).setVisible(langFromIx == MainActivity.CZ_INDEX);
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.openInBrowser: {
                    int position = (Integer) ((View) view.getParent()).getTag();
                    executeActionSubject.onNext(new OpenUrlInBrowserAction(String.format("%s%s-%s?q=%s",
                            UrlRepository.SEZNAM_TRANSLATE,
                            MainActivity.LANG_ORDER_STR[langFromIx],
                            MainActivity.LANG_ORDER_STR[langToIx],
                            suggestList.get(position))));
                    break;
                }
                case R.id.additionalInfo: {
                    int position = (Integer) ((View) view.getParent()).getTag();
                    executeActionSubject.onNext(new DeclensionAction(suggestList.get(position)));
                    break;
                }
                case R.id.dictCom: {
                    int position = (Integer) ((View) view.getParent()).getTag();
                    executeActionSubject.onNext(new OpenUrlInBrowserAction(String.format("%s%s", UrlRepository.DICT_COM, suggestList.get(position))));
                    break;
                }
                case R.id.translateBack:
                    TranslationTextView tvTranslation = ((View) view.getParent()).findViewById(R.id.translations);
                    if (tvTranslation.isWordSelected()) {
                        executeActionSubject.onNext(new TranslateWordAction(tvTranslation.getSelectedWord(), langToIx, langFromIx));
                    }
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
