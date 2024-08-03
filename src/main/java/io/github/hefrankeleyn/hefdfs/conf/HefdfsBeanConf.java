package io.github.hefrankeleyn.hefdfs.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @Date 2024/8/3
 * @Author lifei
 */
@Configuration
public class HefdfsBeanConf {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
