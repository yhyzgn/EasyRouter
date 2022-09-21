package com.yhy.router;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;

import androidx.fragment.app.Fragment;

import com.yhy.router.common.JsonConverter;
import com.yhy.router.common.Transmitter;
import com.yhy.router.service.AutowiredService;
import com.yhy.router.service.impl.AutowiredServiceImpl;
import com.yhy.router.utils.LogUtils;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-17 15:13
 * version: 1.0.0
 * desc   : 路由管理器
 */
public class Router {

    // 单例对象
    @SuppressLint("StaticFieldLeak")
    private static volatile Router instance;

    private final AutowiredService mAutowiredService;

    private Application mApp;
    private boolean mDebugEnable;
    private JsonConverter mJsonConverter;

    /**
     * 构造函数
     */
    private Router() {
        if (null != instance) {
            throw new UnsupportedOperationException("Can not call constructor manual.");
        }
        mAutowiredService = new AutowiredServiceImpl();
    }

    /**
     * 获取单例对象
     *
     * @return 单例对象
     */
    public static Router getInstance() {
        if (null == instance) {
            synchronized (Router.class) {
                if (null == instance) {
                    instance = new Router();
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
    public Router init(Application app) {
        mApp = app;
        LogUtils.getConfig().setApp(mApp).setGlobalTag(getClass().getSimpleName());
        return this;
    }

    /**
     * 是否处于Debug模式
     *
     * @param enable 是否处于Debug模式
     * @return 当前对象
     */
    public Router debug(boolean enable) {
        mDebugEnable = enable;
        LogUtils.getConfig()
                .setLogEnable(mDebugEnable)
                .setConsoleEnable(mDebugEnable)
                .setLogHeadEnable(mDebugEnable)
                .setBorderEnable(mDebugEnable);
        return this;
    }

    /**
     * 初始化
     *
     * @param parser Json解析器
     * @return 当前对象
     */
    public Router jsonParser(JsonConverter parser) {
        mJsonConverter = parser;
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
     * 是否处于Debug模式
     *
     * @return 是否处于Debug模式
     */
    public boolean isDebugEnable() {
        return mDebugEnable;
    }

    /**
     * 获取EJsonParser
     *
     * @return Json解析器
     */
    public JsonConverter getJsonParser() {
        return mJsonConverter;
    }

    /**
     * 设置当前上下文
     *
     * @param ctx 上下文
     * @return 当前对象
     */
    public Transmitter with(Context ctx) {
        if (ctx instanceof Activity) {
            return with((Activity) ctx);
        } else if (ctx instanceof Service) {
            return with((Service) ctx);
        }
        return new Transmitter(ctx).init(mApp);
    }

    /**
     * 设置当前Activity
     *
     * @param activity 当前Activity
     * @return 当前转发器
     */
    public Transmitter with(Activity activity) {
        return new Transmitter(activity).init(mApp);
    }

    /**
     * 设置当前Fragment
     *
     * @param fragment 当前Fragment
     * @return 当前转发器
     */
    public Transmitter with(Fragment fragment) {
        return new Transmitter(fragment).init(mApp);
    }

    /**
     * 设置当前Fragment
     *
     * @param fragment 当前Fragment
     * @return 当前转发器
     */
    public Transmitter with(android.app.Fragment fragment) {
        return new Transmitter(fragment).init(mApp);
    }

    /**
     * 设置当前Service
     *
     * @param service 当前Service
     * @return 当前转发器
     */
    public Transmitter with(Service service) {
        return new Transmitter(service).init(mApp);
    }

    /**
     * 成员自动注入入口
     *
     * @param target 当前需要自动注入的类，一般传this即可
     */
    public void inject(Object target) {
        // 执行自动注入
        mAutowiredService.autowired(target);
    }
}
