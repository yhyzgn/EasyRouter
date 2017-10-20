package com.yhy.easyrouter;

import android.app.Application;

import com.google.gson.Gson;
import com.yhy.easyrouter.entity.User;
import com.yhy.erouter.ERouter;
import com.yhy.erouter.common.EJsonParser;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-19 17:56
 * version: 1.0.0
 * desc   :
 */
public class App extends Application {
    private static App instance;

    private User mUser;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        ERouter.getInstance().init(new EJsonParser() {
            Gson gson = new Gson();

            @Override
            public <T> T fromJson(String json, Class<T> clazz) {
                return gson.fromJson(json, clazz);
            }

            @Override
            public <T> String toJson(T obj) {
                return gson.toJson(obj);
            }
        });
    }

    public static App getInstance() {
        return instance;
    }

    public void setUser(User user) {
        mUser = user;
    }

    public User getUser() {
        return mUser;
    }
}
