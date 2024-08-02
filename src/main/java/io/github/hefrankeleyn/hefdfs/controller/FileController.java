package io.github.hefrankeleyn.hefdfs.controller;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @Date 2024/8/1
 * @Author lifei
 */
@RestController
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    @Value("${hefdfs.path}")
    private String uploadPath;

    @Value("${server.port}")
    private String port;

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String upload(@RequestParam("file") MultipartFile file) {
        try {
            String dirPath = uploadPath + File.separator + port;
            File dirFile = new File(dirPath);
            if (!dirFile.exists()) {
                boolean ok = dirFile.mkdirs();
                log.debug("===> {} create dir {}", ok, dirPath);
            }
            String fileName = file.getOriginalFilename();
            String filePath = dirPath + File.separator + fileName;
            File dest = new File(filePath);
            file.transferTo(dest);
            log.debug("===> success upload file: {}", filePath);
            return dest.getName();
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
