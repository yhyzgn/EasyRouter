package com.yhy.erouter.common;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import com.yhy.erouter.ERouter;
import com.yhy.erouter.expt.IllegalOperationException;
import com.yhy.erouter.expt.UrlMatchException;
import com.yhy.erouter.mapper.ERouterGroupMapper;
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
public class EPoster {

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

    private Bundle mParams;

    /**
     * 构造函数
     *
     * @param activity 当前Activity
     */
    public EPoster(Activity activity) {
        this(activity, null, null);
    }

    /**
     * 构造函数
     *
     * @param fragment 当前Fragment
     */
    public EPoster(Fragment fragment) {
        this(null, fragment, null);
    }

    /**
     * 构造函数
     *
     * @param service 当前Service
     */
    public EPoster(Service service) {
        this(null, null, service);
    }

    /**
     * 构造函数
     *
     * @param activity 构造器
     * @param fragment 构造器
     * @param service  构造器
     */
    public EPoster(Activity activity, Fragment fragment, Service service) {
        mActivity = activity;
        mFragment = fragment;
        mService = service;

        mMetaMap = new HashMap<>();
        mParams = new Bundle();

    }

    /**
     * 设置目标路径
     *
     * @param url 目标路径
     * @return 当前构造器
     */
    public EPoster to(String url) {
        return to(EUtils.getGroupFromUrl(url), url);
    }

    /**
     * 设置目标路径
     *
     * @param group 分组名称
     * @param url   目标路径
     * @return 当前构造器
     */
    public EPoster to(String group, String url) {
        mGroup = TextUtils.isEmpty(group) ? EUtils.getGroupFromUrl(url) : group;
        mUrl = url;
        return this;
    }

    public EPoster param(String name, int value) {
        return setParam(TypeKind.INT.ordinal(), name, value);
    }

    public EPoster param(String name, byte value) {
        return setParam(TypeKind.BYTE.ordinal(), name, value);
    }

    public EPoster param(String name, short value) {
        return setParam(TypeKind.SHORT.ordinal(), name, value);
    }

    public EPoster param(String name, boolean value) {
        return setParam(TypeKind.BOOLEAN.ordinal(), name, value);
    }

    public EPoster param(String name, long value) {
        return setParam(TypeKind.LONG.ordinal(), name, value);
    }

    public EPoster param(String name, float value) {
        return setParam(TypeKind.FLOAT.ordinal(), name, value);
    }

    public EPoster param(String name, double value) {
        return setParam(TypeKind.DOUBLE.ordinal(), name, value);
    }

    public EPoster param(String name, String value) {
        return setParam(TypeKind.STRING.ordinal(), name, value);
    }

    public EPoster param(String name, Parcelable value) {
        return setParam(TypeKind.PARCELABLE.ordinal(), name, value);
    }

    public EPoster param(String name, Object value) {
        return setParam(TypeKind.OBJECT.ordinal(), name, value);
    }

    /**
     * 获取目标
     *
     * @param <T> 目标对象类型
     * @return 目标对象
     * <p>
     * 值：
     * Activity :: XxxxActivity.class
     * Fragment :: new XxxxFragment()
     * Service  :: XxxxService.class
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
     * Activity :: XxxxActivity.class
     * Fragment :: new XxxxFragment()
     * Service  :: XxxxService.class
     */
    public <T> T go() {
        RouterMeta meta = mMetaMap.get(mUrl);
        if (null != meta) {
            return post(meta);
        }

        Map<String, RouterMeta> metaMap = getMetaMap();
        if (null != metaMap) {
            return post(metaMap.get(mUrl));
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
    private <T> T post(RouterMeta meta) {
        if (null != meta) {
            // 针对不同的路由类型，选择对应的路由转发
            switch (meta.getType()) {
                case ACTIVITY: {
                    Intent acInte = postActivity(meta);
                    return null == acInte ? null : (T) acInte;
                }
                case SERVICE: {
                    Intent svInte = postService(meta);
                    return null == svInte ? null : (T) svInte;
                }
                case FRAGMENT: {
                    Fragment fm = postFragment(meta);
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
    private Fragment postFragment(RouterMeta meta) {
        try {
            Fragment fm = (Fragment) meta.getDest().newInstance();
            fm.setArguments(mParams);
            return fm;
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
    private Intent postService(RouterMeta meta) {
        Intent intent = null;
        if (null != mActivity) {
            // Activity中创建服务
            intent = new Intent(mActivity, meta.getDest());
            intent.putExtras(mParams);
            mActivity.startService(intent);
        } else if (null != mFragment) {
            // Fragment中创建服务
            intent = new Intent(mFragment.getActivity(), meta.getDest());
            intent.putExtras(mParams);
            mFragment.getActivity().startService(intent);
        } else if (null != mService) {
            // Service中创建服务
            intent = new Intent(mService, meta.getDest());
            intent.putExtras(mParams);
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
    private Intent postActivity(RouterMeta meta) {
        Intent intent = null;
        if (null != mActivity) {
            // Activity中跳转Activity
            intent = new Intent(mActivity, meta.getDest());
            intent.putExtras(mParams);
            mActivity.startActivity(intent);
        } else if (null != mFragment) {
            // Fragment中跳转Activity
            intent = new Intent(mFragment.getActivity(), meta.getDest());
            intent.putExtras(mParams);
            mFragment.startActivity(intent);
        } else if (null != mService) {
            // Service中跳转Activity页面
            intent = new Intent(mService, meta.getDest());
            // 此时需要添加新Activity栈的标识
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtras(mParams);
            mService.startActivity(intent);
        }
        return intent;
    }

//    private void equipParams(RouterMeta meta) {
//        if (null == meta || null == meta.getParamsType()) {
//            return;
//        }
//
//        Map<String, Integer> paramsType = meta.getParamsType();
//        int type;
//        String name;
//        for (Map.Entry<String, Integer> et : paramsType.entrySet()) {
//            name = et.getKey();
//            type = et.getValue();
//            if (type == TypeKind.PARCELABLE.ordinal()) {
//                mParams.putParcelable(name, mParams.getParcelable(name));
//            } else {
//                setParam(type, name, mParams.get(name));
//            }
//        }
//    }

    private <T> EPoster setParam(Integer type, String name, T value) {
        if (null == type || TextUtils.isEmpty(name) || null == value) {
            return this;
        }

        if (type == TypeKind.PARCELABLE.ordinal()) {
            // Parcelable 无法从字符串解析
            mParams.putParcelable(name, (Parcelable) value);
        } else if (type == TypeKind.OBJECT.ordinal()) {
            // 将对象转换为Json传递
            EJsonParser jsonParser = ERouter.getInstance().getJsonParser();
            if (null == jsonParser) {
                throw new IllegalOperationException("If you want to use EJsonParser, must set EJsonParser in initialization of ERouter!");
            }
            mParams.putString(name, jsonParser.toJson(value));
        } else {
            String strVal = (String) value;

            if (type == TypeKind.INT.ordinal()) {
                mParams.putInt(name, Integer.valueOf(strVal));
            } else if (type == TypeKind.BYTE.ordinal()) {
                mParams.putByte(name, Byte.valueOf(strVal));
            } else if (type == TypeKind.SHORT.ordinal()) {
                mParams.putShort(name, Short.valueOf(strVal));
            } else if (type == TypeKind.INT.ordinal()) {
                mParams.putBoolean(name, Boolean.valueOf(strVal));
            } else if (type == TypeKind.LONG.ordinal()) {
                mParams.putLong(name, Long.valueOf(strVal));
            } else if (type == TypeKind.FLOAT.ordinal()) {
                mParams.putFloat(name, Float.valueOf(strVal));
            } else if (type == TypeKind.DOUBLE.ordinal()) {
                mParams.putDouble(name, Double.valueOf(strVal));
            } else if (type == TypeKind.STRING.ordinal()) {
                mParams.putString(name, strVal);
            } else {
                // 默认传入字符串
                mParams.putString(name, strVal);
            }
        }
        return this;
    }

    /**
     * 根据路由类型，获取目标对象
     *
     * @param meta 路由数据
     * @param <T>  目标对象类型
     * @return 目标对象
     * <p>
     * 值：
     * Activity :: XxxxActivity.class
     * Fragment :: new XxxxFragment()
     * Service  :: XxxxService.class
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
     * @return 一个分组下的路由映射集合
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
            Method loadGroup = clazz.getDeclaredMethod(EConsts.METHOD_ROUTER_LOAD, Map.class);
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
}
