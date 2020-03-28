package com.usharik.seznamslovnik.model.json.slovnik;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Answer {

    @SerializedName("translate")
    @Expose
    private List<Translate> translate = null;
    @SerializedName("sound")
    @Expose
    private String sound;
    @SerializedName("relations")
    @Expose
    private Relations relations;
    @SerializedName("ftx_samp")
    @Expose
    private List<FtxSamp> ftxSamp = null;
    @SerializedName("other")
    @Expose
    private List<Other> other = null;
    @SerializedName("morf_table")
    @Expose
    private Object morfTable = null;
    @SerializedName("short")
    @Expose
    private List<Object> _short = null;

    public List<Translate> getTranslate() {
        return translate;
    }

    public void setTranslate(List<Translate> translate) {
        this.translate = translate;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public Relations getRelations() {
        return relations;
    }

    public void setRelations(Relations relations) {
        this.relations = relations;
    }

    public List<FtxSamp> getFtxSamp() {
        return ftxSamp;
    }

    public void setFtxSamp(List<FtxSamp> ftxSamp) {
        this.ftxSamp = ftxSamp;
    }

    public List<Other> getOther() {
        return other;
    }

    public void setOther(List<Other> other) {
        this.other = other;
    }

    public Object getMorfTable() {
        return morfTable;
    }

    public void setMorfTable(Object morfTable) {
        this.morfTable = morfTable;
    }

    public List<Object> getShort() {
        return _short;
    }

    public void setShort(List<Object> _short) {
        this._short = _short;
    }

}
