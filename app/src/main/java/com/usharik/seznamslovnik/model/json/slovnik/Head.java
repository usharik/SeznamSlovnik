package com.usharik.seznamslovnik.model.json.slovnik;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Head {

    @SerializedName("entr")
    @Expose
    private String entr;
    @SerializedName("pron")
    @Expose
    private String pron;
    @SerializedName("form")
    @Expose
    private String form;
    @SerializedName("morf")
    @Expose
    private String morf;
    @SerializedName("phrs")
    @Expose
    private String phrs;
    @SerializedName("vari")
    @Expose
    private String vari;
    @SerializedName("vfem")
    @Expose
    private String vfem;
    @SerializedName("hyph")
    @Expose
    private String hyph;
    @SerializedName("prag")
    @Expose
    private String prag;
    @SerializedName("cntx")
    @Expose
    private String cntx;
    @SerializedName("dict")
    @Expose
    private String dict;
    @SerializedName("morf_id")
    @Expose
    private String morfId;

    public String getEntr() {
        return entr;
    }

    public void setEntr(String entr) {
        this.entr = entr;
    }

    public String getPron() {
        return pron;
    }

    public void setPron(String pron) {
        this.pron = pron;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getMorf() {
        return morf;
    }

    public void setMorf(String morf) {
        this.morf = morf;
    }

    public String getPhrs() {
        return phrs;
    }

    public void setPhrs(String phrs) {
        this.phrs = phrs;
    }

    public String getVari() {
        return vari;
    }

    public void setVari(String vari) {
        this.vari = vari;
    }

    public String getVfem() {
        return vfem;
    }

    public void setVfem(String vfem) {
        this.vfem = vfem;
    }

    public String getHyph() {
        return hyph;
    }

    public void setHyph(String hyph) {
        this.hyph = hyph;
    }

    public String getPrag() {
        return prag;
    }

    public void setPrag(String prag) {
        this.prag = prag;
    }

    public String getCntx() {
        return cntx;
    }

    public void setCntx(String cntx) {
        this.cntx = cntx;
    }

    public String getDict() {
        return dict;
    }

    public void setDict(String dict) {
        this.dict = dict;
    }

    public String getMorfId() {
        return morfId;
    }

    public void setMorfId(String morfId) {
        this.morfId = morfId;
    }

}
