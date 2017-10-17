package com.yhy.erouter.common;

import com.yhy.erouter.common.RouterMeta;

import java.util.Map;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-17 16:57
 * version: 1.0.0
 * desc   :
 */
public interface RouterGroup {

    void load(Map<String, RouterMeta> metaMap);
}
