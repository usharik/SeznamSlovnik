package com.usharik.seznamslovnik.widget;

import android.content.Context;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by au185034 on 12/03/2018.
 */

public class TranslationTextView extends android.support.v7.widget.AppCompatTextView {

    private List<String> translations;
    private String text;
    private Map<Integer, Pair<Integer, Integer>> wordMap;
    private int selected;

    public TranslationTextView(Context context) {
        super(context);
    }

    public TranslationTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TranslationTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setTranslations(List<String> translations) {
        this.translations = translations;
        this.wordMap = new HashMap<>();
        this.text = concatListWithWordMapping(this.translations, this.wordMap);
        this.selected = -1;
        this.setText(text);
    }

    public String selectNextWord() {
        if (translations == null || translations.size() == 0) {
            return "";
        }
        if (selected < translations.size()-1) {
            selected++;
        } else {
            selected = 0;
        }
        String res = translations.get(selected);
        SpannableString str = new SpannableString(text);
        Pair<Integer, Integer> wordPos = wordMap.get(selected);
        str.setSpan(new UnderlineSpan(), wordPos.first, wordPos.second, 0);
        this.setText(str);
        return res;
    }

    private static String concatListWithWordMapping(List<String> list, Map<Integer, Pair<Integer, Integer>> wordMap) {
        StringBuilder sb = new StringBuilder();
        int ix=0;
        for (String str : list) {
            int start = sb.length();
            sb.append(str);
            sb.append(", ");
            int stop = sb.length() - 2;
            wordMap.put(ix++, Pair.create(start, stop));
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - 2, sb.length());
        }
        return sb.toString();
    }
}
