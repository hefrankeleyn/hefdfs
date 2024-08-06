package io.github.hefrankeleyn.hefdfs.utils;

import com.google.gson.Gson;
import io.github.hefrankeleyn.hefdfs.beans.HefFileMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.UUID;

/**
 * @Date 2024/8/6
 * @Author lifei
 */
public class HefFileUtils {

    public static final String META_SUFFIX = ".meta";

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

    public static void write(HefFileMeta meta, File file) {
        try {
            Files.writeString(Paths.get(file.getAbsolutePath()), new Gson().toJson(meta), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String read(File file) {
        try {
            return Files.readString(Paths.get(file.getAbsolutePath()));
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
