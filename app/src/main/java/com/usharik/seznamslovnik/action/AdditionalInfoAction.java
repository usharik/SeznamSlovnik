package com.usharik.seznamslovnik.action;

public class AdditionalInfoAction implements Action {

    public static final int ADDITIONAL_INFO_ACTION = 5;

    private String word;

    @Override
    public int getType() {
        return ADDITIONAL_INFO_ACTION;
    }

    public AdditionalInfoAction(String word) {
        this.word = word;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
