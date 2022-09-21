package com.yhy.router.common;

import com.yhy.router.annotation.Router;
import com.yhy.router.utils.EUtils;

import java.util.Map;

import javax.lang.model.element.Element;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-17 11:39
 * version: 1.0.0
 * desc   : 整个路由数据
 */
public class RouterMeta {
    // 路径
    private final String mUrl;
    // 路由所注解的元素
    private final Element mElt;
    // 路由类型
    private final RouterType mType;
    // 路由分组
    private final String mGroup;
    // 路由所注解的目标类
    private final Class<?> mDest;

    private final Map<String, Integer> mParamsType;

    /**
     * 在生成的分组类中解析映射路由调用
     *
     * @param url        路径
     * @param dest       目标类
     * @param type       类型
     * @param group      分组
     * @param paramsType 参数类型
     * @return 路由数据
     */
    public static RouterMeta build(String url, Class<?> dest, RouterType type, String group, Map<String, Integer> paramsType) {
        return new RouterMeta(url, null, dest, type, group, paramsType);
    }

    /**
     * 构造函数
     *
     * @param router     路由注解
     * @param elt        注解元素
     * @param type       类型
     * @param paramsType 参数类型
     */
    public RouterMeta(Router router, Element elt, RouterType type, Map<String, Integer> paramsType) {
        this(router.url(), elt, null, type, router.group(), paramsType);
    }

    /**
     * 构造函数
     *
     * @param router     路由注解
     * @param elt        注解元素
     * @param dest       目标类
     * @param type       类型
     * @param paramsType 参数类型
     */
    public RouterMeta(Router router, Element elt, Class<?> dest, RouterType type, Map<String, Integer> paramsType) {
        this(router.url(), elt, dest, type, router.group(), paramsType);
    }

    /**
     * 构造函数
     *
     * @param url        路径
     * @param elt        注解元素
     * @param dest       目标类
     * @param type       类型
     * @param group      分组
     * @param paramsType 参数类型
     */
    public RouterMeta(String url, Element elt, Class<?> dest, RouterType type, String group, Map<String, Integer> paramsType) {
        mUrl = url;
        mElt = elt;
        mType = type;
        mDest = dest;
        mParamsType = paramsType;

        // 检查并得到分组名称。
        // 分组名称默认是路径的第一部分，以手动设置值为主
        mGroup = !EUtils.isEmpty(group) ? group : EUtils.getGroupFromUrl(mUrl);
    }

    /**
     * 获取路径
     *
     * @return 路径
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     * 获取所注解的元素
     *
     * @return 注解元素
     */
    public Element getElement() {
        return mElt;
    }

    /**
     * 获取类型
     *
     * @return 类型
     */
    public RouterType getType() {
        return mType;
    }

    /**
     * 获取分组
     *
     * @return 分组
     */
    public String getGroup() {
        return mGroup;
    }

    /**
     * 获取目标类
     *
     * @return 目标类
     */
    public Class<?> getDest() {
        return mDest;
    }

    public Map<String, Integer> getParamsType() {
        return mParamsType;
    }
}
