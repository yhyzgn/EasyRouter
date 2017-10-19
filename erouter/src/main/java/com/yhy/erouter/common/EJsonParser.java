package com.yhy.erouter.common;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-19 16:50
 * version: 1.0.0
 * desc   :
 */
public interface EJsonParser {

    <T> T fromJson(String json, Class<T> clazz);

    <T> String toJson(T obj);
}
