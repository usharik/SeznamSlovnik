package com.usharik.seznamslovnik.model.json.slovnik;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Samp2 {

    @SerializedName("samp2s")
    @Expose
    private String samp2s;
    @SerializedName("samp2t")
    @Expose
    private String samp2t;

    public String getSamp2s() {
        return samp2s;
    }

    public void setSamp2s(String samp2s) {
        this.samp2s = samp2s;
    }

    public String getSamp2t() {
        return samp2t;
    }

    public void setSamp2t(String samp2t) {
        this.samp2t = samp2t;
    }

}
