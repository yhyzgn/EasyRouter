package com.yhy.erouter.common;

import com.yhy.erouter.annotation.Router;
import com.yhy.erouter.utils.EUtils;

import javax.lang.model.element.Element;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-17 11:39
 * version: 1.0.0
 * desc   :
 */
public class RouterMeta {
    private String mUrl;
    private Element mElt;
    private RouterType mType;
    private String mGroup;
    private Class<?> mDest;

    public static RouterMeta build(String url, Class<?> dest, RouterType type, String group) {
        return new RouterMeta(url, null, dest, type, group);
    }

    public RouterMeta(Router router, Element elt, RouterType type) {
        this(router.url(), elt, null, type, router.group());
    }

    public RouterMeta(Router router, Element elt, Class<?> dest, RouterType type) {
        this(router.url(), elt, dest, type, router.group());
    }

    public RouterMeta(String url, Element elt, Class<?> dest, RouterType type, String group) {
        mUrl = url;
        mElt = elt;
        mType = type;
        mDest = dest;
        mGroup = !EUtils.isEmpty(group) ? group : EUtils.getGroupFromUrl(mUrl);
    }

    public String getUrl() {
        return mUrl;
    }

    public Element getElement() {
        return mElt;
    }

    public RouterType getType() {
        return mType;
    }

    public String getGroup() {
        return mGroup;
    }
}
