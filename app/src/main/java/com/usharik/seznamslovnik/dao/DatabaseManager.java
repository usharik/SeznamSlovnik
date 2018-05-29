package com.usharik.seznamslovnik.dao;

import android.content.Context;
import android.os.Environment;

import com.usharik.seznamslovnik.BuildConfig;
import com.usharik.seznamslovnik.UrlRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;

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

    private HttpURLConnection getHttpURLConnection(String link) throws IOException {
        URL url = new URL(link);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            conn.disconnect();
            throw new RuntimeException("Dictionary not found at URL.");
        }
        return conn;
    }

    public void restoreFromUrl() throws IOException {
        close();
        HttpURLConnection conn = getHttpURLConnection(getDictionaryUrl());
        String fileName = BACKUP_FOLDER + AppDatabase.DB_NAME;
        String destFileName = Environment.getExternalStorageDirectory() + fileName;
        File destFile = new File(Environment.getExternalStorageDirectory(), fileName);
        if (!createBackupFolderIfNotExists()) {
            throw new RuntimeException("Can't create folder for backup");
        }
        if (destFile.exists()) {
            destFile.delete();
        }
        destFile.createNewFile();
        int loadedCount = 0;
        int fileLength;
        try(InputStream input = conn.getInputStream();
            OutputStream output = new FileOutputStream(destFileName)) {
            fileLength = conn.getContentLength();
            byte buffer[] = new byte[16384];
            int count;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
                loadedCount += count;
            }
        }
        finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        if (loadedCount != fileLength) {
            throw new RuntimeException("Incorrect dictionary file length.");
        }
        restore();
    }

    private String getDatabasePath() {
        return Environment.getDataDirectory() + "/data/" + context.getPackageName() + "/databases/";
    }

    private String getDictionaryUrl() {
        return UrlRepository.APPLICATION_HOME + "/releases/download/" + BuildConfig.VERSION_NAME + "/" + AppDatabase.DB_NAME;
    }

    private void copyFile(String sourcePath,
                          String sourceFileName,
                          String destPath,
                          String detFileName) throws IOException {
        File sourceFile = new File(sourcePath, sourceFileName);
        File destFile = new File(destPath, detFileName);
        if (!destFile.exists()) {
            destFile.createNewFile();
        }
        FileChannel source = new FileInputStream(sourceFile).getChannel();
        FileChannel destination = new FileOutputStream(destFile).getChannel();
        destination.transferFrom(source, 0, source.size());
        source.close();
        destination.close();
    }
}