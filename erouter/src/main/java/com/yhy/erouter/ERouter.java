package com.yhy.erouter;

import android.app.Activity;
import android.app.Service;
import android.support.v4.app.Fragment;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-17 15:13
 * version: 1.0.0
 * desc   : 路由管理器
 */
public class ERouter {

    private static volatile ERouter instance;

    private ERouter() {
        if (null != instance) {
            throw new UnsupportedOperationException("Can not call constructor manual.");
        }
    }

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

    public ERouter with(Activity activity) {
        return this;
    }

    public ERouter with(Fragment fragment) {
        return this;
    }

    public ERouter with(Service service) {
        return this;
    }

    public ERouter target(String url) {
        return this;
    }

    public void go() {
    }
}
