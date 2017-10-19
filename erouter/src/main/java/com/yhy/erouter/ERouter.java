package com.yhy.erouter;

import android.app.Activity;
import android.app.Service;
import android.support.v4.app.Fragment;

import com.yhy.erouter.common.EJsonParser;
import com.yhy.erouter.common.EPoster;
import com.yhy.erouter.expt.IllegalOperationException;
import com.yhy.erouter.service.AutowiredService;
import com.yhy.erouter.service.impl.AutowiredServiceImpl;
import com.yhy.erouter.utils.EUtils;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-17 15:13
 * version: 1.0.0
 * desc   : 路由管理器
 */
public class ERouter {

    // 单例对象
    private static volatile ERouter instance;

    private EJsonParser mJsonParser;

    /**
     * 构造函数
     */
    private ERouter() {
        if (null != instance) {
            throw new UnsupportedOperationException("Can not call constructor manual.");
        }
    }

    /**
     * 获取单例对象
     *
     * @return 单例对象
     */
    public static ERouter getInstance() {
        if (null == instance) {
            synchronized (ERouter.class) {
                if (null == instance) {
                    instance = new ERouter();
                }
            }
        }
        return instance;
    }

    public ERouter init() {
        return init(null);
    }

    public ERouter init(EJsonParser parser) {
        mJsonParser = parser;
        return this;
    }

    public EJsonParser getJsonParser() {
        return mJsonParser;
    }

    /**
     * 设置当前Activity
     *
     * @param activity 当前Activity
     * @return 当前转发器
     */
    public EPoster with(Activity activity) {
        return new EPoster(activity);
    }

    /**
     * 设置当前Fragment
     *
     * @param fragment 当前Fragment
     * @return 当前转发器
     */
    public EPoster with(Fragment fragment) {
        return new EPoster(fragment);
    }

    /**
     * 设置当前Service
     *
     * @param service 当前Service
     * @return 当前转发器
     */
    public EPoster with(Service service) {
        return new EPoster(service);
    }

    public void inject(Object target) {
        AutowiredService service = new AutowiredServiceImpl();
        service.autowired(target);
    }
}
