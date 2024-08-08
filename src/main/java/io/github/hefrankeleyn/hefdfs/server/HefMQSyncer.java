package io.github.hefrankeleyn.hefdfs.server;

import com.google.gson.Gson;
import io.github.hefrankeleyn.hefdfs.beans.HefFileMeta;
import io.github.hefrankeleyn.hefdfs.conf.HefDataConf;
import jakarta.annotation.Resource;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * @Date 2024/8/7
 * @Author lifei
 */
@Component
public class HefMQSyncer {

    private static final Logger log = LoggerFactory.getLogger(HefMQSyncer.class);

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Resource
    private HefDataConf hefDataConf;

    public void sync(HefFileMeta hefFileMeta) {
        Message<String> message = MessageBuilder.withPayload(new Gson().toJson(hefFileMeta)).build();
        rocketMQTemplate.send(hefDataConf.getTopic(), message);
        log.debug("====> success send message: {}" , message);
    }
}