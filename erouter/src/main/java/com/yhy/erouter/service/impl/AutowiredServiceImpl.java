package com.yhy.erouter.service.impl;

import android.util.LruCache;

import com.yhy.erouter.common.EConsts;
import com.yhy.erouter.mapper.EAutowiredMapper;
import com.yhy.erouter.service.AutowiredService;

import java.util.ArrayList;
import java.util.List;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-19 10:42
 * version: 1.0.0
 * desc   :
 */
public class AutowiredServiceImpl implements AutowiredService {
    private LruCache<String, EAutowiredMapper> mClassCache;
    private List<String> mBlackList;

    public AutowiredServiceImpl() {
        mClassCache = new LruCache<>(100);
        mBlackList = new ArrayList<>();
    }

    @Override
    public void autowired(Object target) {
        String className = target.getClass().getName();
        try {
            if (!mBlackList.contains(className)) {
                EAutowiredMapper mapper = mClassCache.get(className);
                if (null == mapper) {
                    mapper = (EAutowiredMapper) Class.forName(className + EConsts.SUFFIX_AUTOWIRED).newInstance();
                }
                mapper.inject(target);
                mClassCache.put(className, mapper);
            }
        } catch (Exception e) {
            mBlackList.add(className);
            e.printStackTrace();
        }
    }
}
