package com.usharik.seznamslovnik.model.json.slovnik;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Relations {

    @SerializedName("Synonyma")
    @Expose
    private List<String> synonyma = null;
    @SerializedName("P\u0159edpony")
    @Expose
    private List<String> predpony = null;
    @SerializedName("Odvozen\u00e1 slova")
    @Expose
    private List<String> odvozenaSlova = null;
    @SerializedName("dict")
    @Expose
    private String dict;
    @SerializedName("direction")
    @Expose
    private String direction;

    public List<String> getSynonyma() {
        return synonyma;
    }

    public void setSynonyma(List<String> synonyma) {
        this.synonyma = synonyma;
    }

    public List<String> getPredpony() {
        return predpony;
    }

    public void setPredpony(List<String> predpony) {
        this.predpony = predpony;
    }

    public List<String> getOdvozenaSlova() {
        return odvozenaSlova;
    }

    public void setOdvozenaSlova(List<String> odvozenaSlova) {
        this.odvozenaSlova = odvozenaSlova;
    }

    public String getDict() {
        return dict;
    }

    public void setDict(String dict) {
        this.dict = dict;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

}
