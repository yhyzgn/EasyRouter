package com.yhy.erouter.interceptor;

import com.yhy.erouter.common.EPoster;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-20 10:17
 * version: 1.0.0
 * desc   : 所有拦截器都需要实现的接口
 */
public interface EInterceptor {

    /**
     * 执行操作
     *
     * @param poster 当前转发器
     * @return 是否中断路由
     * <p>
     * true  ::  中断路由
     * false ::  往下执行
     */
    boolean execute(EPoster poster);
}
