package com.usharik.seznamslovnik.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


public class Answer {

    @SerializedName("result")
    @Expose
    public List<Result> result = null;
}