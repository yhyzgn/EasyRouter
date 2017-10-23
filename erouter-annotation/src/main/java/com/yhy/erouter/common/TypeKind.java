package com.yhy.erouter.common;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-19 14:04
 * version: 1.0.0
 * desc   : Java类型枚举
 */
public enum TypeKind {
    // 一些基础类型
    BOOLEAN,
    BYTE,
    SHORT,
    INT,
    LONG,
    CHAR,
    FLOAT,
    DOUBLE,

    // 其他包装类型
    STRING,
    SERIALIZABLE,
    PARCELABLE,
    OBJECT;
}
