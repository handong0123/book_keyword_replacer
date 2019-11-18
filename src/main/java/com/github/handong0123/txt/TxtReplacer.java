package com.github.handong0123.txt;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author handong
 */
public class TxtReplacer {

    public static void replace(String input, String output, Map<String, String> replaceMap) {
        try (FileWriter fileWriter = new FileWriter(output)) {
            List<String> lineList = Files.readAllLines(Paths.get(input));
            lineList.forEach(l -> {
                String line = l;
                for (Map.Entry<String, String> entry : replaceMap.entrySet()) {
                    line = line.replace(entry.getKey(), entry.getValue());
                }
                try {
                    fileWriter.write(line + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
