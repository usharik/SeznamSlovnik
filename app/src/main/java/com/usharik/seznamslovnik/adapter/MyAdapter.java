package com.usharik.seznamslovnik.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.res.Resources;
import android.os.Vibrator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.usharik.seznamslovnik.R;
import com.usharik.seznamslovnik.service.TranslationService;
import com.usharik.seznamslovnik.widget.TranslationTextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.subjects.PublishSubject;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private final List<String> suggestList;
    private final Map<Integer, List<String>> translations;
    private final TranslationService translationService;
    private final ClipboardManager clipboardManager;
    private final Vibrator vibrator;
    private final PublishSubject<String> toastShowSubject;
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

    public MyAdapter(final List<String> suggestList,
                     final TranslationService translationService,
                     final ClipboardManager clipboardManager,
                     final Vibrator vibrator,
                     final PublishSubject<String> toastShowSubject,
                     final Resources resources,
                     final String langFrom,
                     final String langTo) {
        this.suggestList = suggestList;
        this.translationService = translationService;
        this.clipboardManager = clipboardManager;
        this.vibrator = vibrator;
        this.toastShowSubject = toastShowSubject;
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
        String word = suggestList.get(position);
        tvWord.setText(word);

        if (translations.containsKey(position)) {
            tvTranslation.setTranslations(translations.get(position));
            return;
        }
        translationService.translate(suggestList.get(position), langFrom, langTo)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                    translations.put(position, pair.second);
                    tvWord.setText(pair.first);
                    tvTranslation.setTranslations(pair.second);
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
        toastShowSubject.onNext(resources.getString(R.string.translation_is_copied, word));
    }

    private boolean onLongClickListener(View view) {
        TextView tvWord = view.findViewById(R.id.word);
        String word = tvWord.getText().toString();
        clipboardManager.setPrimaryClip(ClipData.newPlainText("translation", word));
        vibrator.vibrate(100);
        toastShowSubject.onNext(resources.getString(R.string.translation_is_copied, word));
        return true;
    }
}
