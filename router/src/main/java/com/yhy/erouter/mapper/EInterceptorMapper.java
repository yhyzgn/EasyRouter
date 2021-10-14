package com.yhy.erouter.mapper;

import com.yhy.erouter.interceptor.EInterceptor;

import java.util.Map;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-20 10:33
 * version: 1.0.0
 * desc   : 拦截器映射器接口
 */
public interface EInterceptorMapper {

    /**
     * 加载拦截器映射关系
     *
     * @param interMap 用来接收拦截器映射关系的集合
     */
    void load(Map<String, Class<? extends EInterceptor>> interMap);
}
