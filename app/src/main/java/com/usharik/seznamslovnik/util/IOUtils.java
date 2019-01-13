package com.usharik.seznamslovnik.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;

public final class IOUtils {

    public static void downloadFromUrl(String url, File destFile) throws IOException {
        HttpURLConnection conn = getHttpURLConnection(url);
        int loadedCount;
        int fileLength = conn.getContentLength();
        try (InputStream input = conn.getInputStream();
             OutputStream output = new FileOutputStream(destFile)) {
            loadedCount = copyIoStream(input, output);
        } finally {
            conn.disconnect();
        }
        if (loadedCount != fileLength) {
            throw new IllegalStateException("Incorrect file length.");
        }
    }

    public static void copyFile(String sourcePath,
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

    private static int copyIoStream(InputStream input, OutputStream output) throws IOException {
        byte buffer[] = new byte[16384];
        int loadedCount = 0;
        int count;
        while ((count = input.read(buffer)) != -1) {
            output.write(buffer, 0, count);
            loadedCount += count;
        }
        return loadedCount;
    }

    private static HttpURLConnection getHttpURLConnection(String link) throws IOException {
        URL url = new URL(link);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            conn.disconnect();
            throw new IllegalStateException("Dictionary not found at URL.");
        }
        return conn;
    }
}
