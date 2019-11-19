package com.github.handong0123.epub;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.epub.EpubWriter;

import java.io.*;
import java.util.Map;

/**
 * epub格式文件读和写示例程序
 *
 * @author handong
 */
public class EpubReplacer {

    /**
     * 替换EPUB文件内容
     *
     * @param input      原文件
     * @param output     新文件
     * @param replaceMap 替换词Map
     */
    public static boolean replace(String input, String output, Map<String, String> replaceMap) {
        Book book = readBook(input);
        if(null == book){
            return false;
        }
        modifyBook(book, replaceMap);
        return writeBook(book, output);
    }


    /**
     * 读epub文件
     *
     * @return book
     */
    private static Book readBook(String epubPath) {
        EpubReader epubReader = new EpubReader();
        Book book = null;
        try (InputStream inputStr = new FileInputStream(epubPath)) {
            book = epubReader.readEpub(inputStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return book;
    }

    /**
     * 修改电子书
     *
     * @param book       book
     * @param replaceMap 替换词Map
     */
    private static void modifyBook(Book book, Map<String, String> replaceMap) {
        for (Resource t : book.getResources().getAll()) {
            if (t.getMediaType().getName().contains("image")) {
                continue;
            }
            String oldHtml;
            try {
                oldHtml = new String(t.getData());
                final String[] newHtml = {oldHtml};
                replaceMap.forEach((k, v) -> newHtml[0] = newHtml[0].replace(k, v));
                t.setData(newHtml[0].getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 输出电子书
     *
     * @param book     book
     * @param fileName 新文件名
     */
    private static boolean writeBook(Book book, String fileName) {
        EpubWriter epubWriter = new EpubWriter();
        try (OutputStream output = new FileOutputStream(fileName)) {
            epubWriter.write(book, output);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}