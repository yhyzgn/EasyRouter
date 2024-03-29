package com.yhy.router.common;

import com.yhy.router.EasyRouter;
import com.yhy.router.utils.LogUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-18 8:21
 * version: 1.0.0
 * desc   : 路由的分组缓存
 */
public class GroupMapCache {
    private final String TAG = getClass().getSimpleName();

    // 单例对象
    private static volatile GroupMapCache instance;

    // 缓存集合
    private final Map<String, Map<String, RouterMeta>> mMap;

    private GroupMapCache() {
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
    public static GroupMapCache getInstance() {
        if (null == instance) {
            synchronized (GroupMapCache.class) {
                if (null == instance) {
                    instance = new GroupMapCache();
                }
            }
        }
        return instance;
    }

    /**
     * 添加一个分组的路由集合
     *
     * @param group 分组名称
     * @param value 路由集合
     */
    public void put(String group, Map<String, RouterMeta> value) {
        mMap.put(group, value);
        if (EasyRouter.getInstance().isDebugEnable()) {
            LogUtils.i(TAG, "Cache router map of group '" + group + "'.");
        }
    }

    /**
     * 按分组名称获取路由集合
     *
     * @param group 分组名称
     * @return 对应的路由集合
     */
    public Map<String, RouterMeta> get(String group) {
        if (EasyRouter.getInstance().isDebugEnable()) {
            LogUtils.i(TAG, "Get router map of '" + group + "'.");
        }
        return mMap.get(group);
    }
}
