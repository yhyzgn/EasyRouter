package com.yhy.erouter.common;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-17 11:00
 * version: 1.0.0
 * desc   : 常量类
 */
public interface EConsts {

    // 日志打印前缀
    String PREFIX_OF_LOGGER = "::RouterLogger ";

    // 路由类型标识
    String ACTIVITY = "android.app.Activity";
    String FRAGMENT = "android.app.Fragment";
    String FRAGMENT_V4 = "android.support.v4.app.Fragment";
    String SERVICE = "android.app.Service";

    // 生成分组类的包名
    String PACKAGE_GROUP = "com.yhy.erouter.group";
    // 生成分组类需要实现的接口(路由映射器)
    String ROUTER_GROUP_MAPPER = "com.yhy.erouter.common.ERouterGroupMapper";
    // 生成分组类的前缀
    String PREFIX_OF_GROUP = "RouterGroup";
    // 分组类中用来加载路由映射的方法名称和参数名称
    String METHOD_LOAD = "load";
    String METHOD_LOAD_ARG = "metaMap";

    // ...
    String AUTHOR = "颜洪毅";
    String E_MAIL = "yhyzgn@gmail.com";
    String GITHUB_URL = "https://github.com/yhyzgn";
}
