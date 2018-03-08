package com.usharik.seznamslovnik.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.usharik.seznamslovnik.AppState;
import com.usharik.seznamslovnik.R;
import com.usharik.seznamslovnik.model.Suggest;
import com.usharik.seznamslovnik.service.APIInterface;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.usharik.seznamslovnik.MainActivity.LANG_ORDER_STR;
import static java.net.HttpURLConnection.HTTP_OK;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private final List<Suggest> suggestList;
    private final Map<Integer, String> translations;
    private Retrofit retrofit;
    private AppState appState;
    private final PublishSubject<String> toastShowSubject;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View view;
        public ViewHolder(View v) {
            super(v);
            view = v;
        }
    }

    public MyAdapter(final List<Suggest> suggestList,
                     final Retrofit retrofit,
                     final AppState appState,
                     final PublishSubject<String> toastShowSubject) {
        this.suggestList = suggestList;
        this.retrofit = retrofit;
        this.appState = appState;
        this.toastShowSubject = toastShowSubject;
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
        String word = suggestList.get(position).value;
        tvWord.setText(word);

        if (translations.containsKey(position)) {
            tvTranslation.setText(translations.get(position));
            return;
        }
        translate(suggestList.get(position).value).subscribe(s -> {
            translations.put(position, s);
            tvTranslation.setText(s);
        });
    }

    @Override
    public int getItemCount() {
        return suggestList.size();
    }

    private void onClickListener(View view) {
        int position = (int) view.getTag();
        translate(suggestList.get(position).value).subscribe(s -> {
            TextView textView = view.findViewById(R.id.translations);
            textView.setText(s);
        });
    }

    private Observable<String> translate(String question) {
        PublishSubject<String> translationPublisher = PublishSubject.create();
        APIInterface apiInterface = retrofit.create(APIInterface.class);
        Call<ResponseBody> call = apiInterface.doTranslate(
                LANG_ORDER_STR[appState.fromLanguageIx],
                LANG_ORDER_STR[appState.toLanguageIx],
                question);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.code() != HTTP_OK) {
                    Log.e(getClass().getName(), "Http error " + response.code());
                    toastShowSubject.onNext("Http error " + response.code());
                    return;
                }
                try {
                    Document html = Jsoup.parse(response.body().string());
                    StringBuilder sb = new StringBuilder();
                    concatTranslations(sb, html.body().select("div#fastMeanings > a"));
                    concatTranslations(sb, html.body().select("div#fastMeanings > span"));
                    if (sb.length() > 0) {
                        sb.delete(sb.length()-2, sb.length());
                    }
                    translationPublisher.onNext(sb.toString());
                    translationPublisher.onComplete();
                } catch (Exception e) {
                    Log.e(getClass().getName(), e.getLocalizedMessage());
                    toastShowSubject.onNext(e.getLocalizedMessage());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(getClass().getName(), t.getLocalizedMessage());
                toastShowSubject.onNext(t.getLocalizedMessage());
            }
        });
        return translationPublisher;
    }

    private void concatTranslations(StringBuilder sb, Elements translations) {
        for (Element el : translations) {
            if (el.text().trim().length() > 0) {
                sb.append(el.text() + ", ");
            }
        }
    }
}
