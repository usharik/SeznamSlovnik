package com.usharik.seznamslovnik.model.json.slovnik;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Other {

    @SerializedName("entr")
    @Expose
    private String entr;
    @SerializedName("dict")
    @Expose
    private String dict;
    @SerializedName("trans")
    @Expose
    private String trans;

    public String getEntr() {
        return entr;
    }

    public void setEntr(String entr) {
        this.entr = entr;
    }

    public String getDict() {
        return dict;
    }

    public void setDict(String dict) {
        this.dict = dict;
    }

    public String getTrans() {
        return trans;
    }

    public void setTrans(String trans) {
        this.trans = trans;
    }

}

