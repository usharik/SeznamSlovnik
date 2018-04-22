package com.usharik.seznamslovnik.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import com.usharik.seznamslovnik.dao.entity.Translation;
import com.usharik.seznamslovnik.dao.entity.Word;
import com.usharik.seznamslovnik.dao.entity.WordToTranslation;

import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.reactivex.Maybe;

@Dao
public abstract class TranslationStorageDao {

    @Query("select * from WORD")
    public abstract List<Word> getAllWords();

    @Query("select * from WORD where lang = :lang")
    public abstract List<Word> getAllWordsForLang(String lang);

    @Query("select * from WORD where word = :word and lang = :lang")
    public abstract Maybe<Word> getWord(String word, String lang);

    @Query("select id from WORD where word = :word and lang = :lang")
    public abstract Long getWordId(String word, String lang);

    @Query("select id from TRANSLATION where translation = :translation and lang = :lang")
    public abstract Long getTranslationId(String translation, String lang);

    @Query("select word " +
            " from WORD " +
            "where lang = :lang " +
            "  and (word_for_search like :template1 || '%')" +
            "order by " +
            " case when word like :template2 || '%' then 1" +
            "      else 2 end, " +
            " word " +
            "limit :limit")
    public abstract Maybe<List<String>> getSuggestions(String template1, String template2, String lang, int limit);

    @Query("select * from TRANSLATION")
    public abstract List<Translation> getAllTranslation();

    @Query("select A.translation " +
            " from TRANSLATION as A " +
            "inner join WORD_TO_TRANSLATION as B on A.id = B.translation_id " +
            "inner join WORD as C on B.word_id = C.id " +
            "where C.word = :word " +
            "  and C.lang = :langFrom " +
            "  and A.lang = :langTo " +
            "order by A.translation " +
            "limit :limit")
    public abstract Maybe<List<String>> getTranslations(String word, String langFrom, String langTo, int limit);

    @Transaction
    public void insertTranslationsForWord(String request, String langFrom, List<String> translations, String langTo) {
        Long wordId = getWordId(request, langFrom);
        if (wordId == null) {
            wordId = insertWord(new Word(request, StringUtils.stripAccents(request), langFrom));
        } else {
            updateWordLoadDate(wordId, Calendar.getInstance().getTime());
        }
        for (String translation : translations) {
            Long translationId = getTranslationId(translation, langTo);
            if (translationId == null) {
                translationId = insertTranslation(new Translation(wordId, translation, langTo));
            }
            insertWordToTranslation(new WordToTranslation(wordId, translationId));
        }
    }

    @Query("update WORD " +
            "  set load_date = :loadDate " +
            "where id = :id")
    public abstract long updateWordLoadDate(long id, Date loadDate);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long insertWord(Word word);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long insertTranslation(Translation translations);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertAllTranslations(Translation... translations);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insertWordToTranslation(WordToTranslation wordToTranslation);
}
