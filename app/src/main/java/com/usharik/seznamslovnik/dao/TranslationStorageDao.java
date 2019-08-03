package com.usharik.seznamslovnik.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.usharik.seznamslovnik.dao.entity.CasesOfNoun;
import com.usharik.seznamslovnik.dao.entity.FormsOfVerb;
import com.usharik.seznamslovnik.dao.entity.Translation;
import com.usharik.seznamslovnik.dao.entity.Word;
import com.usharik.seznamslovnik.dao.entity.WordInfo;
import com.usharik.seznamslovnik.dao.entity.WordToTranslation;

import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.Single;

@Dao
public abstract class TranslationStorageDao {

    @Query("select count(*) from WORD")
    public abstract Single<Long> getWordCount();

    @Query("select * from WORD")
    public abstract List<Word> getAllWords();

    @Query("select * from WORD where lang = :lang")
    public abstract List<Word> getAllWordsForLang(String lang);

    @Query("select * from WORD where word = :word and lang = :lang")
    public abstract Maybe<Word> getWord(String word, String lang);

    @Query("select info " +
            " from WORD_INFO " +
            "where info like 'rod:%' " +
            "  and word_id = :wordId " +
            "order by word_id " +
            "limit 1")
    public abstract String getWordGender(long wordId);

    @Query("select id from WORD where word = :word and lang = :lang")
    public abstract Long getWordId(String word, String lang);

    @Query("select id from TRANSLATION where translation = :translation and lang = :lang")
    public abstract Long getTranslationId(String translation, String lang);

    @Query("select distinct A.word " +
            " from WORD as A " +
            "inner join WORD_TO_TRANSLATION as B on A.id = B.word_id " +
            "inner join TRANSLATION as C on B.translation_id = C.id " +
            "where A.lang = :langFrom " +
            "  and C.lang = :langTo " +
            "  and (A.word_for_search like :template1 || '%')" +
            "order by " +
            " case when A.word_for_search = :template1 or A.word = :template2 then 1" +
            "      when A.word like :template2 || '%' then 2" +
            "      else 3 end, " +
            " A.word " +
            "limit :limit")
    public abstract Maybe<List<String>> getSuggestions(String template1, String template2, String langFrom, String langTo, int limit);

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

    @Query("select A.word " +
            " from CASES_OF_NOUN as A " +
            "inner join WORD as B on A.word_id = B.id " +
            "where B.word = :word " +
            "  and A.case_num = :caseNum " +
            "  and A.number = :number")
    public abstract String getCaseOfNoun(String word, Integer caseNum, String number);

    @Query("select A.* " +
            " from CASES_OF_NOUN as A " +
            "inner join WORD as B on A.word_id = B.id " +
            "where B.word = :word " +
            "  and A.number = :number")
    public abstract List<CasesOfNoun> getCasesOfNoun(String word, String number);

    @Query("select A.* " +
            " from FORMS_OF_VERB as A " +
            "inner join WORD as B on A.word_id = B.id " +
            "where B.word = :word " +
            "  and A.number = :number")
    public abstract List<FormsOfVerb> getFormsOfVerb(String word, String number);

    @Query("select A.info " +
            " from WORD_INFO as A " +
            "inner join WORD as B on A.word_id = B.id " +
            "where B.word = :word ")
    public abstract List<String> getWordInfo(String word);

    @Query("select 1 " +
            " from WORD as A" +
            " where A.id = :wordId" +
            "   and A.json is not null " +
            "   and A.json != ''")
    public abstract int isJsonExists(long wordId);

    @Transaction
    public long insertTranslationsForWord(String request, String langFrom, List<String> translations, String langTo, String json) {
        Long wordId = getWordId(request, langFrom);
        if (wordId == null) {
            wordId = insertWord(new Word(request, StringUtils.stripAccents(request), langFrom, json));
        } else {
            if (isJsonExists(wordId) != 1) {
                updateWordJson(wordId, json);
            }
            updateWordLoadDate(wordId, Calendar.getInstance().getTime());
        }
        for (String translation : translations) {
            Long translationId = getTranslationId(translation, langTo);
            if (translationId == null) {
                translationId = insertTranslation(new Translation(translation, langTo));
            }
            insertWordToTranslation(new WordToTranslation(wordId, translationId));
        }
        return wordId;
    }

    @Query("update WORD " +
            "  set load_date = :loadDate " +
            "where id = :id")
    public abstract void updateWordLoadDate(long id, Date loadDate);

    @Query("update WORD " +
            "  set json = :json " +
            "where id = :id")
    public abstract void updateWordJson(long id, String json);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract long insertWord(Word word);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long insertTranslation(Translation translations);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertAllTranslations(Translation... translations);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract void insertWordToTranslation(WordToTranslation wordToTranslation);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertCaseOfNoun(CasesOfNoun casesOfNoun);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertCasesOfNoun(CasesOfNoun... casesOfNoun);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertFormsOfVerb(FormsOfVerb... formsOfVerbs);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertWordInfos(WordInfo... wordInfos);
}
