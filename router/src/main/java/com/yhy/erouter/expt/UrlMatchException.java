package com.yhy.erouter.expt;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-18 14:31
 * version: 1.0.0
 * desc   : Url匹配异常
 */
public class UrlMatchException extends RuntimeException {

    /**
     * 构造函数
     *
     * @param msg 信息
     */
    public UrlMatchException(String msg) {
        super(msg);
    }
}
