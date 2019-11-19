package com.github.handong0123.word;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;

import java.io.*;
import java.util.Map;

/**
 * @author handong
 */
public class DocReplacer {

    public static boolean replace(String input, String output, Map<String, String> replaceMap) {
        try (InputStream in = new FileInputStream(new File(input));
             OutputStream outputStream = new FileOutputStream(output)) {
            HWPFDocument document = new HWPFDocument(in);
            // 读取文本内容
            Range bodyRange = document.getRange();
            // 替换内容
            for (Map.Entry<String, String> entry : replaceMap.entrySet()) {
                bodyRange.replaceText(entry.getKey(), entry.getValue());
            }
            //导出到文件
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            document.write(byteArrayOutputStream);
            outputStream.write(byteArrayOutputStream.toByteArray());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
