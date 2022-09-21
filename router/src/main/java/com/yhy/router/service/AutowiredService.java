package com.yhy.router.service;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-19 10:33
 * version: 1.0.0
 * desc   : 自动注入服务接口
 */
public interface AutowiredService {

    /**
     * 自动注入
     *
     * @param instance 当前需要自动注入的类
     */
    void autowired(Object instance);
}
