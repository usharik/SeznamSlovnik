package com.usharik.seznamslovnik.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.usharik.seznamslovnik.R;

import java.util.Map;

public class DeclensionAdapter extends RecyclerView.Adapter<DeclensionAdapter.ViewHolder> {

    private static class Case {
        int num;
        String name;
        String questions;
        String hint;

        public Case(int num, String name, String questions, String hint) {
            this.num = num;
            this.name = name;
            this.questions = questions;
            this.hint = hint;
        }
    }

    private static final Case[] cases = {
            new Case(1,"Nominativ","kdo? co?", ""),
            new Case(2,"Genitiv", "koho? čeho?", "bez"),
            new Case(3,"Dativ", "komu? čemu?", "ke"),
            new Case(4,"Akuzative", "koho? co?", "vidím"),
            new Case(5,"Vokative", "", ""),
            new Case(6,"Lokál", "(o) kom? (o) čem?", "o"),
            new Case(7,"Instrumentál", "kým? čím?", "s")
    };

    private final Map<Integer, String> singularDeclensions;
    private final Map<Integer, String> pluralDeclensions;

    public class ViewHolder extends RecyclerView.ViewHolder {

        View view;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
        }
    }

    public DeclensionAdapter(final Map<Integer, String> singularDeclensions,
                             final Map<Integer, String> pluralDeclensions) {
        this.singularDeclensions = singularDeclensions;
        this.pluralDeclensions = pluralDeclensions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_case, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        setText(holder.view, R.id.caseNum, Integer.toString(cases[position].num));
        setText(holder.view, R.id.caseName, cases[position].name);
        setText(holder.view, R.id.caseHint, cases[position].hint);
        setText(holder.view, R.id.caseQuestion, cases[position].questions);
        setText(holder.view, R.id.singular, singularDeclensions.get(position+1));
        setText(holder.view, R.id.plural, pluralDeclensions.get(position+1));
    }

    private void setText(View parent, int id, String text) {
        TextView tv = parent.findViewById(id);
        tv.setText(text);
    }

    @Override
    public int getItemCount() {
        return cases.length;
    }
}
