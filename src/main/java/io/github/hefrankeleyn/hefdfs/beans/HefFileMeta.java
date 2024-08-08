package io.github.hefrankeleyn.hefdfs.beans;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Maps;
import io.github.hefrankeleyn.hefdfs.utils.HefFileUtils;

import java.util.Map;

/**
 * @Date 2024/8/6
 * @Author lifei
 */
public class HefFileMeta {
    private String name;
    private String originFileName;
    private Long size;
    private String downloadURL;
    private Map<String, String> tags = Maps.newHashMap();

    public HefFileMeta() {
    }

    public HefFileMeta(String name, String originFileName, Long size) {
        this.name = name;
        this.originFileName = originFileName;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOriginFileName() {
        return originFileName;
    }

    public void setOriginFileName(String originFileName) {
        this.originFileName = originFileName;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }

    public String getDownloadURL() {
        return downloadURL;
    }

    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(HefFileUtils.class)
                .add("name", name)
                .add("originFileName", originFileName)
                .add("size", size)
                .add("tags", tags)
                .add("downloadURL", downloadURL)
                .toString();
    }
}
