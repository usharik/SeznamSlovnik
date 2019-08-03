package com.usharik.seznamslovnik.model.json.slovnik;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Sen {

    @SerializedName("morf")
    @Expose
    private String morf;
    @SerializedName("numb")
    @Expose
    private String numb;
    @SerializedName("phrs")
    @Expose
    private String phrs;
    @SerializedName("styl")
    @Expose
    private String styl;
    @SerializedName("form")
    @Expose
    private String form;
    @SerializedName("trans")
    @Expose
    private List<List<String>> trans = null;
    @SerializedName("coll2")
    @Expose
    private List<Coll2> coll2 = null;
    @SerializedName("samp2")
    @Expose
    private List<Samp2> samp2 = null;
    @SerializedName("note2")
    @Expose
    private String note2;
    @SerializedName("desc2")
    @Expose
    private String desc2;
    @SerializedName("link2")
    @Expose
    private List<Object> link2 = null;

    public String getMorf() {
        return morf;
    }

    public void setMorf(String morf) {
        this.morf = morf;
    }

    public String getNumb() {
        return numb;
    }

    public void setNumb(String numb) {
        this.numb = numb;
    }

    public String getPhrs() {
        return phrs;
    }

    public void setPhrs(String phrs) {
        this.phrs = phrs;
    }

    public String getStyl() {
        return styl;
    }

    public void setStyl(String styl) {
        this.styl = styl;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public List<List<String>> getTrans() {
        return trans;
    }

    public void setTrans(List<List<String>> trans) {
        this.trans = trans;
    }

    public List<Coll2> getColl2() {
        return coll2;
    }

    public void setColl2(List<Coll2> coll2) {
        this.coll2 = coll2;
    }

    public List<Samp2> getSamp2() {
        return samp2;
    }

    public void setSamp2(List<Samp2> samp2) {
        this.samp2 = samp2;
    }

    public String getNote2() {
        return note2;
    }

    public void setNote2(String note2) {
        this.note2 = note2;
    }

    public String getDesc2() {
        return desc2;
    }

    public void setDesc2(String desc2) {
        this.desc2 = desc2;
    }

    public List<Object> getLink2() {
        return link2;
    }

    public void setLink2(List<Object> link2) {
        this.link2 = link2;
    }

}
