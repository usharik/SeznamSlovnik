package com.usharik.seznamslovnik.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

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
            "inner join WORD as B on A.wordId = B.id " +
            "where B.word = :word " +
            "  and B.lang = :langFrom " +
            "  and A.lang = :langTo " +
            "order by A.translation " +
            "limit :limit")
    public abstract Maybe<List<String>> getTranslations(String word, String langFrom, String langTo, int limit);

    @Transaction
    public void insertTranslationsForWord(String request, String langFrom, List<String> translations, String langTo) {
        Long id = getWordId(request, langFrom);
        if (id == null) {
            id = insertWord(new Word(request, StringUtils.stripAccents(request), langFrom));
        } else {
            updateWordLoadDate(id, Calendar.getInstance().getTime());
        }
        Translation trn[] = new Translation[translations.size()];
        for (int i = 0; i < trn.length; i++) {
            trn[i] = new Translation(id, translations.get(i), langTo);
        }
        insertAllTranslations(trn);
    }

    @Query("update WORD " +
            "  set load_date = :loadDate " +
            "where id = :id")
    public abstract long updateWordLoadDate(long id, Date loadDate);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long insertWord(Word word);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertAllTranslations(Translation... translations);
}
