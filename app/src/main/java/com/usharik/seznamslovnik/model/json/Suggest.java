package com.usharik.seznamslovnik.model.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Suggest {

    @SerializedName("value")
    @Expose
    public String value;
    @SerializedName("highlightStart")
    @Expose
    public Integer highlightStart;
    @SerializedName("highlightEnd")
    @Expose
    public Integer highlightEnd;
    @SerializedName("relevance")
    @Expose
    public Integer relevance;

    public static Suggest fromValue(String value) {
        Suggest suggest = new Suggest();
        suggest.value = value;
        suggest.highlightStart = 0;
        suggest.highlightEnd = 0;
        suggest.relevance = 1;
        return suggest;
    }
}
