package com.usharik.seznamslovnik.dao.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;
import static androidx.room.ForeignKey.RESTRICT;

@Entity(tableName = "WORD_TO_TRANSLATION",
        foreignKeys = {
                @ForeignKey(entity = Word.class, parentColumns = "id", childColumns = "word_id",
                        onUpdate = RESTRICT, onDelete = CASCADE),
                @ForeignKey(entity = Translation.class, parentColumns = "id", childColumns = "translation_id",
                        onUpdate = RESTRICT, onDelete = CASCADE)
        },
        indices = {
                @Index(value = {"word_id", "translation_id"}, unique = true),
                @Index(value = {"translation_id"})
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
