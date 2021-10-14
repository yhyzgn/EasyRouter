package com.yhy.erouter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;

import com.yhy.erouter.common.EJsonParser;
import com.yhy.erouter.common.EPoster;
import com.yhy.erouter.service.AutowiredService;
import com.yhy.erouter.service.impl.AutowiredServiceImpl;
import com.yhy.erouter.utils.ELogUtils;

import androidx.fragment.app.Fragment;

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
    private boolean mDebugEnable;
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
        ELogUtils.getConfig().setApp(mApp).setGlobalTag(getClass().getSimpleName());
        return this;
    }

    /**
     * 是否处于Debug模式
     *
     * @param enable 是否处于Debug模式
     * @return 当前对象
     */
    public ERouter debug(boolean enable) {
        mDebugEnable = enable;
        ELogUtils.getConfig()
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
        } else if (ctx instanceof Service) {
            return with((Service) ctx);
        }
        return new EPoster(ctx).init(mApp);
    }

    /**
     * 设置当前Activity
     *
     * @param activity 当前Activity
     * @return 当前转发器
     */
    public EPoster with(Activity activity) {
        return new EPoster(activity).init(mApp);
    }

    /**
     * 设置当前Fragment
     *
     * @param fragment 当前Fragment
     * @return 当前转发器
     */
    public EPoster with(Fragment fragment) {
        return new EPoster(fragment).init(mApp);
    }

    /**
     * 设置当前Fragment
     *
     * @param fragment 当前Fragment
     * @return 当前转发器
     */
    public EPoster with(android.app.Fragment fragment) {
        return new EPoster(fragment).init(mApp);
    }

    /**
     * 设置当前Service
     *
     * @param service 当前Service
     * @return 当前转发器
     */
    public EPoster with(Service service) {
        return new EPoster(service).init(mApp);
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
