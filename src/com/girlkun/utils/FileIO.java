package com.girlkun.utils;

import java.io.FileInputStream;
import java.io.IOException;

public class FileIO {
    public static byte[] readFile(String url) {
        try (FileInputStream fis = new FileInputStream(url)) {
            byte[] ab = new byte[fis.available()];
            int bytesRead = fis.read(ab);
            if (bytesRead != ab.length) {
                throw new IOException("Incomplete file read");
            }
            return ab;
        } catch (IOException e) {
            Logger.logException(FileIO.class, e, "Error: " + url);
        }
        return null;
    }
}
