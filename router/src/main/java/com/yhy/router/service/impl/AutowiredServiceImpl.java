package com.yhy.router.service.impl;

import android.util.LruCache;

import com.yhy.router.EasyRouter;
import com.yhy.router.common.Constant;
import com.yhy.router.mapper.AutowiredMapper;
import com.yhy.router.service.AutowiredService;
import com.yhy.router.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-19 10:42
 * version: 1.0.0
 * desc   : 自动注入服务实现
 */
public class AutowiredServiceImpl implements AutowiredService {
    private final String TAG = getClass().getSimpleName();
    // 缓存
    private final LruCache<String, AutowiredMapper> mClassCache;
    // 解析器黑名单，保存找不到自动注入解析器的类，也就是当前类没有自动注入字段
    private final List<String> mBlackList;

    public AutowiredServiceImpl() {
        mClassCache = new LruCache<>(1024);
        mBlackList = new ArrayList<>();
    }

    @Override
    public void autowired(Object target) {
        String className = target.getClass().getName();
        try {
            if (!mBlackList.contains(className)) {
                // 先从缓存中获取解析器
                AutowiredMapper mapper = mClassCache.get(className);
                if (null == mapper) {
                    // 通过反射获取对应解析器
                    mapper = (AutowiredMapper) Class.forName(className + Constant.SUFFIX_AUTOWIRED).newInstance();
                }
                mapper.inject(target);
                mClassCache.put(className, mapper);
                if (EasyRouter.getInstance().isDebugEnable()) {
                    LogUtils.i(TAG, "Mapping autowired fields of class '" + target + "'.");
                }
            }
        } catch (Exception e) {
            // 如果解析器获取失败，说明当前类中没有需要自动注入的参数，就将该类放到黑名单中
            mBlackList.add(className);
        }
    }
}
