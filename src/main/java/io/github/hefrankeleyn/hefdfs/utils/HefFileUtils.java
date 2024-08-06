package io.github.hefrankeleyn.hefdfs.utils;

import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.Objects;

/**
 * @Date 2024/8/6
 * @Author lifei
 */
public class HefFileUtils {

    private static final String DEFAULT_MIME_TYPE = "application/octet-stream";

    public static String getMimeType(String fileName) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentType = fileNameMap.getContentTypeFor(fileName);
        if (Objects.isNull(contentType) || contentType.isEmpty()) {
            contentType = DEFAULT_MIME_TYPE;
        }
        return contentType;
    }

}
