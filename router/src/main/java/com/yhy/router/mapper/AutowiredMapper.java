package com.yhy.router.mapper;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-19 10:48
 * version: 1.0.0
 * desc   : 自动注入解析器
 */
public interface AutowiredMapper {

    /**
     * 注入操作
     *
     * @param target 当前需要自动注入的类
     */
    void inject(Object target);
}
