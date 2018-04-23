package com.usharik.seznamslovnik.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LanguageServiceResponse {

    @SerializedName("response")
    @Expose
    private String response;

    @SerializedName("text")
    @Expose
    private String text;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
