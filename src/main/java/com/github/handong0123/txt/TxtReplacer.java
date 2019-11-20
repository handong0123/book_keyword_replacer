package com.github.handong0123.txt;

import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * txt文件内容替换器
 *
 * @author handong0123
 */
public class TxtReplacer {

    public static boolean replace(String src, String dest, Map<String, String> replaceMap) {
        try (FileWriter fileWriter = new FileWriter(dest)) {
            // 判断文件编码
            String encoding = EncodeUtils.getEncode(src);
            List<String> lineList;
            if ("GBK".equals(encoding)) {
                lineList = Files.readAllLines(Paths.get(src), Charset.forName("GBK"));
            } else {
                lineList = Files.readAllLines(Paths.get(src));
            }
            for (String l : lineList) {
                String line = l;
                for (Map.Entry<String, String> entry : replaceMap.entrySet()) {
                    line = line.replace(entry.getKey(), entry.getValue());
                }
                fileWriter.write(line + "\n");
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
