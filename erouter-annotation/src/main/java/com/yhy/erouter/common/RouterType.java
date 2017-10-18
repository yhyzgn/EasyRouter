package com.yhy.erouter.common;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-17 11:35
 * version: 1.0.0
 * desc   : 路由类型枚举
 */
public enum RouterType {
    // 定义各种类型
    ACTIVITY(0, "android.app.Activity"),
    SERVICE(1, "android.app.Service"),
    FRAGMENT(2, "android.app.Fragment"),
    UNKNOWN(-1, "Unknown route type");

    int id;
    String name;

    RouterType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * 获取id
     *
     * @return id
     */
    public int getId() {
        return id;
    }

    /**
     * 获取名称
     *
     * @return 名称
     */
    public String getName() {
        return name;
    }
}
