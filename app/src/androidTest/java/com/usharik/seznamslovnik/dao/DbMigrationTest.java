package com.usharik.seznamslovnik.dao;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory;
import androidx.room.testing.MigrationTestHelper;
import android.database.Cursor;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(AndroidJUnit4.class)
public class DbMigrationTest {

    private static final String TEST_DB = "migration-test";

    @Rule
    public MigrationTestHelper helper;

    public DbMigrationTest() {
        helper = new MigrationTestHelper(InstrumentationRegistry.getInstrumentation(),
                AppDatabase.class.getCanonicalName(),
                new FrameworkSQLiteOpenHelperFactory());
    }

    @Test
    public void migrate3To4() throws IOException {
        final int rowCount = 100;
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 3);

        db.execSQL(buildWordInsertSql(rowCount));

        db.execSQL("insert into `TRANSLATION`(`wordId`, `translation`, `lang`)" +
                "select id, word || '_translation', 'en' from WORD");

        db.close();

        db = helper.runMigrationsAndValidate(TEST_DB, 4, true, AppDatabase.MIGRATION_3_4);
        db.setForeignKeyConstraintsEnabled(true);

        checkDataMigration(db, rowCount);
        checkForeignKeys(db);
    }

    private String buildWordInsertSql(int count) {
        StringBuilder query = new StringBuilder();
        query.append("insert into `WORD`(`word`, `word_for_search`, `lang`, `load_date`) values");
        for (int i=0; i<count-1; i++) {
            query.append(String.format("('word%d', 'word%d', 'cz', '123'),", i, i));
        }
        query.append(String.format("('word%d', 'word%d', 'cz', '123')", count, count));
        return query.toString();
    }

    private void checkDataMigration(SupportSQLiteDatabase db, int expectedRowCount) {
        Cursor cursor = db.query("select * " +
                "from WORD_TO_TRANSLATION a " +
                "inner join WORD b on a.word_id = b.id " +
                "inner join TRANSLATION c on a.translation_id = c.id");
        Assert.assertEquals(expectedRowCount, cursor.getCount());
        cursor.close();
    }

    private void checkForeignKeys(SupportSQLiteDatabase db) {
        Cursor cursor = db.query("select * from WORD where word = 'word1'");
        Assert.assertTrue(cursor.moveToNext());
        long wordId = cursor.getLong(0);
        cursor.close();

        db.execSQL("delete from WORD where id = " + wordId);
        cursor = db.query("select * from WORD_TO_TRANSLATION where word_id = " + wordId);
        Assert.assertEquals(0, cursor.getCount());

        cursor = db.query("select * from TRANSLATION where translation = 'word2_translation'");
        Assert.assertTrue(cursor.moveToNext());
        long translationId = cursor.getLong(0);
        cursor.close();

        db.execSQL("delete from TRANSLATION where id = " + translationId);
        cursor = db.query("select * from WORD_TO_TRANSLATION where translation_id = " + translationId);
        Assert.assertEquals(0, cursor.getCount());
    }

    @Test
    public void migrate4To5() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 4);

        db.close();

        db = helper.runMigrationsAndValidate(TEST_DB, 5, true, AppDatabase.MIGRATION_4_5);
    }

    @Test
    public void migrate5To6() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 5);

        db.close();

        db = helper.runMigrationsAndValidate(TEST_DB, 6, true, AppDatabase.MIGRATION_5_6);
    }

    @Test
    public void migrate6To7() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 6);

        db.close();

        db = helper.runMigrationsAndValidate(TEST_DB, 7, true, AppDatabase.MIGRATION_6_7);
    }

    @Test
    public void migrate7To8() throws IOException {
        SupportSQLiteDatabase db = helper.createDatabase(TEST_DB, 7);

        db.close();

        db = helper.runMigrationsAndValidate(TEST_DB, 8, true, AppDatabase.MIGRATION_7_8);
    }
}
