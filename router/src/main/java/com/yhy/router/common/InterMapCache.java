package com.yhy.router.common;

import com.yhy.router.interceptor.TransferInterceptor;

import java.util.HashMap;
import java.util.Map;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-20 13:34
 * version: 1.0.0
 * desc   : 拦截器映射器缓存
 */
public class InterMapCache {

    // 单例对象
    private static volatile InterMapCache instance;

    // 缓存集合
    private final Map<String, Class<? extends TransferInterceptor>> mMap;

    private InterMapCache() {
        if (null != instance) {
            throw new UnsupportedOperationException("Can not be instantiate.");
        }
        mMap = new HashMap<>();
    }

    /**
     * 获取单例对象
     *
     * @return 缓存对象
     */
    public static InterMapCache getInstance() {
        if (null == instance) {
            synchronized (InterMapCache.class) {
                if (null == instance) {
                    instance = new InterMapCache();
                }
            }
        }
        return instance;
    }

    /**
     * 添加所有拦截器映射器
     *
     * @param clazz 所有拦截器映射器
     */
    public void putAll(Map<String, Class<? extends TransferInterceptor>> clazz) {
        mMap.clear();
        mMap.putAll(clazz);
    }

    /**
     * 添加一个拦截器映射器
     *
     * @param name  拦截器名称
     * @param inter 拦截器映射器
     */
    public void put(String name, Class<? extends TransferInterceptor> inter) {
        mMap.put(name, inter);
    }

    /**
     * 获取所有拦截器映射器
     *
     * @return 所有拦截器映射器
     */
    public Map<String, Class<? extends TransferInterceptor>> get() {
        return mMap;
    }

    /**
     * 获取一个拦截器映射器
     *
     * @param name 拦截器名称
     * @return 拦截器映射器
     */
    public Class<? extends TransferInterceptor> get(String name) {
        return mMap.get(name);
    }
}
