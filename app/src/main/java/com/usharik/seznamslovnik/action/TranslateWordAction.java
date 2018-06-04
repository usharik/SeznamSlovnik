package com.usharik.seznamslovnik.action;

public class TranslateWordAction implements Action {

    public static final int TRANSLATE_WORD = 6;
    private final String word;
    private final int langFrom;
    private final int langTo;

    public TranslateWordAction(final String word,
                               final int langFrom,
                               final int langTo) {
        this.word = word;
        this.langFrom = langFrom;
        this.langTo = langTo;
    }

    @Override
    public int getType() {
        return TRANSLATE_WORD;
    }

    public String getWord() {
        return word;
    }

    public int getLangFrom() {
        return langFrom;
    }

    public int getLangTo() {
        return langTo;
    }
}
