package io.github.hefrankeleyn.hefdfs.server;

import jakarta.annotation.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.beans.Encoder;
import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @Date 2024/8/3
 * @Author lifei
 */
@Component
public class HttpSyncer {

    public static final String XFILENAME = "X-fileName";
    public static final String XORIGINALFILENAME = "X-original-fileName";


    @Resource
    private RestTemplate restTemplate;

    public String sync(File file, String fileName, String originalFileName, String url) {
        HttpHeaders httpHeaders = new HttpHeaders();
        // 模拟表格
        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        // 添加标记，用于区分，是否是同步调用的
        String encodeFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        String encodeOriginalFileName = URLEncoder.encode(originalFileName, StandardCharsets.UTF_8);
        httpHeaders.add(XFILENAME, encodeFileName);
        httpHeaders.add(XORIGINALFILENAME, encodeOriginalFileName);
        // 创建请求体
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        // 用资源类包装一下，这样它就知道用流的方式去读
        builder.part("file", new FileSystemResource(file));

        HttpEntity<MultiValueMap<String, HttpEntity<?>>> request = new HttpEntity<>(builder.build(), httpHeaders);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        return response.getBody();
    }
}
