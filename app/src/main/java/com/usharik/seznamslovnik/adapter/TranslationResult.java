package com.usharik.seznamslovnik.adapter;

import com.usharik.seznamslovnik.model.json.slovnik.Answer;

import java.util.List;

public class TranslationResult {

    private long wordId;
    private String word;
    private List<String> translations;
    private String gender;
    private String jsonAnswer;
    private Answer answer;

    public TranslationResult(String word, List<String> translations, String gender) {
        this.word = word;
        this.translations = translations;
        this.gender = gender != null ? gender : "";
    }

    public TranslationResult(String word, List<String> translations, String jsonAnswer, Answer answer) {
        this.word = word;
        this.translations = translations;
        this.gender = "";
        this.jsonAnswer = jsonAnswer;
        this.answer = answer;
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

    public String getJsonAnswer() {
        return jsonAnswer;
    }

    public void setJsonAnswer(String jsonAnswer) {
        this.jsonAnswer = jsonAnswer;
    }

    public Answer getAnswer() {
        return answer;
    }

    public void setAnswer(Answer answer) {
        this.answer = answer;
    }
}
