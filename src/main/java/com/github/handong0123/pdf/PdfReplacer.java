package com.github.handong0123.pdf;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * pdf替换文字工具类
 * <p>
 * 不足之处：
 * 替换之后的文字无法和原pdf中替换掉的文字信息一致（主要有：字体大小、样式等）
 * 某些情况下（主要是替换字体的大小）替换之后显示不是太整齐
 * 无法匹配目标文字在两页中显示的情况（例如：目标文字：替换工具，第一页页尾有替换两字，第二页页首有工具二字）
 *
 * @author handong
 */
public class PdfReplacer {

    private static final Pattern PATTERN = Pattern.compile("[\\u4e00-\\u9fa5]");

    public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();
        map.put("公众号", "微信号");
        replace("D:\\外快\\小说替换\\test\\会说话的人，都拥有开挂的人生xg\\会说话的人，都拥有开挂的人生xg.pdf", "D:\\外快\\小说替换\\test_new\\会说话的人，都拥有开挂的人生xg\\会说话的人，都拥有开挂的人生xg.pdf", map);
    }

    /**
     * 替换pdf中的字符串
     *
     * @param input      原PDF
     * @param output     新PDF
     * @param replaceMap 替换词Map
     */
    public static void replace(String input, String output, Map<String, String> replaceMap) {
        Map<String, List<MatchItem>> matchResultMap = matchAll(input, new ArrayList<>(replaceMap.keySet()));
        manipulatePdf(input, output, matchResultMap, replaceMap);
    }

    /**
     * 根据关键字和pdf路径，全文搜索关键字
     *
     * @param filePath    pdf目标路径
     * @param keywordList 关键词列表
     * @return
     */
    private static Map<String, List<MatchItem>> matchAll(String filePath, List<String> keywordList) {
        Map<String, List<MatchItem>> matchResultMap = new HashMap<>(keywordList.size());
        PdfReader reader;
        try {
            reader = new PdfReader(filePath);
            int pageSize = reader.getNumberOfPages();
            keywordList.forEach(k -> {
                List<MatchItem> items = new ArrayList<>();
                for (int page = 1; page <= pageSize; page++) {
                    items.addAll(matchPage(reader, page, k));
                }
                matchResultMap.put(k, items);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return matchResultMap;
    }

    /**
     * 根据关键字、文档路径、pdf页数寻找特定的文件内容
     *
     * @param reader
     * @param pageNumber 页数
     * @param keyword    关键字
     * @return
     */
    private static List<MatchItem> matchPage(PdfReader reader, Integer pageNumber, String keyword) {
        try {
            PdfReaderContentParser parse = new PdfReaderContentParser(reader);
            Rectangle rectangle = reader.getPageSize(pageNumber);
            //匹配监听
            KeyWordPositionListener renderListener = new KeyWordPositionListener();
            renderListener.setKeyword(keyword);
            renderListener.setPageNumber(pageNumber);
            renderListener.setCurPageSize(rectangle);
            parse.processContent(pageNumber, renderListener);
            return findKeywordItems(renderListener, keyword);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 找到匹配的关键词块
     *
     * @param renderListener
     * @param keyword
     * @return
     */
    private static List<MatchItem> findKeywordItems(KeyWordPositionListener renderListener, String keyword) {
        //先判断本页中是否存在关键词
        //所有块LIST
        List<MatchItem> allItems = renderListener.getAllItems();
        StringBuffer sbtemp = new StringBuffer();
        //将一页中所有的块内容连接起来组成一个字符串。
        for (MatchItem item : allItems) {
            sbtemp.append(item.getContent());
        }
        List<MatchItem> matches = renderListener.getMatches();
        //一页组成的字符串没有关键词，直接return
        //第一种情况：关键词与块内容完全匹配的项,直接返回
        if (!sbtemp.toString().contains(keyword) || matches.size() > 0) {
            return matches;
        }
        //第二种情况：多个块内容拼成一个关键词，则一个一个来匹配，组装成一个关键词
        sbtemp = new StringBuffer();
        List<MatchItem> tempItems = new ArrayList();
        for (MatchItem item : allItems) {
            if (keyword.contains(item.getContent())) {
                tempItems.add(item);
                sbtemp.append(item.getContent());
                //如果暂存的字符串和关键词 不再匹配时
                if (!keyword.contains(sbtemp.toString())) {
                    sbtemp = new StringBuffer(item.getContent());
                    tempItems.clear();
                    tempItems.add(item);
                }
                //暂存的字符串正好匹配到关键词时
                if (sbtemp.toString().equalsIgnoreCase(keyword)) {
                    //得到匹配的项
                    matches.add(tempItems.get(0));
                    //清空暂存的字符串
                    sbtemp = new StringBuffer();
                    //清空暂存的LIST
                    tempItems.clear();
                }
                //如果找不到则清空
            } else {
                sbtemp = new StringBuffer();
                tempItems.clear();
            }
        }
        return matches;
    }

    /**
     * 替换目标文字，生成新的pdf文件
     *
     * @param src  目标pdf路径
     * @param dest 新pdf的路径
     */
    private static void manipulatePdf(String src, String dest, Map<String, List<MatchItem>> matchItems, Map<String, String> replaceMap) {
        try (OutputStream outputStream = new FileOutputStream(dest)) {
            PdfReader reader = new PdfReader(src);
            PdfStamper stamper = new PdfStamper(reader, outputStream);
            PdfContentByte canvas;
            for (Map.Entry<String, List<MatchItem>> entry : matchItems.entrySet()) {
                Map<Integer, List<MatchItem>> mapItem = new HashMap<>(16);
                for (MatchItem item : entry.getValue()) {
                    Integer pageNum = item.getPageNum();
                    List<MatchItem> matchItemList = mapItem.getOrDefault(pageNum, new ArrayList<>());
                    matchItemList.add(item);
                    mapItem.put(pageNum, matchItemList);
                }
                //遍历每一页去修改
                for (Integer page : mapItem.keySet()) {
                    List<MatchItem> items = mapItem.get(page);
                    //遍历每一页中的匹配项
                    for (MatchItem item : items) {
                        canvas = stamper.getOverContent(page);
                        float x = item.getX();
                        float y = item.getY();
                        float fontWidth = item.getFontWidth();
                        float fontHeight = item.getFontHeight();
                        canvas.saveState();
                        canvas.setColorFill(BaseColor.WHITE);
                        float width = fontWidth * entry.getKey().length();
                        canvas.rectangle(x, y, width, fontHeight * 2);
                        canvas.fill();
                        canvas.restoreState();
                        //开始写入文本
                        canvas.beginText();
                        BaseFont bf = BaseFont.createFont("C:\\Windows\\Fonts\\simfang.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                        Font font = new Font(bf, fontWidth, Font.BOLD);
                        String newKeyword = replaceMap.get(entry.getKey());
                        Matcher m = PATTERN.matcher(newKeyword);
                        float fontSize = m.find() ? width / newKeyword.length() : width / newKeyword.length() * 2f;
                        //设置字体和大小
                        canvas.setFontAndSize(font.getBaseFont(), fontSize);
                        //设置字体的输出位置
                        canvas.setTextMatrix(x, y + fontWidth / 10 + 0.5f);
                        //要输出的text
                        canvas.showText(newKeyword);
                        canvas.endText();
                    }
                }
            }
            stamper.close();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}