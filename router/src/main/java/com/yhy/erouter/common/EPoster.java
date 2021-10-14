package com.yhy.erouter.common;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.View;

import com.yhy.erouter.ERouter;
import com.yhy.erouter.callback.Callback;
import com.yhy.erouter.expt.IllegalOperationException;
import com.yhy.erouter.expt.UrlMatchException;
import com.yhy.erouter.interceptor.EInterceptor;
import com.yhy.erouter.mapper.EInterceptorMapper;
import com.yhy.erouter.mapper.ERouterGroupMapper;
import com.yhy.erouter.utils.EClassUtils;
import com.yhy.erouter.utils.ELogUtils;
import com.yhy.erouter.utils.EUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-17 19:46
 * version: 1.0.0
 * desc   : 路由转发器
 */
@SuppressWarnings("unchecked")
public class EPoster {
    private final String TAG = getClass().getSimpleName();
    private Application mApp;
    // 当前环境
    private Context mContext;
    private Activity mActivity;
    private Fragment mFragmentX;
    private android.app.Fragment mFragment;
    private Service mService;
    // 分组
    private String mGroup;
    // 路径
    private String mUrl;
    // 保存url和路由数据的集合
    private Map<String, RouterMeta> mMetaMap;
    // 目标Activity的Uri
    private Uri mUri;
    // Activity跳转时的Action
    private String mAction;
    // 路由参数
    private Bundle mParams;
    // 拦截器名称集合
    private List<String> mInterList;
    // 拦截器名称和实例映射集合
    private Map<String, EInterceptor> mInterMap;
    // 请求码，只有startActivityForResult时使用
    private int mRequestCode;
    // 路由回调
    private Callback mCallback;
    // Activity切换动画
    private int mTransEnter;
    private int mTransExit;
    // Activity共享元素动画
    private Pair<View, String>[] mAnimArr;
    private ActivityOptionsCompat mOptions;

    // Intent Flag List
    private List<Integer> mFlagList;

    // Intent Category List
    private List<String> mCategoryList;

    public EPoster(Context context) {
        this(context, null, null, null, null);
    }

    /**
     * 构造函数
     *
     * @param activity 当前Activity
     */
    public EPoster(Activity activity) {
        this(null, activity, null, null, null);
    }

    /**
     * 构造函数
     *
     * @param fragmentX 当前Fragment
     */
    public EPoster(Fragment fragmentX) {
        this(null, null, fragmentX, null, null);
    }

    /**
     * 构造函数
     *
     * @param fragment 当前Fragment
     */
    public EPoster(android.app.Fragment fragment) {
        this(null, null, null, fragment, null);
    }

    /**
     * 构造函数
     *
     * @param service 当前Service
     */
    public EPoster(Service service) {
        this(null, null, null, null, service);
    }

    /**
     * 构造函数
     *
     * @param context   当前上下文
     * @param activity  当前Activity
     * @param fragmentX 当前Fragment
     * @param fragment  当前Fragment
     * @param service   当前Service
     */
    private EPoster(Context context, Activity activity, Fragment fragmentX, android.app.Fragment fragment, Service service) {
        mContext = context;
        mActivity = activity;
        mFragmentX = fragmentX;
        mFragment = fragment;
        mService = service;

        // 初始化
        mRequestCode = -1;
        mMetaMap = new HashMap<>();
        mParams = new Bundle();
        mInterList = new ArrayList<>();
        mInterMap = new HashMap<>();
        mFlagList = new ArrayList<>();
        mCategoryList = new ArrayList<>();
    }

    /**
     * 初始化Application
     *
     * @param app 当前Application
     * @return 当前对象
     */
    public EPoster init(Application app) {
        mApp = app;
        return this;
    }

    /**
     * 获取当前Application
     *
     * @return 当前Application
     */
    public Application getApp() {
        return mApp;
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
        if (ERouter.getInstance().isDebugEnable()) {
            ELogUtils.i(TAG, "Set url as '" + mUrl + "'.");
        }
        return this;
    }

    /**
     * 设置参数
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 当前对象
     */
    public EPoster param(String name, int value) {
        return setParam(TypeKind.INT.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 当前对象
     */
    public EPoster param(String name, byte value) {
        return setParam(TypeKind.BYTE.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 当前对象
     */
    public EPoster param(String name, short value) {
        return setParam(TypeKind.SHORT.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 当前对象
     */
    public EPoster param(String name, boolean value) {
        return setParam(TypeKind.BOOLEAN.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 当前对象
     */
    public EPoster param(String name, long value) {
        return setParam(TypeKind.LONG.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 当前对象
     */
    public EPoster param(String name, float value) {
        return setParam(TypeKind.FLOAT.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 当前对象
     */
    public EPoster param(String name, double value) {
        return setParam(TypeKind.DOUBLE.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 当前对象
     */
    public EPoster param(String name, String value) {
        return setParam(TypeKind.STRING.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 当前对象
     */
    public EPoster param(String name, Serializable value) {
        return setParam(TypeKind.SERIALIZABLE.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 当前对象
     */
    public EPoster param(String name, Parcelable value) {
        return setParam(TypeKind.PARCELABLE.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 当前对象
     */
    public EPoster param(String name, Object value) {
        return setParam(TypeKind.OBJECT.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param bundle bundle参数
     * @return 当前对象
     */
    public EPoster param(Bundle bundle) {
        if (null == mParams) {
            mParams = bundle;
        } else {
            mParams.putAll(bundle);
        }
        return this;
    }

    /**
     * 添加拦截器
     *
     * @param name 拦截器名称
     * @return 当前对象
     */
    public EPoster interceptor(String name) {
        if (!mInterList.contains(name)) {
            mInterList.add(name);
            if (ERouter.getInstance().isDebugEnable()) {
                ELogUtils.i(TAG, "Add interceptor '" + name + "' successfully.");
            }
        }
        return this;
    }

    /**
     * Activity切换动画
     *
     * @param enter 进入动画
     * @param exit  退出动画
     * @return 当前对象
     */
    public EPoster transition(int enter, int exit) {
        mTransEnter = enter;
        mTransExit = exit;
        if (ERouter.getInstance().isDebugEnable()) {
            ELogUtils.i(TAG, "Set animation of enter and exit are '" + enter + "' and '" + exit + "' successfully.");
        }
        return this;
    }

    /**
     * 添加Activity共享元素动画
     * <p>
     * API 16+ 有效
     *
     * @param name 共享名称
     * @param view 共享控件
     * @return 当前对象
     */
    @SuppressWarnings("unchecked")
    public EPoster animate(String name, View view) {
        // 需要动态控制数组大小，不能直接使用List或者Vector的toArray()方法（类型强制转换失败）
        if (null == mAnimArr) {
            mAnimArr = new Pair[]{Pair.create(view, name)};
        } else {
            Pair<View, String>[] temp = mAnimArr;
            // 扩容 +1
            mAnimArr = new Pair[mAnimArr.length + 1];
            // 拷贝数组
            System.arraycopy(temp, 0, mAnimArr, 0, temp.length);
            mAnimArr[mAnimArr.length - 1] = Pair.create(view, name);
        }
        if (ERouter.getInstance().isDebugEnable()) {
            ELogUtils.i(TAG, "Add shared animation '" + name + "' on '" + view + "' successfully.");
        }
        return this;
    }

    /**
     * 添加Intent flag
     *
     * @param flag Intent flag
     * @return 当前对象
     */
    public EPoster flag(int flag) {
        mFlagList.add(flag);
        if (ERouter.getInstance().isDebugEnable()) {
            ELogUtils.i(TAG, "Add flag '" + flag + "' successfully.");
        }
        return this;
    }

    /**
     * 添加Intent category
     *
     * @param category Intent category
     * @return 当前对象
     */
    public EPoster category(String category) {
        mCategoryList.add(category);
        if (ERouter.getInstance().isDebugEnable()) {
            ELogUtils.i(TAG, category + category + "' successfully.");
        }
        return this;
    }

    /**
     * 设置目标Activity的Uri
     *
     * @param uri 目标Activity的Uri
     * @return 当前对象
     */
    public EPoster uri(Uri uri) {
        mUri = uri;
        if (ERouter.getInstance().isDebugEnable()) {
            ELogUtils.i(TAG, "Set uri as '" + uri + "'.");
        }
        return this;
    }

    /**
     * 设置Activity跳转的Action
     *
     * @param action Activity跳转的Action
     * @return 当前对象
     */
    public EPoster action(String action) {
        mAction = action;
        if (ERouter.getInstance().isDebugEnable()) {
            ELogUtils.i(TAG, "Set action as '" + action + "'.");
        }
        return this;
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
        return go(mRequestCode, null);
    }

    /**
     * 转发路由
     *
     * @param requestCode 请求码
     * @param <T>         目标对象类型
     * @return 目标对象
     * <p>
     * 值：
     * Activity :: XxxxActivity.class
     * Fragment :: new XxxxFragment()
     * Service  :: XxxxService.class
     */
    public <T> T go(int requestCode) {
        return go(requestCode, null);
    }

    /**
     * 转发路由
     *
     * @param callback 回调
     * @param <T>      目标对象类型
     * @return 目标对象
     * <p>
     * 值：
     * Activity :: XxxxActivity.class
     * Fragment :: new XxxxFragment()
     * Service  :: XxxxService.class
     */
    public <T> T go(Callback callback) {
        return go(mRequestCode, callback);
    }

    /**
     * 转发路由
     *
     * @param requestCode 请求码
     * @param callback    回调
     * @param <T>         目标对象类型
     * @return 目标对象
     * <p>
     * 值：
     * Activity :: XxxxActivity.class
     * Fragment :: new XxxxFragment()
     * Service  :: XxxxService.class
     */
    public <T> T go(int requestCode, Callback callback) {
        mRequestCode = requestCode;
        mCallback = callback;

        if (ERouter.getInstance().isDebugEnable()) {
            ELogUtils.i(TAG, "Post to '" + mUrl + "' start.");
        }

        // 优先判断Uri跳转，Uri跳转的目标只有Activity
        if (null != mUri) {
            return (T) postActivity(null);
        }

        // 执行路由，先从缓存中获取，不存在再加载
        RouterMeta meta = mMetaMap.get(mUrl);
        if (null != meta) {
            return post(meta);
        }

        // 缓存中没有，加载路由
        Map<String, RouterMeta> metaMap = getMetaMap();
        if (null != metaMap) {
            return post(metaMap.get(mUrl));
        }
        return null;
    }

    /**
     * 获取当前路由上下文
     *
     * @return 当前路由上下文
     */
    public Context getContext() {
        return null != mActivity ? mActivity : null != mFragmentX ? mFragmentX.getActivity() : mService;
    }

    /**
     * 路由转发
     *
     * @param meta 路由数据
     * @param <T>  目标对象类型
     * @return 目标对象
     */
    @SuppressWarnings("unchecked")
    private <T> T post(RouterMeta meta) {
        if (null != meta) {
            // 先执行拦截器
            if (null != mInterList && !mInterList.isEmpty()) {
                loadInterceptors();
                createCurrentInterceptors();

                // 执行拦截器队列
                EInterceptor interceptor;
                for (String name : mInterList) {
                    interceptor = mInterMap.get(name);
                    if (ERouter.getInstance().isDebugEnable()) {
                        ELogUtils.i(TAG, "Execute interceptor named '" + name + "' that '" + interceptor + "'.");
                    }
                    if (interceptor.execute(this)) {
                        // 中断路由
                        if (ERouter.getInstance().isDebugEnable()) {
                            ELogUtils.i(TAG, "The interceptor named '" + name + "' that '" + interceptor + "' interrupted current router.");
                        }
                        return null;
                    }
                }
            }

            // 所有拦截器都通过后
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
                case FRAGMENT_X: {
                    Fragment fm = postFragmentX(meta);
                    return null == fm ? null : (T) fm;
                }
                case FRAGMENT: {
                    android.app.Fragment fm = postFragment(meta);
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
     * 根据设置的拦截器名称列表，创建对应的拦截器对象，并保存到Map集合中
     */
    private void createCurrentInterceptors() {
        Class<? extends EInterceptor> clazz;
        EInterceptor interceptor;
        for (String name : mInterList) {
            // 先从映射器缓存中获取到映射器
            clazz = EInterMapCache.getInstance().get(name);
            if (null != clazz) {
                try {
                    interceptor = clazz.newInstance();
                    mInterMap.put(name, interceptor);
                    if (ERouter.getInstance().isDebugEnable()) {
                        ELogUtils.i(TAG, "Load interceptor '" + name + "' that '" + interceptor + "'.");
                    }
                } catch (InstantiationException e) {
                    if (null != mCallback) {
                        mCallback.onError(this, e);
                    }
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    if (null != mCallback) {
                        mCallback.onError(this, e);
                    }
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 加载拦截器映射器，并保存到拦截器映射器缓存中
     */
    @SuppressWarnings("unchecked")
    private void loadInterceptors() {
        if (EInterMapCache.getInstance().get().isEmpty()) {
            try {
                // 加载映射器
                List<Class<?>> classList = EClassUtils.getClassListInPackage(mApp, EConsts.INTERCEPTOR_PACKAGE, EInterceptorMapper.class.getSimpleName() + EConsts.SUFFIX_INTERCEPTOR_CLASS + EConsts.SEPARATOR);
                Class<?>[] interfaces;
                EInterceptorMapper interMapper;
                // 定义接收拦截器映射关系的集合
                Map<String, Class<? extends EInterceptor>> interMap = new HashMap<>();

                for (Class<?> clazz : classList) {
                    // 由于按包名获取类，所以必定含有内部类，而内部类中没有加载路由映射的方法
                    // 所以先要判断当前class是否实现了ERouterGroupMapper接口，只有实现了该接口的类才能加载路由映射
                    interfaces = clazz.getInterfaces();
                    if (null == interfaces || interfaces.length == 0 || interfaces[0] != EInterceptorMapper.class) {
                        continue;
                    }
                    interMapper = (EInterceptorMapper) clazz.newInstance();
                    // 执行映射器的加载方法
                    interMapper.load(interMap);
                }
                // 将映射关系集合保存到缓存中
                EInterMapCache.getInstance().putAll(interMap);
            } catch (InstantiationException e) {
                if (null != mCallback) {
                    mCallback.onError(this, e);
                }
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                if (null != mCallback) {
                    mCallback.onError(this, e);
                }
                e.printStackTrace();
            }
        }
    }

    /**
     * 转发Fragment路由，创建目标Fragment实例
     *
     * @param meta 路由数据
     * @return 目标Fragment实例
     */
    private Fragment postFragmentX(RouterMeta meta) {
        try {
            Fragment fm = (Fragment) meta.getDest().newInstance();
            fm.setArguments(mParams);
            if (null != mCallback) {
                mCallback.onPosted(this);
            }
            if (ERouter.getInstance().isDebugEnable()) {
                ELogUtils.i(TAG, "Post fragment x.");
            }
            return fm;
        } catch (InstantiationException e) {
            if (null != mCallback) {
                mCallback.onError(this, e);
            }
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            if (null != mCallback) {
                mCallback.onError(this, e);
            }
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 转发Fragment路由，创建目标Fragment实例
     *
     * @param meta 路由数据
     * @return 目标Fragment实例
     */
    private android.app.Fragment postFragment(RouterMeta meta) {
        try {
            android.app.Fragment fm = (android.app.Fragment) meta.getDest().newInstance();
            fm.setArguments(mParams);
            if (null != mCallback) {
                mCallback.onPosted(this);
            }
            if (ERouter.getInstance().isDebugEnable()) {
                ELogUtils.i(TAG, "Post fragment.");
            }
            return fm;
        } catch (InstantiationException e) {
            if (null != mCallback) {
                mCallback.onError(this, e);
            }
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            if (null != mCallback) {
                mCallback.onError(this, e);
            }
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
        try {
            if (null != mContext) {
                // Context中创建服务
                intent = new Intent(mContext, meta.getDest());
                addFlags(intent);
                addCategories(intent);
                intent.putExtras(mParams);
                mContext.startService(intent);
                if (ERouter.getInstance().isDebugEnable()) {
                    ELogUtils.i(TAG, "Post to '" + mUrl + "' from '" + mContext + "'.");
                }
            } else if (null != mActivity) {
                // Activity中创建服务
                intent = new Intent(mActivity, meta.getDest());
                addFlags(intent);
                addCategories(intent);
                intent.putExtras(mParams);
                mActivity.startService(intent);
                if (ERouter.getInstance().isDebugEnable()) {
                    ELogUtils.i(TAG, "Post to '" + mUrl + "' from '" + mActivity + "'.");
                }
            } else if (null != mFragmentX) {
                // Fragment中创建服务
                intent = new Intent(mFragmentX.getActivity(), meta.getDest());
                addFlags(intent);
                addCategories(intent);
                intent.putExtras(mParams);
                if (null != mFragmentX.getActivity()) {
                    mFragmentX.getActivity().startService(intent);
                    if (ERouter.getInstance().isDebugEnable()) {
                        ELogUtils.i(TAG, "Post to '" + mUrl + "' from '" + mFragmentX + "'.");
                    }
                } else {
                    ELogUtils.e(TAG, "The activity which attached '" + mFragmentX + "' is null.");
                }
            } else if (null != mFragment) {
                // Fragment中创建服务
                intent = new Intent(mFragment.getActivity(), meta.getDest());
                addFlags(intent);
                addCategories(intent);
                intent.putExtras(mParams);
                mFragment.getActivity().startService(intent);
                if (ERouter.getInstance().isDebugEnable()) {
                    ELogUtils.i(TAG, "Post to '" + mUrl + "' from '" + mFragment + "'.");
                }
            } else if (null != mService) {
                // Service中创建服务
                intent = new Intent(mService, meta.getDest());
                addFlags(intent);
                addCategories(intent);
                intent.putExtras(mParams);
                mService.startService(intent);
                if (ERouter.getInstance().isDebugEnable()) {
                    ELogUtils.i(TAG, "Post to '" + mUrl + "' from '" + mService + "'.");
                }
            }
            // 成功转发回调
            if (null != mCallback) {
                mCallback.onPosted(this);
            }
        } catch (Exception e) {
            if (null != mCallback) {
                mCallback.onError(this, e);
            }
            e.printStackTrace();
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
        if (null == meta && null == mUri) {
            throw new UrlMatchException("Either 'url' or 'uri' is not null, but both of them are null.");
        }
        Intent intent = null;
        try {
            if (null != mContext) {
                // Activity中跳转Activity
                if (null == mUri) {
                    intent = new Intent(mContext, meta.getDest());
                } else {
                    throw new UnsupportedOperationException("Can not post uri from context.");
                }
                addFlags(intent);
                addCategories(intent);
                intent.putExtras(mParams);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mContext.startActivity(intent, null == mOptions ? null : mOptions.toBundle());
                } else {
                    mContext.startActivity(intent);
                }
            } else if (null != mActivity) {
                // Activity中跳转Activity
                if (null == mUri) {
                    intent = new Intent(mActivity, meta.getDest());
                } else {
                    intent = new Intent(mAction, mUri);
                    if (ERouter.getInstance().isDebugEnable()) {
                        ELogUtils.i(TAG, "Post to uri '" + mUri + "' from '" + mActivity + "' with action '" + mAction + "'.");
                    }
                }
                addFlags(intent);
                addCategories(intent);
                intent.putExtras(mParams);
                // 设置共享元素动画
                makeAnimate(mActivity);
                if (mRequestCode == -1) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mActivity.startActivity(intent, null == mOptions ? null : mOptions.toBundle());
                    } else {
                        mActivity.startActivity(intent);
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mActivity.startActivityForResult(intent, mRequestCode, null == mOptions ? null : mOptions.toBundle());
                    } else {
                        mActivity.startActivityForResult(intent, mRequestCode);
                    }
                }
                // 设置切换动画
                overrideTransition(mActivity);
            } else if (null != mFragmentX) {
                // Fragment中跳转Activity
                if (null == mUri) {
                    intent = new Intent(mFragmentX.getActivity(), meta.getDest());
                } else {
                    intent = new Intent(mAction, mUri);
                    if (ERouter.getInstance().isDebugEnable()) {
                        ELogUtils.i(TAG, "Post to '" + mUri.getPath() + "' from '" + mFragmentX + "' with action '" + mAction + "'.");
                    }
                }
                addFlags(intent);
                addCategories(intent);
                intent.putExtras(mParams);
                makeAnimate(mFragmentX.getActivity());
                if (mRequestCode == -1) {
                    mFragmentX.startActivity(intent, null == mOptions ? null : mOptions.toBundle());
                } else {
                    mFragmentX.startActivityForResult(intent, mRequestCode, null == mOptions ? null : mOptions.toBundle());
                }
                overrideTransition(mFragmentX.getActivity());
            } else if (null != mFragment) {
                // Fragment中跳转Activity
                if (null == mUri) {
                    intent = new Intent(mFragment.getActivity(), meta.getDest());
                } else {
                    intent = new Intent(mAction, mUri);
                    if (ERouter.getInstance().isDebugEnable()) {
                        ELogUtils.i(TAG, "Post to '" + mUri.getPath() + "' from '" + mFragment + "' with action '" + mAction + "'.");
                    }
                }
                addFlags(intent);
                addCategories(intent);
                intent.putExtras(mParams);
                makeAnimate(mFragment.getActivity());
                if (mRequestCode == -1) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mFragment.startActivity(intent, null == mOptions ? null : mOptions.toBundle());
                    } else {
                        mFragment.startActivity(intent);
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mFragment.startActivityForResult(intent, mRequestCode, null == mOptions ? null : mOptions.toBundle());
                    } else {
                        mFragment.startActivityForResult(intent, mRequestCode);
                    }
                }
                overrideTransition(mFragment.getActivity());
            } else if (null != mService) {
                // Service中跳转Activity页面
                if (null == mUri) {
                    intent = new Intent(mService, meta.getDest());
                } else {
                    intent = new Intent(mAction, mUri);
                    if (ERouter.getInstance().isDebugEnable()) {
                        ELogUtils.i(TAG, "Post to '" + mUri.getPath() + "' from '" + mService + "' with action '" + mAction + "'.");
                    }
                }
                addFlags(intent);
                addCategories(intent);
                intent.putExtras(mParams);
                mService.startActivity(intent);
            }
            // 成功转发回调
            if (null != mCallback) {
                mCallback.onPosted(this);
            }
        } catch (Exception e) {
            if (null != mCallback) {
                mCallback.onError(this, e);
            }
        }
        return intent;
    }

    /**
     * Activity切换动画
     *
     * @param activity 当前Activity
     */
    private void overrideTransition(Activity activity) {
        if (null != activity && mTransEnter > 0 && mTransExit > 0) {
            activity.overridePendingTransition(mTransEnter, mTransExit);
        }
    }

    /**
     * Activity共享元素动画
     *
     * @param activity 当前Activity
     */
    private void makeAnimate(Activity activity) {
        if (null != activity && null != mAnimArr && mAnimArr.length > 0) {
            mOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, mAnimArr);
        }
    }

    /**
     * 向Intent中添加Flag
     *
     * @param intent Intent
     */
    private void addFlags(Intent intent) {
        if (null != intent && null != mFlagList && !mFlagList.isEmpty()) {
            for (int flag : mFlagList) {
                intent.addFlags(flag);
            }
        }
    }

    /**
     * 向Intent中添加Category
     *
     * @param intent Intent
     */
    private void addCategories(Intent intent) {
        if (null != intent && null != mCategoryList && !mCategoryList.isEmpty()) {
            for (String category : mCategoryList) {
                intent.addCategory(category);
            }
        }
    }

    /**
     * 设置参数
     *
     * @param type  参数类型
     * @param name  参数名称
     * @param value 参数值
     * @param <T>   具体参数类型
     * @return 当前对象
     */
    private <T> EPoster setParam(Integer type, String name, T value) {
        if (null == type || TextUtils.isEmpty(name) || null == value) {
            return this;
        }

        if (type == TypeKind.SERIALIZABLE.ordinal()) {
            // Serializable 无法从字符串解析
            mParams.putSerializable(name, (Serializable) value);
        } else if (type == TypeKind.PARCELABLE.ordinal()) {
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
            String strVal = (value instanceof String) ? (String) value : value.toString();

            if (type == TypeKind.INT.ordinal()) {
                mParams.putInt(name, Integer.valueOf(strVal));
            } else if (type == TypeKind.BYTE.ordinal()) {
                mParams.putByte(name, Byte.valueOf(strVal));
            } else if (type == TypeKind.SHORT.ordinal()) {
                mParams.putShort(name, Short.valueOf(strVal));
            } else if (type == TypeKind.LONG.ordinal()) {
                mParams.putLong(name, Long.valueOf(strVal));
            } else if (type == TypeKind.FLOAT.ordinal()) {
                mParams.putFloat(name, Float.valueOf(strVal));
            } else if (type == TypeKind.DOUBLE.ordinal()) {
                mParams.putDouble(name, Double.valueOf(strVal));
            } else if (type == TypeKind.BOOLEAN.ordinal()) {
                mParams.putBoolean(name, Boolean.valueOf(strVal));
            } else if (type == TypeKind.STRING.ordinal()) {
                mParams.putString(name, strVal);
            } else {
                // 默认传入字符串
                mParams.putString(name, strVal);
            }
        }
        if (ERouter.getInstance().isDebugEnable()) {
            ELogUtils.i(TAG, "Add arg '" + name + "' successfully, value is '" + value + "'.");
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
    @SuppressWarnings("unchecked")
    private <T> T parseResult(RouterMeta meta) {
        if (null != meta) {
            switch (meta.getType()) {
                case ACTIVITY:
                case SERVICE: {
                    // Activity和Service都返回Xxxx.class
                    return (T) meta.getDest();
                }
                case FRAGMENT_X:
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

        try {
            // 加载当前分组对应的java类
            List<Class<?>> clazzList = EClassUtils.getClassListInPackage(mApp, EConsts.GROUP_PACKAGE, EConsts.PREFIX_OF_GROUP + EUtils.upCaseFirst(mGroup) + EConsts.SEPARATOR);
            if (null != clazzList) {
                // 缓存中不存在时再从路由映射器中获取
                metaMap = new HashMap<>();

                Class<?>[] interfaces;
                Method loadGroup;
                ERouterGroupMapper erg;
                if (ERouter.getInstance().isDebugEnable()) {
                    ELogUtils.i(TAG, clazzList.toString());
                }
                for (Class<?> clazz : clazzList) {
                    // 由于按包名获取类，所以必定含有内部类，而内部类中没有加载路由映射的方法
                    // 所以先要判断当前class是否实现了ERouterGroupMapper接口，只有实现了该接口的类才能加载路由映射
                    interfaces = clazz.getInterfaces();
                    if (null == interfaces || interfaces.length == 0 || interfaces[0] != ERouterGroupMapper.class) {
                        continue;
                    }

                    if (ERouter.getInstance().isDebugEnable()) {
                        ELogUtils.i(TAG, "Loading class '" + clazz.getName() + "' into router mapper.");
                    }

                    // 开始映射路由
                    // 获取到加载路由的方法
                    loadGroup = clazz.getDeclaredMethod(EConsts.METHOD_ROUTER_LOAD, Map.class);
                    // 创建当前分组的路由映射器对象
                    erg = (ERouterGroupMapper) clazz.newInstance();
                    // 执行映射器的加载路由方法
                    loadGroup.invoke(erg, metaMap);
                }
            }
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
        if (null != metaMap) {
            EGroupMapCache.getInstance().put(mGroup, metaMap);
        }
        return metaMap;
    }
}
