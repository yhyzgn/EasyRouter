package com.yhy.erouter.expt;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-18 13:46
 * version: 1.0.0
 * desc   : 非法操作异常
 */
public class IllegalOperationException extends RuntimeException {

    /**
     * 构造函数
     *
     * @param msg 信息
     */
    public IllegalOperationException(String msg) {
        super(msg);
    }
}
