package io.github.hefrankeleyn.hefdfs.utils;


import org.junit.Test;

/**
 * @Date 2024/8/6
 * @Author lifei
 */
public class FileUtilsTest {

    @Test
    public void test01() {
        String fileName = "2024-03-12-RPC框架-consumer.pdf";
        String mimeType = HefFileUtils.getMimeType(fileName);
        System.out.println(mimeType);
    }
}
