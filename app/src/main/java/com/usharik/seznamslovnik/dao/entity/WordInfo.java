package com.usharik.seznamslovnik.dao.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import static android.arch.persistence.room.ForeignKey.CASCADE;
import static android.arch.persistence.room.ForeignKey.RESTRICT;

@Entity(tableName = "WORD_INFO",
        foreignKeys = {
                @ForeignKey(entity = Word.class, parentColumns = "id", childColumns = "word_id",
                        onUpdate = RESTRICT, onDelete = CASCADE),
        },
        indices =   {
                @Index(value = "word_id")
        })
public class WordInfo {
    @PrimaryKey(autoGenerate = true)
    private Long id;

    @ColumnInfo(name = "word_id")
    private Long wordId;

    @ColumnInfo(name = "info")
    private String info;

    public WordInfo(Long wordId, String info) {
        this.wordId = wordId;
        this.info = info;
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

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
