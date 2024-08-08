package io.github.hefrankeleyn.hefdfs.controller;

import static com.google.common.base.Preconditions.*;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import io.github.hefrankeleyn.hefdfs.beans.HefFileMeta;
import io.github.hefrankeleyn.hefdfs.conf.HefDataConf;
import io.github.hefrankeleyn.hefdfs.server.HefMQSyncer;
import io.github.hefrankeleyn.hefdfs.server.HttpSyncer;
import io.github.hefrankeleyn.hefdfs.utils.HefFileUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;

/**
 * @Date 2024/8/1
 * @Author lifei
 */
@RestController
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    @Resource
    private HefDataConf hefDataConf;

    @Resource
    private HttpSyncer httpSyncer;

    @Resource
    private HefMQSyncer hefMQSyncer;



    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        try {
            // 1. 处理文件
            String dirPath = hefDataConf.findUploadDirPath();
            String fileName = request.getHeader(HttpSyncer.XFILENAME);
            String originalFileName = null;
            boolean needSync = false;
            if (Objects.isNull(fileName) || fileName.isBlank()) {
                needSync = true;
                originalFileName = file.getOriginalFilename();
                fileName = HefFileUtils.getUUIDFileName(originalFileName);
            } else {
                fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
                originalFileName = URLDecoder.decode(request.getHeader(HttpSyncer.XORIGINALFILENAME), StandardCharsets.UTF_8);
            }
            String filePath = HefFileUtils.getFilePath(fileName, dirPath);
            File dest = new File(filePath);
            file.transferTo(dest);
            log.debug("===> success upload file: {}", filePath);
            // 2. 保存meta
            HefFileMeta hefFileMeta = new HefFileMeta(fileName, originalFileName, dest.length());
            hefFileMeta.setDownloadURL(hefDataConf.getDownloadURL());
            if (hefDataConf.getAutoMd5()) {
                try (InputStream inputStream = new FileInputStream(dest)) {
                    hefFileMeta.getTags().put("md5", DigestUtils.md5DigestAsHex(inputStream));
                }
            }
            String metaName = fileName + HefFileUtils.META_SUFFIX;
            String metaFilePath = HefFileUtils.getFilePath(metaName, dirPath);
            HefFileUtils.write(hefFileMeta, new File(metaFilePath));

            // 3. 同步
            if (needSync) {
                // 同步文件到backup
                if (hefDataConf.getAutoBackUp()) {
                    try {
                        String syncFileName = httpSyncer.sync(dest, fileName, originalFileName, hefDataConf.getBackupURL());
                        log.info("===> success sync file: {}", syncFileName);
                    }catch (Exception e) {
                        log.error("===> error sync file: {}", e.getMessage());
                        hefMQSyncer.sync(hefFileMeta);
                    }
                } else {
                    hefMQSyncer.sync(hefFileMeta);
                }
            }
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
        String dirPath = hefDataConf.findUploadDirPath();
        String filePath = HefFileUtils.getFilePath(fileName, dirPath);
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
//        response.setContentType("application/octet-stream");
        response.setContentType(HefFileUtils.getMimeType(fileName));
        // 文件的名称，解决中文乱码
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        // attachment （附件） 提示浏览器下载， inline 提示浏览器显示内容（如果支持的话）
//        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
        response.setHeader("Content-Disposition", "inline; filename=\"" + encodedFileName + "\"");
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
        String dirPath = hefDataConf.findUploadDirPath();
        String filePath = HefFileUtils.getFilePath(fileName, dirPath) + HefFileUtils.META_SUFFIX;
        File file = new File(filePath);
        if (!file.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
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

    @RequestMapping(value = "/meta", method = RequestMethod.GET)
    public String meta(@RequestParam("fileName") String fileName) {
        String dirPath = hefDataConf.findUploadDirPath();
        String filePath = HefFileUtils.getFilePath(fileName, dirPath);
        File file = new File(filePath);
        if (!file.exists()) {
            return Strings.lenientFormat("No found: %s", fileName);
        }
        return HefFileUtils.read(file);
    }


}
