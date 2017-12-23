package com.yhy.erouter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.support.v4.app.Fragment;

import com.yhy.erouter.common.EJsonParser;
import com.yhy.erouter.common.EPoster;
import com.yhy.erouter.service.AutowiredService;
import com.yhy.erouter.service.impl.AutowiredServiceImpl;
import com.yhy.erouter.utils.LogUtils;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-17 15:13
 * version: 1.0.0
 * desc   : 路由管理器
 */
public class ERouter {

    // 单例对象
    @SuppressLint("StaticFieldLeak")
    private static volatile ERouter instance;

    private Application mApp;
    private boolean mLogEnable;
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

    /**
     * 初始化
     *
     * @param app 当前应用Application
     * @return 当前对象
     */
    public ERouter init(Application app) {
        mApp = app;
        LogUtils.getConfig().setApp(mApp).setGlobalTag(getClass().getSimpleName());
        return this;
    }

    /**
     * 是否开启log
     *
     * @param enable 是否开启log
     * @return 当前对象
     */
    public ERouter log(boolean enable) {
        mLogEnable = enable;
        LogUtils.getConfig()
                .setLogEnable(mLogEnable)
                .setConsoleEnable(mLogEnable)
                .setLogHeadEnable(mLogEnable)
                .setBorderEnable(mLogEnable);
        return this;
    }

    /**
     * 初始化
     *
     * @param parser Json解析器
     * @return 当前对象
     */
    public ERouter jsonParser(EJsonParser parser) {
        mJsonParser = parser;
        return this;
    }

    /**
     * 获取当前应用Application
     *
     * @return 当前应用Application
     */
    public Application getApp() {
        return mApp;
    }

    /**
     * 获取log开关
     *
     * @return log开关
     */
    public boolean getLogEnable() {
        return mLogEnable;
    }

    /**
     * 获取EJsonParser
     *
     * @return Json解析器
     */
    public EJsonParser getJsonParser() {
        return mJsonParser;
    }

    /**
     * 设置当前上下文
     *
     * @param ctx 上下文
     * @return 当前对象
     */
    public EPoster with(Context ctx) {
        if (ctx instanceof Activity) {
            return with((Activity) ctx);
        }
        return with((Service) ctx);
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

    /**
     * 成员自动注入入口
     *
     * @param target 当前需要自动注入的类，一般传this即可
     */
    public void inject(Object target) {
        AutowiredService service = new AutowiredServiceImpl();
        // 执行自动注入
        service.autowired(target);
    }
}
