package com.usharik.seznamslovnik.adapter;

import java.util.List;

public class TranslationResult {

    private long wordId;
    private String word;
    private List<String> translations;
    private String gender;

    public TranslationResult(String word, List<String> translations, String gender) {
        this.word = word;
        this.translations = translations;
        this.gender = gender != null ? gender : "";
    }

    public TranslationResult(String word, List<String> translations) {
        this.word = word;
        this.translations = translations;
        this.gender = "";
    }

    public long getWordId() {
        return wordId;
    }

    public String getWord() {
        return word;
    }

    public List<String> getTranslations() {
        return translations;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
