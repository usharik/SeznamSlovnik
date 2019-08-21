package com.usharik.seznamslovnik.action;

import com.usharik.seznamslovnik.adapter.TranslationResult;

public class AdditionalInfoAction implements Action {

    public static final int ADDITIONAL_INFO_ACTION = 5;

    private String word;

    private TranslationResult translationResult;

    @Override
    public int getType() {
        return ADDITIONAL_INFO_ACTION;
    }

    public AdditionalInfoAction(String word, TranslationResult translationResult) {
        this.word = word;
        this.translationResult = translationResult;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public TranslationResult getTranslationResult() {
        return translationResult;
    }
}
