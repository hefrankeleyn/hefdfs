package io.github.hefrankeleyn.hefdfs;

import io.github.hefrankeleyn.hefdfs.conf.HefDataConf;
import io.github.hefrankeleyn.hefdfs.utils.HefFileUtils;
import jakarta.annotation.Resource;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.File;

@SpringBootApplication
public class HefdfsApplication {

    @Resource
    private HefDataConf hefDataConf;

    public static void main(String[] args) {
        SpringApplication.run(HefdfsApplication.class, args);
    }


    @Bean
    public ApplicationRunner runner() {
        return args->{
            // 创建跟文件夹
            String dirPath = hefDataConf.getUploadPath() + File.separator + hefDataConf.getPort();
            HefFileUtils.createDirPath(dirPath);
            // 创建256个字符
            // 创建子文件夹：一个字节可以用两个16进制表示
            // 一个字节有8个bit组成，意味着一个字节可以表示256种不同的值
            // 两个16进制为刚好可以表示256个数
            for (int i=0; i<256; i++) {
                String subDir = String.format("%02x", i);
                String subPath = dirPath + File.separator + subDir;
                HefFileUtils.createDirPath(subPath);
            }
        };
    }
}
