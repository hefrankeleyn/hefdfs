package io.github.hefrankeleyn.hefdfs.conf;

import com.google.common.base.MoreObjects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * @Date 2024/8/6
 * @Author lifei
 */
@Configuration
public class HefDataConf {

    @Value("${hefdfs.path}")
    private String uploadPath;

    @Value("${server.port}")
    private String port;

    @Value("${hefdfs.backup-url}")
    private String backupURL;

    public String findUploadDirPath() {
        return getUploadPath() + File.separator + getPort();
    }

    public String getUploadPath() {
        return uploadPath;
    }

    public void setUploadPath(String uploadPath) {
        this.uploadPath = uploadPath;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getBackupURL() {
        return backupURL;
    }

    public void setBackupURL(String backupURL) {
        this.backupURL = backupURL;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(HefDataConf.class)
                .add("uploadPath", uploadPath)
                .add("port", port)
                .add("backupURL", backupURL)
                .toString();
    }
}