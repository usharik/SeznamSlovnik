package com.usharik.seznamslovnik.dao.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "TRANSLATION",
        indices =   {
                        @Index(value = "lang"),
                        @Index(value = {"translation", "lang"}, unique = true)
                    })
public class Translation {
    @PrimaryKey(autoGenerate = true)
    private Long id;

    @ColumnInfo(name = "translation")
    private String translation;

    @ColumnInfo(name = "lang")
    private String lang;

    public Translation(String translation, String lang) {
        this.translation = translation;
        this.lang = lang;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
}
