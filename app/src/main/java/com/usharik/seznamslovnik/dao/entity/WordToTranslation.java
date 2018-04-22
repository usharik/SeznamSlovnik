package com.usharik.seznamslovnik.dao.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "WORD_TO_TRANSLATION",
        foreignKeys = {
                @ForeignKey(entity = Word.class, parentColumns = "id", childColumns = "word_id"),
                @ForeignKey(entity = Translation.class, parentColumns = "id", childColumns = "translation_id")
        },
        indices = {
                @Index(value = {"word_id", "translation_id"}, unique = true)
        })
public class WordToTranslation {

    @PrimaryKey(autoGenerate = true)
    private Long id;

    @ColumnInfo(name = "word_id")
    private Long wordId;

    @ColumnInfo(name = "translation_id")
    private Long translationId;

    public WordToTranslation(Long wordId, Long translationId) {
        this.wordId = wordId;
        this.translationId = translationId;
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

    public Long getTranslationId() {
        return translationId;
    }

    public void setTranslationId(Long translationId) {
        this.translationId = translationId;
    }
}
