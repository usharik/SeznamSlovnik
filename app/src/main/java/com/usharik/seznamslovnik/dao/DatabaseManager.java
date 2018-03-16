package com.usharik.seznamslovnik.dao;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by au185034 on 14/03/2018.
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

    public void backup() throws IOException {
        close();
        File folder = new File(Environment.getExternalStorageDirectory() + BACKUP_FOLDER);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if (!success) {
            throw new RuntimeException("Can't create folder for backup");
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

    private String getDatabasePath() {
        return Environment.getDataDirectory() + "/data/" + context.getPackageName() + "/databases/";
    }

    private void copyFile(String sourcePath,
                          String sourceFileName,
                          String destPath,
                          String detFileName) throws IOException {
        File sourceFile = new File(sourcePath, sourceFileName);
        File destFile = new File(destPath, detFileName);
        FileChannel source = new FileInputStream(sourceFile).getChannel();
        FileChannel destination = new FileOutputStream(destFile).getChannel();
        destination.transferFrom(source, 0, source.size());
        source.close();
        destination.close();
    }
}
