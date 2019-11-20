package com.github.handong0123.word;

import com.github.handong0123.IReplacer;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Range;

import java.io.*;
import java.util.Map;

/**
 * doc文件内容替换器
 *
 * @author handong0123
 */
public class DocReplacer implements IReplacer {

    public static boolean replace(String src, String dest, Map<String, String> replaceMap) {
        try (InputStream in = new FileInputStream(new File(src));
             OutputStream outputStream = new FileOutputStream(dest)) {
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
