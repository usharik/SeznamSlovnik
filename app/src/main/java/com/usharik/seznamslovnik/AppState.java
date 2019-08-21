package com.usharik.seznamslovnik;

import com.usharik.seznamslovnik.adapter.TranslationResult;
import com.usharik.seznamslovnik.dialog.ProxyInfo;

import java.util.Deque;
import java.util.LinkedList;

public class AppState {
    private int fromLanguageIx = 0;
    private int toLanguageIx = 1;
    private String word;

    private Deque<WordState> stateList = new LinkedList<>();

    public int suggestionCount = 50;
    public boolean isOfflineMode = false;
    public ProxyInfo proxyInfo = ProxyInfo.DIRECT_PROXY;
    public String wordForDeclension;
    private TranslationResult translationResult;

    private int currentTab;

    public int getFromLanguageIx() {
        return fromLanguageIx;
    }

    public int getToLanguageIx() {
        return toLanguageIx;
    }

    public void setTranslationMode(String word, int fromLanguageIx, int toLanguageIx) {
        if (this.word == null || !this.word.equals(word) || this.toLanguageIx != toLanguageIx || this.fromLanguageIx != fromLanguageIx) {
            stateList.push(new WordState(this.word, this.fromLanguageIx, this.toLanguageIx));
        }
        this.word = word;
        this.fromLanguageIx = fromLanguageIx;
        this.toLanguageIx = toLanguageIx;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        if (this.word == null || !this.word.equals(word)) {
            stateList.push(new WordState(this.word, this.fromLanguageIx, this.toLanguageIx));
        }
        this.word = word;
    }

    public void stateBack() {
        if (stateList.isEmpty()) {
            return;
        }
        WordState pop = stateList.pop();
        this.word = pop.word;
        this.fromLanguageIx = pop.fromLanguageIx;
        this.toLanguageIx = pop.toLanguageIx;
    }

    public int getCurrentTab() {
        return currentTab;
    }

    public void setCurrentTab(int currentTab) {
        this.currentTab = currentTab;
    }

    public TranslationResult getTranslationResult() {
        return translationResult;
    }

    public void setTranslationResult(TranslationResult translationResult) {
        this.translationResult = translationResult;
    }

    private static class WordState {
        final int fromLanguageIx;
        final int toLanguageIx;
        final String word;

        WordState(String word, int fromLanguageIx, int toLanguageIx) {
            this.fromLanguageIx = fromLanguageIx;
            this.toLanguageIx = toLanguageIx;
            this.word = word;
        }
    }
}
