package com.github.handong0123.pdf;

import com.itextpdf.awt.geom.Rectangle2D;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.RenderListener;
import com.itextpdf.text.pdf.parser.TextRenderInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 匹配pdf关键词监听器
 *
 * @author handong0123
 */
public class KeyWordPositionListener implements RenderListener {

    /**
     * 存放匹配上的字符信息
     */
    private List<MatchItem> matches = new ArrayList<>();
    /**
     * 存放所有的字符信息
     */
    private List<MatchItem> allItems = new ArrayList<>();

    private Rectangle curPageSize;

    /**
     * 匹配的关键字
     */
    private String keyword;
    /**
     * 匹配的当前页
     */
    private Integer pageNumber;

    @Override
    public void beginTextBlock() {
        //do nothing
    }

    @Override
    public void renderText(TextRenderInfo renderInfo) {
        //获取字符
        String content = renderInfo.getText();
        Rectangle2D.Float textRectangle = renderInfo.getDescentLine().getBoundingRectange();

        MatchItem item = new MatchItem();
        item.setContent(content);
        item.setPageNum(pageNumber);
        //默认12
        item.setFontHeight(textRectangle.height == 0 ? 12:textRectangle.height);
        item.setFontWidth(textRectangle.width);
        item.setPageHeight(curPageSize.getHeight());
        item.setPageWidth(curPageSize.getWidth());
        item.setX((float)textRectangle.getX());
        item.setY((float)textRectangle.getY());

        //若keyword是单个字符，匹配上的情况
        if(content.equalsIgnoreCase(keyword)) {
            matches.add(item);
        }
        //保存所有的项
        allItems.add(item);
    }

    @Override
    public void endTextBlock() {
        //do nothing
    }

    @Override
    public void renderImage(ImageRenderInfo renderInfo) {
        //do nothing
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public List<MatchItem> getMatches() {
        return matches;
    }

    void setCurPageSize(Rectangle rect) {
        this.curPageSize = rect;
    }

    public List<MatchItem> getAllItems() {
        return allItems;
    }

    public void setAllItems(List<MatchItem> allItems) {
        this.allItems = allItems;
    }

}