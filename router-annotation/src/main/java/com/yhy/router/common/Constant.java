package com.yhy.router.common;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-17 11:00
 * version: 1.0.0
 * desc   : 常量类
 */
public interface Constant {

    // 路由入口
    String EASY_ROUTER = "com.yhy.router.EasyRouter";

    // 模块名
    String MODULE_NAME = "module.name";

    // 增量编译开关
    String ENABLE_INCREMENTAL = "router.incremental";

    // 日志打印前缀
    String PREFIX_OF_LOGGER = "::RouterLogger::";

    String SEPARATOR = "_$$_";

    // 路由类型标识
    String ACTIVITY = "android.app.Activity";
    String FRAGMENT = "android.app.Fragment";
    String FRAGMENT_X = "androidx.fragment.app.Fragment";
    String SERVICE = "android.app.Service";

    // 生成分组类的包名
    String GROUP_PACKAGE = "com.yhy.router.group";
    // 生成分组类需要实现的接口(路由映射器)
    String ROUTER_GROUP_MAPPER = "com.yhy.router.mapper.RouterGroupMapper";
    // 生成分组类的前缀
    String PREFIX_OF_GROUP = "RouterGroup";
    // 默认的模块名称
    String DEF_MODULE_NAME = "DefaultModule";
    // 分组类中用来加载路由映射的方法名称和参数名称
    String METHOD_ROUTER_LOAD = "load";
    String METHOD_ROUTER_LOAD_ARG = "metaMap";

    // 生成自动注入类需要实现的接口(字段映射器)
    String AUTO_WIRED_MAPPER = "com.yhy.router.mapper.AutowiredMapper";
    String SUFFIX_AUTOWIRED = "Autowired";
    // 用来执行自动注入操作的方法名称和参数名称
    String METHOD_AUTOWIRED_INJECT = "inject";
    String METHOD_AUTOWIRED_INJECT_TARGET = "target";

    // Json转换器接口
    String JSON_CONVERTER = "com.yhy.router.common.JsonConverter";
    // private字段名称，反射中使用
    String PRIVATE_FIELD_NAME = "field";
    // Json解析字段名称
    String JSON_PARSER_NAME = "mJsonConverter";

    // 自定义拦截器需要实现的接口
    String INTERCEPTOR_INTERFACE = "com.yhy.router.interceptor.TransferInterceptor";
    // 拦截器映射器接口
    String INTERCEPTOR_MAPPER = "com.yhy.router.mapper.InterceptorMapper";
    // 生成拦截器的包名
    String INTERCEPTOR_PACKAGE = "com.yhy.router.interceptor";
    // 拦截器映射器实现类后缀
    String SUFFIX_INTERCEPTOR_CLASS = "Impl";
    // 拦截器映射器加载映射关系的方法名称
    String INTERCEPTOR_MAPPER_LOAD = "load";
    // 拦截器映射器加载映射关系的方法参数名称
    String INTERCEPTOR_MAPPER_LOAD_ARG = "interMap";

    // Java类型
    String LANG = "java.lang";
    String BYTE = LANG + ".Byte";
    String SHORT = LANG + ".Short";
    String INTEGER = LANG + ".Integer";
    String LONG = LANG + ".Long";
    String FLOAT = LANG + ".Float";
    String DOUBEL = LANG + ".Double";
    String BOOLEAN = LANG + ".Boolean";
    String STRING = LANG + ".String";
    String SERIALIZABLE = "java.io.Serializable";
    String PARCELABLE = "android.os.Parcelable";

    // ...
    String AUTHOR = "颜洪毅";
    String E_MAIL = "yhyzgn@gmail.com";
    String GITHUB_URL = "https://github.com/yhyzgn";
}
