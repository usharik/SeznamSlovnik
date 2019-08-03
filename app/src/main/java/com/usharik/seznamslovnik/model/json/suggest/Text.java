package com.usharik.seznamslovnik.model.json.suggest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Text {

    @SerializedName("text")
    @Expose
    private String text;
    @SerializedName("emphasized")
    @Expose
    private Boolean emphasized;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean getEmphasized() {
        return emphasized;
    }

    public void setEmphasized(Boolean emphasized) {
        this.emphasized = emphasized;
    }

}
