package com.usharik.seznamslovnik.dao.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import static android.arch.persistence.room.ForeignKey.CASCADE;
import static android.arch.persistence.room.ForeignKey.RESTRICT;

@Entity(tableName = "FORMS_OF_VERB",
        indices = {
                @Index(value = "word_id"),
                @Index(value = {"word_id", "form_num", "number"}, unique = true)
        },
        foreignKeys = {
                @ForeignKey(entity = Word.class, parentColumns = "id", childColumns = "word_id",
                        onUpdate = RESTRICT, onDelete = CASCADE)
        })
public class FormsOfVerb {
    @PrimaryKey(autoGenerate = true)
    private Long id;

    @ColumnInfo(name = "word_id")
    private Long wordId;

    @ColumnInfo(name = "form_num")
    private Integer formNum;

    @ColumnInfo(name = "number")
    private String number;

    @ColumnInfo(name = "word")
    private String word;

    public FormsOfVerb(Long wordId, Integer formNum, String number, String word) {
        this.wordId = wordId;
        this.formNum = formNum;
        this.number = number;
        this.word = word;
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

    public void setWordId(Long wordId) {
        this.wordId = wordId;
    }

    public Integer getFormNum() {
        return formNum;
    }

    public void setFormNum(Integer formNum) {
        this.formNum = formNum;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
