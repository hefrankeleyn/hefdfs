package io.github.hefrankeleyn.hefdfs.controller;

import static com.google.common.base.Preconditions.*;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 下载
     *
     * @param fileName
     * @param response
     */
    @RequestMapping(value = "/download")
    public void download(@RequestParam("fileName") String fileName, HttpServletResponse response) {
        String dirPath = uploadPath + File.separator + port;
        String filePath = dirPath + File.separator + fileName;
        File file = new File(filePath);
        if (!(file.exists() && file.isFile())) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            log.debug("====> file not exists: {}", filePath);
            return;
        }
        // 需要添加一些头信息，响应才知道是下载的文件
        // 添加字符集
        response.setCharacterEncoding("UTF-8");
        // 文件类型： 方式一：指定具体的文件类型；方式二：指定其是一个二进制格式
        // 其它的文件类型：text/plain、application/pdf、application/vnd.ms-excel、image/jpeg、image/png
        response.setContentType("application/octet-stream");
        // 文件的名称，解决中文乱码
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        // attachment （附件） 提示浏览器下载， inline 提示浏览器显示内容（如果支持的话）
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
        // 文件的长度
        response.setContentLength((int) file.length());

        try (InputStream inputStream = new FileInputStream(file);
             BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
            byte[] bytes = new byte[1024 * 16];
            int len = 0;
            ServletOutputStream outputStream = response.getOutputStream();
            while ((len = bufferedInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
            }
            outputStream.flush();
        } catch (Exception e) {
            log.error(e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/downloadBytes")
    @ResponseBody
    public byte[] downloadBytes(@RequestParam("fileName") String fileName, HttpServletResponse response) {
        String dirPath = uploadPath + File.separator + port;
        String filePath = dirPath + File.separator + fileName;
        File file = new File(filePath);
        if (!file.exists() && file.isFile()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        checkState(file.exists() && file.isFile(), "file not exists: %s", filePath);
        // 需要添加一些头信息，响应才知道是下载的文件
        // 添加字符集
        response.setCharacterEncoding("UTF-8");
        // 文件类型： 方式一：指定具体的文件类型；方式二：指定其是一个二进制格式
        // 其它的文件类型：text/plain、application/pdf、application/vnd.ms-excel、image/jpeg、image/png
        response.setContentType("application/octet-stream");
        // 文件的名称，解决中文乱码
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        // attachment （附件） 提示浏览器下载， inline 提示浏览器显示内容（如果支持的话）
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");

        try (InputStream inputStream = new FileInputStream(file)) {
            byte[] bytes = new byte[(int) file.length()];
            int readLen = inputStream.read(bytes);
            log.debug("====> readLen {}", readLen);
            return bytes;
        } catch (Exception e) {
            log.error(e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return null;
        }
    }


}
