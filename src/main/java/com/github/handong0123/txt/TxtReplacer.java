package com.github.handong0123.txt;

import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * @author handong
 */
public class TxtReplacer {

    public static boolean replace(String input, String output, Map<String, String> replaceMap) {
        try (FileWriter fileWriter = new FileWriter(output)) {
            String encoding = EncodeUtils.getEncode(input);
            List<String> lineList;
            if ("GBK".equals(encoding)) {
                lineList = Files.readAllLines(Paths.get(input), Charset.forName("GBK"));
            } else {
                lineList = Files.readAllLines(Paths.get(input));
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
