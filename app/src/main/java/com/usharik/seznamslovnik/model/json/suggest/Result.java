package com.usharik.seznamslovnik.model.json.suggest;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Result {

    @SerializedName("text")
    @Expose
    private List<Text> text = null;
    @SerializedName("leadingMetaData")
    @Expose
    private LeadingMetaData leadingMetaData;
    @SerializedName("itemType")
    @Expose
    private String itemType;

    public List<Text> getText() {
        return text;
    }

    public void setText(List<Text> text) {
        this.text = text;
    }

    public LeadingMetaData getLeadingMetaData() {
        return leadingMetaData;
    }

    public void setLeadingMetaData(LeadingMetaData leadingMetaData) {
        this.leadingMetaData = leadingMetaData;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

}
