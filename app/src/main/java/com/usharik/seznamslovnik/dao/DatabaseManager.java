package com.usharik.seznamslovnik.dao;

import android.content.Context;
import android.os.Environment;

import com.usharik.seznamslovnik.BuildConfig;
import com.usharik.seznamslovnik.UrlRepository;

import java.io.File;
import java.io.IOException;

import static com.usharik.seznamslovnik.util.IOUtils.copyFile;
import static com.usharik.seznamslovnik.util.IOUtils.downloadFromUrl;

/**
 * Created by macbook on 14/03/2018.
 */

public class DatabaseManager {

    private static final String BACKUP_FOLDER = "/Seznam-Slovnik/";

    private Context context;
    private AppDatabase instance;

    public DatabaseManager(Context context) {
        this.context = context;
    }

    public synchronized AppDatabase getActiveDbInstance() {
        if (instance == null || !instance.isOpen()) {
            instance = AppDatabase.getAppDatabase(context);
        }
        return instance;
    }

    public void close() {
        if (instance == null) {
            return;
        }
        instance.close();
    }

    private boolean createBackupFolderIfNotExists() {
        File folder = new File(Environment.getExternalStorageDirectory() + BACKUP_FOLDER);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        return success;
    }

    public void backup() throws IOException {
        close();
        if (!createBackupFolderIfNotExists()) {
            throw new IllegalStateException("Can't create folder for backup");
        }
        String sourcePath = getDatabasePath();
        String destPath = Environment.getExternalStorageDirectory() + BACKUP_FOLDER;
        copyFile(sourcePath, AppDatabase.DB_NAME, destPath, AppDatabase.DB_NAME);
    }

    public void restore() throws IOException {
        close();
        String sourcePath = Environment.getExternalStorageDirectory() + BACKUP_FOLDER;
        String destPath = getDatabasePath();
        copyFile(sourcePath, AppDatabase.DB_NAME, destPath, AppDatabase.DB_NAME);
    }

    public void restoreFromUrl() throws IOException {
        close();
        String fileName = BACKUP_FOLDER + AppDatabase.DB_NAME;
        File destFile = new File(Environment.getExternalStorageDirectory(), fileName);
        if (!createBackupFolderIfNotExists()) {
            throw new IllegalStateException("Can't create folder for backup");
        }
        if (destFile.exists()) {
            destFile.delete();
        }
        destFile.createNewFile();
        downloadFromUrl(getDictionaryUrl(), destFile);
        restore();
    }

    private String getDatabasePath() {
        return Environment.getDataDirectory() + "/data/" + context.getPackageName() + "/databases/";
    }

    private String getDictionaryUrl() {
        return UrlRepository.APPLICATION_HOME + "/releases/download/" + BuildConfig.VERSION_NAME + "/" + AppDatabase.DB_NAME;
    }
}