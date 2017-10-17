package com.yhy.erouter.common;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-17 11:35
 * version: 1.0.0
 * desc   :
 */
public enum RouterType {
    ACTIVITY(0, "android.app.Activity"),
    SERVICE(1, "android.app.Service"),
    FRAGMENT(-1, "android.app.Fragment"),
    UNKNOWN(-1, "Unknown route type");

    int id;
    String name;

    RouterType(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static RouterType parse(String name) {
        for (RouterType rt : RouterType.values()) {
            if (name.equals(rt.name)) {
                return rt;
            }
        }
        return UNKNOWN;
    }
}
