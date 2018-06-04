package com.usharik.seznamslovnik.action;

public class PrevWordAction implements Action {

    public static final int PREV_WORD_ACTION = 7;

    @Override
    public int getType() {
        return PREV_WORD_ACTION;
    }
}
