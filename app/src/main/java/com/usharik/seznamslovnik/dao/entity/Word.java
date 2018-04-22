package com.usharik.seznamslovnik.dao.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.util.Calendar;
import java.util.Date;

@Entity(tableName = "WORD",
        indices =   {
                        @Index(value = "word"),
                        @Index(value = "lang"),
                        @Index(value = {"word", "lang"}, unique = true),
                        @Index(value = {"word_for_search", "lang"})
                    })
public class Word {

    public static final Word NULL_WORD = new Word(null, null, null);

    @PrimaryKey(autoGenerate = true)
    private Long id;

    @ColumnInfo(name = "word")
    private String word;

    @ColumnInfo(name = "word_for_search")
    private String wordForSearch;

    @ColumnInfo(name = "lang")
    private String lang;

    @ColumnInfo(name = "load_date")
    private Date loadDate;

    public Word(String word, String wordForSearch, String lang) {
        this.word = word;
        this.wordForSearch = wordForSearch;
        this.lang = lang;
        this.loadDate = Calendar.getInstance().getTime();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getWordForSearch() {
        return wordForSearch;
    }

    public void setWordForSearch(String wordForSearch) {
        this.wordForSearch = wordForSearch;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public Date getLoadDate() {
        return loadDate;
    }

    public void setLoadDate(Date loadDate) {
        this.loadDate = loadDate;
    }
}
