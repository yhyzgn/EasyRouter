package com.yhy.router.mapper;

import com.yhy.router.common.RouterMeta;

import java.util.Map;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-17 16:57
 * version: 1.0.0
 * desc   : 路由映射器接口
 */
public interface RouterGroupMapper {

    /**
     * 加载路由映射
     *
     * @param metaMap 用来接收路由映射的集合
     */
    void load(Map<String, RouterMeta> metaMap);
}
