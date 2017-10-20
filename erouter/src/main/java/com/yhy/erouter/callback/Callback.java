package com.yhy.erouter.callback;

import com.yhy.erouter.common.EPoster;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-20 10:19
 * version: 1.0.0
 * desc   : 路由回调
 */
public interface Callback {

    /**
     * 转发成功
     *
     * @param poster 当前转发器
     */
    void onPosted(EPoster poster);

    /**
     * 发生错误
     *
     * @param poster 当前转发器
     * @param e      异常信息
     */
    void onError(EPoster poster, Throwable e);
}
