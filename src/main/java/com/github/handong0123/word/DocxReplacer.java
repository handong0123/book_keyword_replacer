package com.github.handong0123.word;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTFonts;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;

/**
 * @author handong
 */
public class DocxReplacer {
    /**
     * 用一个docx文档作为模板，程序替换其中的内容，再写入目标文档中。
     *
     * @param input
     * @param output
     * @param replaceMap
     */
    public static void replace(String input, String output, Map<String, String> replaceMap) {
        try (InputStream is = new FileInputStream(input);
             OutputStream os = new FileOutputStream(output)) {
            XWPFDocument doc = new XWPFDocument(is);
            // 替换段落里面的变量
            replaceInPara(doc, replaceMap);
            doc.write(os);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 替换段落里面的变量
     *
     * @param doc        要替换的文档
     * @param replaceMap 参数
     */
    private static void replaceInPara(XWPFDocument doc, Map<String, String> replaceMap) {
        Iterator<XWPFParagraph> iterator = doc.getParagraphsIterator();
        XWPFParagraph para;
        while (iterator.hasNext()) {
            para = iterator.next();
            replaceInPara(para, replaceMap);
        }
    }

    /**
     * 替换段落里面的变量
     *
     * @param para       要替换的段落
     * @param replaceMap 参数
     */
    private static void replaceInPara(XWPFParagraph para, Map<String, String> replaceMap) {
        List<XWPFRun> runs;
        for (Map.Entry<String, String> entry : replaceMap.entrySet()) {
            if (!para.getParagraphText().contains(entry.getKey())) {
                continue;
            }
            runs = para.getRuns();
            for (int i = 0; i < runs.size(); i++) {
                XWPFRun run = runs.get(i);
                String runText = run.toString().replace(entry.getKey(), entry.getValue());
                int fontSize = run.getFontSize();
                String fontFamily = run.getFontFamily();
                CTR ctr = run.getCTR();
                para.removeRun(i);
                XWPFRun newRun = para.insertNewRun(i);
                newRun.setText(runText);
                try {
                    // 复制格式
                    newRun.setBold(run.isBold());
                    newRun.setItalic( run.isItalic());
                    newRun.setUnderline(run.getUnderline());
                    newRun.setColor(run.getColor());
                    newRun.setTextPosition(run.getTextPosition());
                    if (fontSize != -1) {
                        newRun.setFontSize(fontSize);
                        CTRPr rpr = newRun.getCTR().isSetRPr() ? newRun.getCTR().getRPr() : newRun.getCTR().addNewRPr();
                        CTFonts fonts = rpr.isSetRFonts() ? rpr.getRFonts() : rpr.addNewRFonts();
                        fonts.setAscii(fontFamily);
                        fonts.setEastAsia(fontFamily);
                        fonts.setHAnsi(fontFamily);
                    }
                    if (fontFamily != null) {
                        newRun.setFontFamily(fontFamily);
                    }
                    if (ctr != null) {
                        boolean flat = false;
                        try {
                            flat = ctr.isSetRPr();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (flat) {
                            CTRPr tmpRPr = ctr.getRPr();
                            if (tmpRPr.isSetRFonts()) {
                                CTFonts tmpFonts = tmpRPr.getRFonts();
                                CTRPr cellRPr = newRun.getCTR().isSetRPr() ? newRun
                                        .getCTR().getRPr() : newRun
                                        .getCTR().addNewRPr();
                                CTFonts cellFonts = cellRPr.isSetRFonts() ? cellRPr
                                        .getRFonts() : cellRPr
                                        .addNewRFonts();
                                cellFonts.setAscii(tmpFonts.getAscii());
                                cellFonts.setAsciiTheme(tmpFonts
                                        .getAsciiTheme());
                                cellFonts.setCs(tmpFonts.getCs());
                                cellFonts.setCstheme(tmpFonts.getCstheme());
                                cellFonts.setEastAsia(tmpFonts
                                        .getEastAsia());
                                cellFonts.setEastAsiaTheme(tmpFonts
                                        .getEastAsiaTheme());
                                cellFonts.setHAnsi(tmpFonts.getHAnsi());
                                cellFonts.setHAnsiTheme(tmpFonts
                                        .getHAnsiTheme());
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}