package com.yhy.erouter.utils;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-17 14:14
 * version: 1.0.0
 * desc   : 路由工具类
 */
public class EUtils {

    private EUtils() {
        throw new UnsupportedOperationException("Can not be instantiate.");
    }

    /**
     * 从路径中获取分组名称
     *
     * @param url 路径
     * @return 分组名称
     */
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

    /**
     * 将字符串首字符大写
     *
     * @param str 原始字符串
     * @return 更改后的字符串
     */
    public static String upCaseFirst(String str) {
        if (!isEmpty(str) || Character.isUpperCase(str.charAt(0))) {
            return (new StringBuilder()).append(Character.toUpperCase(str.charAt(0))).append(str.substring(1)).toString();
        }
        return str;
    }

    /**
     * 判断字符串是否为空
     *
     * @param str 字符串
     * @return 是否为空
     */
    public static boolean isEmpty(String str) {
        return null == str || "".equals(str);
    }
}
