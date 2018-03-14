package com.usharik.seznamslovnik.dao;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.database.Cursor;

import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;


@Database(entities = {Word.class, Translation.class}, version = 3)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    static final String DB_NAME = "slovnik-database";

    public abstract TranslationStorageDao translationStorageDao();

    static AppDatabase getAppDatabase(Context context) {
        return Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DB_NAME)
                .addMigrations(MIGRATION_2_3)
                .build();
    }

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("alter table WORD add column load_date INTEGER");
            database.execSQL("update WORD set load_date = ?", new Object[] {Calendar.getInstance().getTime().getTime()});
            database.execSQL("alter table WORD add column word_for_search TEXT");

            Cursor cursor = database.query("select id, word from WORD where word_for_search is null");
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String word = StringUtils.stripAccents(cursor.getString(1));
                database.execSQL("update WORD set word_for_search = ? where id =?", new Object[] {word, id});
            }
        }
    };
}
