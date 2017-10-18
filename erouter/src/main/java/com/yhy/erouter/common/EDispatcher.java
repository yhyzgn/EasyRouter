package com.yhy.erouter.common;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.yhy.erouter.expt.UrlMatchException;
import com.yhy.erouter.utils.EUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-17 19:46
 * version: 1.0.0
 * desc   : 路由转发器
 */
public class EDispatcher {

    // 用来保存url和转发器的集合
    private static Map<String, EDispatcher> DISP_MAP = new HashMap<>();

    // 当前环境
    private Activity mActivity;
    private Fragment mFragment;
    private Service mService;

    // 分组
    private String mGroup;
    // 路径
    private String mUrl;

    // 保存url和路由数据的集合
    private Map<String, RouterMeta> mMetaMap;

    /**
     * 构造函数
     *
     * @param builder 构造器
     */
    private EDispatcher(Builder builder) {
        mActivity = builder.mActivity;
        mFragment = builder.mFragment;
        mService = builder.mService;
        mGroup = builder.mGroup;
        mUrl = builder.mUrl;

        mMetaMap = new HashMap<>();
    }

    /**
     * 获取目标
     *
     * @param <T> 目标对象类型
     * @return 目标对象
     * <p>
     * 值：
     * Activity -> XxxxActivity.class
     * Fragment -> new XxxxFragment()
     * Service  -> XxxxService.class
     */
    public <T> T get() {
        // 先尝试从缓存中获取
        RouterMeta meta = mMetaMap.get(mUrl);
        if (null != meta) {
            return parseResult(meta);
        }

        // 缓存中没有再从路由映射器中获取
        Map<String, RouterMeta> metaMap = getMetaMap();
        if (null != metaMap) {
            return parseResult(metaMap.get(mUrl));
        }
        return null;
    }

    /**
     * 转发路由
     *
     * @param <T> 目标对象类型
     * @return 目标对象
     * <p>
     * 值：
     * Activity -> XxxxActivity.class
     * Fragment -> new XxxxFragment()
     * Service  -> XxxxService.class
     */
    public <T> T go() {
        RouterMeta meta = mMetaMap.get(mUrl);
        if (null != meta) {
            return dispatch(meta);
        }

        Map<String, RouterMeta> metaMap = getMetaMap();
        if (null != metaMap) {
            return dispatch(metaMap.get(mUrl));
        }
        return null;
    }

    /**
     * 路由转发
     *
     * @param meta 路由数据
     * @param <T>  目标对象类型
     * @return 目标对象
     */
    private <T> T dispatch(RouterMeta meta) {
        if (null != meta) {
            // 针对不同的路由类型，选择对应的路由转发
            switch (meta.getType()) {
                case ACTIVITY: {
                    Intent acInte = dispatchActivity(meta);
                    return null == acInte ? null : (T) acInte;
                }
                case SERVICE: {
                    Intent svInte = dispatchService(meta);
                    return null == svInte ? null : (T) svInte;
                }
                case FRAGMENT: {
                    Fragment fm = dispatchFragment(meta);
                    return null == fm ? null : (T) fm;
                }
                case UNKNOWN:
                default: {
                    break;
                }
            }
        }
        throw new UrlMatchException("Not found router which " + mUrl);
    }

    /**
     * 转发Fragment路由，创建目标Fragment实例
     *
     * @param meta 路由数据
     * @return 目标Fragment实例
     */
    private Fragment dispatchFragment(RouterMeta meta) {
        try {
            return (Fragment) meta.getDest().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 转发Service路由，创建并启动目标服务
     *
     * @param meta 路由数据
     * @return 目标Service.class
     */
    private Intent dispatchService(RouterMeta meta) {
        Intent intent = null;
        if (null != mActivity) {
            // Activity中创建服务
            intent = new Intent(mActivity, meta.getDest());
            mActivity.startService(intent);
        } else if (null != mFragment) {
            // Fragment中创建服务
            intent = new Intent(mFragment.getActivity(), meta.getDest());
            mFragment.getActivity().startService(intent);
        } else if (null != mService) {
            // Service中创建服务
            intent = new Intent(mService, meta.getDest());
            mService.startService(intent);
        }
        return intent;
    }

    /**
     * 转发Activity路由，跳转到目标Activity
     *
     * @param meta 路由数据
     * @return 目标Activity.class
     */
    private Intent dispatchActivity(RouterMeta meta) {
        Intent intent = null;
        if (null != mActivity) {
            // Activity中跳转Activity
            intent = new Intent(mActivity, meta.getDest());
            mActivity.startActivity(intent);
        } else if (null != mFragment) {
            // Fragment中跳转Activity
            intent = new Intent(mFragment.getActivity(), meta.getDest());
            mFragment.startActivity(intent);
        } else if (null != mService) {
            // Service中跳转Activity页面
            intent = new Intent(mService, meta.getDest());
            // 此时需要添加新Activity栈的标识
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mService.startActivity(intent);
        }
        return intent;
    }

    /**
     * 根据路由类型，获取目标对象
     *
     * @param meta 路由数据
     * @param <T>  目标对象类型
     * @return 目标对象
     * <p>
     * 值：
     * Activity -> XxxxActivity.class
     * Fragment -> new XxxxFragment()
     * Service  -> XxxxService.class
     */
    private <T> T parseResult(RouterMeta meta) {
        if (null != meta) {
            switch (meta.getType()) {
                case ACTIVITY:
                case SERVICE: {
                    // Activity和Service都返回Xxxx.class
                    return (T) meta.getDest();
                }
                case FRAGMENT: {
                    // Fragment返回new XxxxFragment()
                    try {
                        return (T) meta.getDest().newInstance();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                case UNKNOWN:
                default: {
                    break;
                }
            }
        }
        throw new UrlMatchException("Not found router which " + mUrl);
    }

    /**
     * 从路由映射器中获取当前分组下的路由映射集合，并将其保存到缓存中
     *
     * @return
     */
    private Map<String, RouterMeta> getMetaMap() {
        // 先尝试从缓存中获取
        Map<String, RouterMeta> metaMap = EGroupMapCache.getInstance().get(mGroup);
        if (null != metaMap) {
            return metaMap;
        }

        // 缓存中不存在时再从路由映射器中获取
        metaMap = new HashMap<>();
        try {
            // 加载当前分组对应的java类
            Class<?> clazz = Class.forName(EConsts.PACKAGE_GROUP + "." + EConsts.PREFIX_OF_GROUP + EUtils.upCaseFirst(mGroup));
            // 获取到加载路由的方法
            Method loadGroup = clazz.getDeclaredMethod(EConsts.METHOD_LOAD, Map.class);
            // 创建当前分组的路由映射器对象
            ERouterGroupMapper erg = (ERouterGroupMapper) clazz.newInstance();
            // 执行映射器的加载路由方法
            loadGroup.invoke(erg, metaMap);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        // 存放到缓存中
        EGroupMapCache.getInstance().put(mGroup, metaMap);
        return metaMap;
    }

    /**
     * 转发器构造器
     */
    public static class Builder {
        // 当前环境
        private Activity mActivity;
        private Fragment mFragment;
        private Service mService;
        // 分组
        private String mGroup;
        // 路径
        private String mUrl;

        /**
         * 设置当前Activity
         *
         * @param activity 当前Activity
         * @return 当前构造器
         */
        public Builder with(Activity activity) {
            mActivity = activity;
            return this;
        }

        /**
         * 设置当前Fragment
         *
         * @param fragment 当前Fragment
         * @return 当前构造器
         */
        public Builder with(Fragment fragment) {
            mFragment = fragment;
            return this;
        }

        /**
         * 设置当前Service
         *
         * @param service 当前Service
         * @return 当前构造器
         */
        public Builder with(Service service) {
            mService = service;
            return this;
        }

        /**
         * 设置目标路由
         *
         * @param group 分组
         * @param url   路径
         * @return 当前构造器
         */
        public Builder target(String group, String url) {
            mGroup = group;
            mUrl = url;
            return this;
        }

        /**
         * 创建转发器
         *
         * @return 转发器
         */
        public EDispatcher build() {
            // 尝试从缓存中获取，缓存中没有再创建
            EDispatcher disp = DISP_MAP.get(mUrl);
            if (null == disp) {
                disp = new EDispatcher(this);
                DISP_MAP.put(mUrl, disp);
            }
            return disp;
        }
    }
}
