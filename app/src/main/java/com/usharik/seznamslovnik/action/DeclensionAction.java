package com.usharik.seznamslovnik.action;

public class DeclensionAction implements Action {

    public static final int DECLENSION_ACTION_ACTION = 5;

    private String word;

    @Override
    public int getType() {
        return DECLENSION_ACTION_ACTION;
    }

    public DeclensionAction(String word) {
        this.word = word;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
