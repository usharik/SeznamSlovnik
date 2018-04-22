package com.usharik.seznamslovnik.dao;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.database.Cursor;

import com.usharik.seznamslovnik.dao.entity.Translation;
import com.usharik.seznamslovnik.dao.entity.Word;
import com.usharik.seznamslovnik.dao.entity.WordToTranslation;

import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;


@Database(entities = {Word.class, Translation.class, WordToTranslation.class}, version = 4)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    static final String DB_NAME = "slovnik-database";

    public abstract TranslationStorageDao translationStorageDao();

    static AppDatabase getAppDatabase(Context context) {
        return Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DB_NAME)
                .addMigrations(MIGRATION_2_3)
                .addMigrations(MIGRATION_3_4)
                .build();
    }

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("alter table WORD add column load_date INTEGER");
            database.execSQL("update WORD set load_date = ?", new Object[]{Calendar.getInstance().getTime().getTime()});
            database.execSQL("alter table WORD add column word_for_search TEXT");

            Cursor cursor = database.query("select id, word from WORD where word_for_search is null");
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String word = StringUtils.stripAccents(cursor.getString(1));
                database.execSQL("update WORD set word_for_search = ? where id = ?", new Object[]{word, id});
            }
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("create table WORD_TO_TRANSLATION (" +
                    "   id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "   word_id INTEGER," +
                    "   translation_id INTEGER," +
                    "   FOREIGN KEY(word_id) REFERENCES WORD(id) ON UPDATE RESTRICT ON DELETE CASCADE," +
                    "   FOREIGN KEY(translation_id) REFERENCES TRANSLATION(id) ON UPDATE RESTRICT ON DELETE CASCADE)");
            database.execSQL("CREATE UNIQUE INDEX index_word_id_translation_id ON WORD_TO_TRANSLATION (word_id, translation_id)");
            database.execSQL("CREATE INDEX index_translation_id ON WORD_TO_TRANSLATION (translation_id)");
            database.execSQL("insert into WORD_TO_TRANSLATION(word_id, translation_id)" +
                    "select w.wordId, w.id " +
                    "  from TRANSLATION w");

            database.execSQL("ALTER TABLE TRANSLATION RENAME TO _TRANSLATION_OLD");
            database.execSQL("CREATE TABLE TRANSLATION (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "`translation` TEXT, " +
                    "`lang` TEXT)");
            database.execSQL("insert into TRANSLATION(id, translation, lang) select id, translation, lang from _TRANSLATION_OLD");
            database.execSQL("drop table _TRANSLATION_OLD");
            database.execSQL("CREATE UNIQUE INDEX `index_TRANSLATION_translation_lang` ON `TRANSLATION` (`translation`, `lang`)");
            database.execSQL("CREATE INDEX `index_TRANSLATION_lang` ON `TRANSLATION` (`lang`)");
        }
    };
}
