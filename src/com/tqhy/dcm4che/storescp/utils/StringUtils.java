package com.tqhy.dcm4che.storescp.utils;

/**
 * 处理字符串工具类
 *
 * @author Yiheng <guoyiheng89@gmail.com>
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @create 2018/5/8
 * @since 1.0.0
 */
public class StringUtils {

    public static String[] EMPTY_STRING = {};

    public static boolean isNotEmpty(String str) {
        return str != null && !str.isEmpty();
    }

    public static boolean equals(String str1, String str2) {
        if (null == str1 || null == str2) {
            return false;
        }
        return str1.equals(str2);
    }

    public static String[] split(String s, char delim) {
        if (s == null || s.isEmpty())
            return EMPTY_STRING;

        int count = 1;
        int delimPos = -1;
        while ((delimPos = s.indexOf(delim, delimPos + 1)) >= 0)
            count++;

        if (count == 1) {
            return new String[]{s};
        }

        String[] ss = new String[count];
        int delimPos2 = s.length();
        while (--count >= 0) {
            delimPos = s.lastIndexOf(delim, delimPos2 - 1);
            ss[count] = s.substring(delimPos + 1, delimPos2);
            delimPos2 = delimPos;
        }
        return ss;
    }
}
