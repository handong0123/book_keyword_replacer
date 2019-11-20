package com.github.handong0123;

import java.util.Map;

/**
 * 内容替换器
 *
 * @author handong0123
 */
public interface IReplacer {

    /**
     * 替换
     *
     * @param src        原文件路径
     * @param dest       目标文件路径
     * @param replaceMap 替换词典
     * @return 替换成功
     */
    static boolean replace(String src, String dest, Map<String, String> replaceMap) {
        return false;
    }
}
