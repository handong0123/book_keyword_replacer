package com.github.handong0123.epub;

import com.github.handong0123.IReplacer;
import net.sf.jazzlib.ZipEntry;
import net.sf.jazzlib.ZipException;
import net.sf.jazzlib.ZipInputStream;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.epub.EpubWriter;
import nl.siegmann.epublib.epub.ResourcesLoader;

import java.io.*;
import java.util.Map;

/**
 * epub文件内容替换器
 *
 * @author handong123
 */
public class EpubReplacer implements IReplacer {

    public static boolean replace(String src, String dest, Map<String, String> replaceMap) {
        Book book = readBook(src);
        // 考虑到本身可能存在的文件损坏，所以这里返回true
        if (null == book) {
            return true;
        }
        modifyBook(book, replaceMap);
        return writeBook(book, dest);
    }


    /**
     * 读取epub文件
     *
     * @return book
     */
    private static Book readBook(String src) {
        EpubReader epubReader = new EpubReader();
        Book book = null;
        try (InputStream inputStr = new FileInputStream(src);
             ZipInputStream zip = new ZipInputStream(inputStr)) {
            // 用getNextEntry方法判断epub文件是否损坏，否则会造成死循环
            try {
                zip.getNextEntry();
            } catch (Exception e) {
                zip.closeEntry();
                e.printStackTrace();
                return null;
            }
            book = epubReader.readEpub(zip);
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
            if (t.getMediaType() == null || t.getMediaType().getName() == null || t.getMediaType().getName().contains("image")) {
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
     * 输出epub
     *
     * @param book book
     * @param dest 目标文件
     */
    private static boolean writeBook(Book book, String dest) {
        EpubWriter epubWriter = new EpubWriter();
        try (OutputStream output = new FileOutputStream(dest)) {
            epubWriter.write(book, output);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}