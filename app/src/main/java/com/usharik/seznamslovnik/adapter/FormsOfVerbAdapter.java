package com.usharik.seznamslovnik.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.usharik.seznamslovnik.R;
import com.usharik.seznamslovnik.service.WordInfoService;

import java.util.Map;

public class FormsOfVerbAdapter extends RecyclerView.Adapter<FormsOfVerbAdapter.ViewHolder>{

    private final Map<Integer, String> singularForms;
    private final Map<Integer, String> pluralForms;

    public class ViewHolder extends RecyclerView.ViewHolder {

        View view;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
        }
    }

    public FormsOfVerbAdapter(final Map<Integer, String> singularForms,
                              final Map<Integer, String> pluralForms) {
        this.singularForms = singularForms;
        this.pluralForms = pluralForms;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_verb_form, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FormsOfVerbAdapter.ViewHolder holder, int position) {
        setText(holder.view, R.id.formNum, Integer.toString(position+1));
        setText(holder.view, R.id.formName, WordInfoService.verbFormsByNum.get(position+1));
        setText(holder.view, R.id.caseSingular, singularForms.get(position+1));
        setText(holder.view, R.id.formPlural, pluralForms.get(position+1));
    }

    private void setText(View parent, int id, String text) {
        TextView tv = parent.findViewById(id);
        tv.setText(text);
    }

    @Override
    public int getItemCount() {
        return WordInfoService.verbFormsByNum.size();
    }
}