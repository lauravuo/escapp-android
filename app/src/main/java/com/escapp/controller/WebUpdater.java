package com.escapp.controller;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.StringTokenizer;

import com.escapp.model.ContestHeader;
import com.escapp.view.App;

/**
 * Created by laura on 13.1.2015.
 */
public class WebUpdater {
    private static final int BUFFER_SIZE = 4096;

    public static boolean getUpdatedContestData(ContestHeader contestHeader) {
        Context context = App.getContext();
        InputStream input = null;
        FileOutputStream outputStream = null;
        HttpURLConnection connection = null;
        boolean abortDownload = false;
        int writtenCount = 0;
        try {
            Logger.d("Downloading XML file " + contestHeader.getUpdateSrc());
            URL url = new URL(contestHeader.getUpdateSrc());
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Logger.e("HTTP error " + connection.getResponseCode() + " (" +
                        connection.getResponseMessage() + ") when updating data for contest " + contestHeader.getId());
            } else {
                File outputFile = App.getContestUpdateFile(contestHeader.getId());
                try {
                    outputStream = new FileOutputStream(outputFile);
                    input = connection.getInputStream();
                    byte data[] = new byte[BUFFER_SIZE];
                    boolean versionGreater = false;
                    long total = 0;
                    int count = 0;
                    while (!abortDownload && (count = input.read(data)) != -1) {
                        total += count;
                        if (!versionGreater) {
                            String header = new String(data, 0, count);
                            versionGreater = getFileVersion(header) > contestHeader.getVersion();
                            if (!versionGreater) {
                                abortDownload = true;
                            }
                        }
                        outputStream.write(data, 0, count);
                        writtenCount += count;
                        Logger.d("Downloaded bytes " + total  + " for " + contestHeader.getId());
                    }
                } catch (IOException e) {
                    Logger.e("Exception when handling input stream: " + e.toString());
                } finally {
                    if (input != null) {
                        input.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                        if (abortDownload || writtenCount == 0) {
                            writtenCount = 0;
                            outputFile.delete();
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.e("Exception when downloading file: " + e.toString());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return writtenCount != 0;
    }

    private static double getFileVersion(String xmlHeader) {
        double version = 0;
        StringTokenizer tokenizer = new StringTokenizer(xmlHeader);
        String xmlAttribute = "";
        while (tokenizer.hasMoreElements()) {
            xmlAttribute = tokenizer.nextToken();
            if (xmlAttribute.startsWith("version=\"")) {
                String [] elements = xmlAttribute.split("\"");
                if (elements.length > 1) {
                    version = Double.parseDouble(elements[1]);
                }
                break;
            }
        }
        return version;
    }
}
