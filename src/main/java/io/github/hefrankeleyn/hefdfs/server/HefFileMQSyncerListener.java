package io.github.hefrankeleyn.hefdfs.server;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import io.github.hefrankeleyn.hefdfs.beans.HefFileMeta;
import io.github.hefrankeleyn.hefdfs.conf.HefDataConf;
import io.github.hefrankeleyn.hefdfs.utils.HefFileUtils;
import jakarta.annotation.Resource;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Objects;

/**
 * @Date 2024/8/8
 * @Author lifei
 */
@Service
@RocketMQMessageListener(topic = "${hefdfs.topic}", consumerGroup = "${hefdfs.consumer-group}")
public class HefFileMQSyncerListener implements RocketMQListener<MessageExt> {

    private static final Logger log = LoggerFactory.getLogger(HefFileMQSyncerListener.class);

    @Resource
    private HefDataConf hefDataConf;

    @Override
    public void onMessage(MessageExt messageExt) {
        log.debug("====> onMessage ID: {}", messageExt.getMsgId());
        String json = new String(messageExt.getBody());
        HefFileMeta hefFileMeta = new Gson().fromJson(json, HefFileMeta.class);
        String downloadURL = hefFileMeta.getDownloadURL();
        if (Objects.isNull(downloadURL) || downloadURL.isBlank()) {
            return;
        }
        // 去除本机操作
        if (downloadURL.equals(hefDataConf.getDownloadURL())) {
            log.debug("=====> same file server, ignore sync task");
            return;
        }
        log.debug("====> process sync task.");

        String filePath = HefFileUtils.getFilePath(hefFileMeta.getName(), hefDataConf.getBaseDirPath());
        // 写meta文件
        String metaFilePath = filePath + HefFileUtils.META_SUFFIX;
        File metaFile = new File(metaFilePath);
        if (metaFile.exists()) {
            log.debug("===> meta file exists: {}", metaFilePath);
        } else {
            HefFileUtils.write(json, metaFile);
            log.debug("===> meta file sync success: {}", metaFilePath);
        }
        // 下载文件
        File file = new File(filePath);
        if (file.exists() && file.length() == hefFileMeta.getSize()) {
            log.debug("===> file exists: {}", filePath);
        } else {
            String url = Strings.lenientFormat("%s?fileName=%s", hefFileMeta.getDownloadURL(), hefFileMeta.getName());
            HefFileUtils.downloadFile(url, file);
            log.debug("==> file sync success: {}", filePath);
        }
    }
}