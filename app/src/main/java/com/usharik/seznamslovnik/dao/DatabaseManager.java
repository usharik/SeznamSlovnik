package com.usharik.seznamslovnik.dao;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created by au185034 on 14/03/2018.
 */

public class DatabaseManager {

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

    public void backup() {
        close();
        String sourcePath = Environment.getDataDirectory() + "/data/" + context.getPackageName() + "/databases/";
        String destPath = Environment.getExternalStorageDirectory() + "/Download/";
        copyFile(sourcePath, AppDatabase.DB_NAME, destPath, AppDatabase.DB_NAME);
    }

    public void restore() {
        close();
        String sourcePath = Environment.getExternalStorageDirectory() + "/Download/";
        String destPath = Environment.getDataDirectory() + "/data/" + context.getPackageName() + "/databases/";
        copyFile(sourcePath, AppDatabase.DB_NAME, destPath, AppDatabase.DB_NAME);
    }

    private void copyFile(String sourcePath,
                         String sourceFileName,
                         String destPath,
                         String detFileName){
        File sourceFile = new File(sourcePath, sourceFileName);
        File destFile = new File(destPath, detFileName);
        try {
            FileChannel source = new FileInputStream(sourceFile).getChannel();
            FileChannel destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
        } catch(IOException e) {
            Log.e(getClass().getName(), e.getLocalizedMessage());
        }
    }
}
