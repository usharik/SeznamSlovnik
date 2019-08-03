package com.usharik.seznamslovnik.model.json.slovnik;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Grp {

    @SerializedName("sens")
    @Expose
    private List<Sen> sens = null;

    public List<Sen> getSens() {
        return sens;
    }

    public void setSens(List<Sen> sens) {
        this.sens = sens;
    }

}
