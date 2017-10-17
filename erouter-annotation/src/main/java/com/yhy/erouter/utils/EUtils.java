package com.yhy.erouter.utils;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-17 14:14
 * version: 1.0.0
 * desc   : 通用工具类
 */
public class EUtils {

    private EUtils() {
        throw new UnsupportedOperationException("Can not be instantiate.");
    }

    public static String getGroupFromUrl(String url) {
        if (isEmpty(url) || !url.startsWith("/")) {
            return "";
        }
        // 先去除第一个“/”
        url = url.replaceFirst("/", "");

        // 截取路径中的第一个词组作为分组名称
        int index = url.indexOf("/");
        if (index > -1) {
            url = url.substring(0, index);
        }
        return url;
    }

    public static String upCaseFirst(String str) {
        if (!isEmpty(str) || Character.isUpperCase(str.charAt(0))) {
            return (new StringBuilder()).append(Character.toUpperCase(str.charAt(0))).append(str.substring(1)).toString();
        }
        return str;
    }

    public static boolean isEmpty(String str) {
        return null == str || "".equals(str);
    }
}
