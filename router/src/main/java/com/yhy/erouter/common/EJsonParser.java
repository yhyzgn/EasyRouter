package com.yhy.erouter.common;

import java.lang.reflect.Type;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-19 16:50
 * version: 1.0.0
 * desc   : Json解析器
 */
public interface EJsonParser {

    /**
     * 将Json字符串转换为T对象
     *
     * @param json Json字符串
     * @param type 泛型
     * @param <T>  对象类型
     * @return 转换后的具体对象
     */
    <T> T fromJson(String json, Type type);

    /**
     * 将T对象转换为Json字符串
     *
     * @param obj 具体T对象
     * @param <T> 对象类型
     * @return 转换后的Json字符串
     */
    <T> String toJson(T obj);
}
