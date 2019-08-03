package com.usharik.seznamslovnik.model.json.slovnik;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Coll2 {

    @SerializedName("coll2s")
    @Expose
    private String coll2s;
    @SerializedName("coll2t")
    @Expose
    private String coll2t;

    public String getColl2s() {
        return coll2s;
    }

    public void setColl2s(String coll2s) {
        this.coll2s = coll2s;
    }

    public String getColl2t() {
        return coll2t;
    }

    public void setColl2t(String coll2t) {
        this.coll2t = coll2t;
    }

}
