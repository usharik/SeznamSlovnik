package com.usharik.seznamslovnik.model;

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

}
