package com.yhy.easyrouter.utils;

import android.app.Application;
import android.widget.Toast;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-12-26 9:24
 * version: 1.0.0
 * desc   :
 */
public class ToastUtils {
    private static Application app;

    private ToastUtils() {
    }

    public static void init(Application app) {
        ToastUtils.app = app;
    }

    public static void toast(String text) {
        if (null == app) {
            return;
        }
        Toast.makeText(app, text, Toast.LENGTH_LONG).show();
    }
}
