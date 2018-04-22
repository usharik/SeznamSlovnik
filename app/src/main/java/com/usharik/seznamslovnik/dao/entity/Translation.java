package com.usharik.seznamslovnik.dao.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "TRANSLATION",
        foreignKeys = @ForeignKey(entity = Word.class, parentColumns = "id", childColumns = "wordId"),
        indices =   {
                        @Index(value = "lang"),
                        @Index(value = "wordId"),
                        @Index(value = {"translation", "lang"}, unique = true)
                    })
public class Translation {
    @PrimaryKey(autoGenerate = true)
    private Long id;

    @ColumnInfo(name = "wordId")
    private Long wordId;

    @ColumnInfo(name = "translation")
    private String translation;

    @ColumnInfo(name = "lang")
    private String lang;

    public Translation(long wordId, String translation, String lang) {
        this.wordId = wordId;
        this.translation = translation;
        this.lang = lang;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWordId() {
        return wordId;
    }

    public void setWordId(long wordId) {
        this.wordId = wordId;
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
