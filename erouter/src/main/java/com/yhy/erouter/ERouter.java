package com.yhy.erouter;

import android.app.Activity;
import android.app.Service;
import android.support.v4.app.Fragment;

import com.yhy.erouter.common.EDispatcher;
import com.yhy.erouter.expt.IllegalOperationException;
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

    // 转发器构造器
    private EDispatcher.Builder mBuilder;
    // 转发器
    private EDispatcher mDispatcher;

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

    /**
     * 设置当前Activity
     *
     * @param activity 当前Activity
     * @return 当前对象
     */
    public ERouter with(Activity activity) {
        reset();
        mBuilder.with(activity);
        return this;
    }

    /**
     * 设置当前Fragment
     *
     * @param fragment 当前Fragment
     * @return 当前对象
     */
    public ERouter with(Fragment fragment) {
        reset();
        mBuilder.with(fragment);
        return this;
    }

    /**
     * 设置当前Service
     *
     * @param service 当前Service
     * @return 当前对象
     */
    public ERouter with(Service service) {
        reset();
        mBuilder.with(service);
        return this;
    }

    /**
     * 设置目标路径
     *
     * @param url 目标路径
     * @return 当前对象
     */
    public ERouter target(String url) {
        return target(EUtils.getGroupFromUrl(url), url);
    }

    /**
     * 设置目标路径
     *
     * @param group 分组名称
     * @param url   目标路径
     * @return 当前对象
     */
    public ERouter target(String group, String url) {
        varify();
        mBuilder.target(group, url);
        return this;
    }

    /**
     * 获取目标
     *
     * @param <T> 目标对象类型
     * @return 目标对象
     * <p>
     * 值：
     * Activity :: XxxxActivity.class
     * Fragment :: new XxxxFragment()
     * Service  :: XxxxService.class
     */
    public <T> T get() {
        varify();
        mDispatcher = mBuilder.build();
        return mDispatcher.get();
    }

    /**
     * 转发路由
     *
     * @param <T> 目标对象类型
     * @return 目标对象
     * <p>
     * 值：
     * Activity :: XxxxActivity.class
     * Fragment :: new XxxxFragment()
     * Service  :: XxxxService.class
     */
    public <T> T go() {
        varify();
        mDispatcher = mBuilder.build();
        return mDispatcher.go();
    }

    /**
     * 重置构造器
     */
    private void reset() {
        mBuilder = new EDispatcher.Builder();
    }

    /**
     * 检查构造器
     */
    private void varify() {
        if (null == mBuilder) {
            throw new IllegalOperationException("Must call with to reset dispatcher builder at first.");
        }
    }
}
