package com.usharik.seznamslovnik.model.json.suggest;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Suggest {

    @SerializedName("source")
    @Expose
    private String source;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("result")
    @Expose
    private List<Result> result = null;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Result> getResult() {
        return result;
    }

    public void setResult(List<Result> result) {
        this.result = result;
    }

}
