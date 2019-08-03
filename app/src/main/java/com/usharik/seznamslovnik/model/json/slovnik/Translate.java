package com.usharik.seznamslovnik.model.json.slovnik;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Translate {

    @SerializedName("head")
    @Expose
    private Head head;
    @SerializedName("grps")
    @Expose
    private List<Grp> grps = null;

    public Head getHead() {
        return head;
    }

    public void setHead(Head head) {
        this.head = head;
    }

    public List<Grp> getGrps() {
        return grps;
    }

    public void setGrps(List<Grp> grps) {
        this.grps = grps;
    }

}
