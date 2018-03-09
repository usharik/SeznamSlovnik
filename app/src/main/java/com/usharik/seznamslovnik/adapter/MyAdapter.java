package com.usharik.seznamslovnik.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.usharik.seznamslovnik.R;
import com.usharik.seznamslovnik.service.TranslationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private final List<String> suggestList;
    private final Map<Integer, String> translations;
    private TranslationService translationService;
    private final String langFrom;
    private final String langTo;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View view;
        public ViewHolder(View v) {
            super(v);
            view = v;
        }
    }

    public MyAdapter(final List<String> suggestList,
                     final TranslationService translationService,
                     final String langFrom,
                     final String langTo) {
        this.suggestList = suggestList;
        this.translationService = translationService;
        this.langFrom = langFrom;
        this.langTo = langTo;
        this.translations = new HashMap<>();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_layout, parent, false);
        v.setOnClickListener(this::onClickListener);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.view.setTag(position);
        TextView tvWord = holder.view.findViewById(R.id.word);
        TextView tvTranslation = holder.view.findViewById(R.id.translations);
        String word = suggestList.get(position);
        tvWord.setText(word);

        if (translations.containsKey(position)) {
            tvTranslation.setText(translations.get(position));
            return;
        }
        translationService.translate(suggestList.get(position), langFrom, langTo)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                    translations.put(position, pair.second);
                    tvWord.setText(pair.first);
                    tvTranslation.setText(pair.second);
                });
    }

    @Override
    public int getItemCount() {
        return suggestList.size();
    }

    private void onClickListener(View view) {
        int position = (int) view.getTag();
    }
}
