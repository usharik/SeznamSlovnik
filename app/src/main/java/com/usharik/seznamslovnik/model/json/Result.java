package com.usharik.seznamslovnik.model.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


public class Result {

    @SerializedName("suggest")
    @Expose
    public List<Suggest> suggest = null;

}
