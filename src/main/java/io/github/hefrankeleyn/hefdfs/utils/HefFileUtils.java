package io.github.hefrankeleyn.hefdfs.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.Objects;
import java.util.UUID;

/**
 * @Date 2024/8/6
 * @Author lifei
 */
public class HefFileUtils {

    private static final Logger log = LoggerFactory.getLogger(HefFileUtils.class);

    private static final String DEFAULT_MIME_TYPE = "application/octet-stream";

    public static String getMimeType(String fileName) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentType = fileNameMap.getContentTypeFor(fileName);
        if (Objects.isNull(contentType) || contentType.isEmpty()) {
            contentType = DEFAULT_MIME_TYPE;
        }
        return contentType;
    }

    public static void createDirPath(String dirPath) {
        File dirFile = new File(dirPath);
        if (!dirFile.exists()) {
            boolean ok = dirFile.mkdirs();
            log.debug("===> {} create dir {}", ok, dirPath);
        }
    }

    public static String getUUIDFileName(String originalFileName) {
        return UUID.randomUUID() + HefFileUtils.getExt(originalFileName);
    }

    public static String getSubDirByUUIDFileName(String uuidFileName) {
        return uuidFileName.substring(0,2);
    }

    public static String getExt(String fileName) {
        if (Objects.isNull(fileName) || fileName.isBlank() || fileName.lastIndexOf(".")==-1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }


    public static String getFilePath(String uuidFileName, String dirPath) {
        String subDir = getSubDirByUUIDFileName(uuidFileName);
        return dirPath + File.separator+subDir+File.separator + uuidFileName;
    }

}
