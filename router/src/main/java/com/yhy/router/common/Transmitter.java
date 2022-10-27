package com.yhy.router.common;

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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;

import com.yhy.router.EasyRouter;
import com.yhy.router.callback.Callback;
import com.yhy.router.expt.IllegalOperationException;
import com.yhy.router.expt.UrlMatchException;
import com.yhy.router.interceptor.TransferInterceptor;
import com.yhy.router.mapper.InterceptorMapper;
import com.yhy.router.mapper.RouterGroupMapper;
import com.yhy.router.service.AutowiredService;
import com.yhy.router.service.impl.AutowiredServiceImpl;
import com.yhy.router.utils.ClassUtils;
import com.yhy.router.utils.EUtils;
import com.yhy.router.utils.LogUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-17 19:46
 * version: 1.0.0
 * desc   : 路由转发器
 */
@SuppressWarnings("unchecked")
public class Transmitter {
    private final String TAG = getClass().getSimpleName();
    private Application mApp;
    // 当前环境
    private final Context mContext;
    private final Activity mActivity;
    private final Fragment mFragmentX;
    private final android.app.Fragment mFragment;
    private final Service mService;
    // 分组
    private String mGroup;
    // 路径
    private String mUrl;
    // 目标 Activity 的 Uri
    private Uri mUri;
    // Activity 跳转时的 Action
    private String mAction;
    // 路由参数
    private Bundle mParamBundle;
    // 拦截器名称集合
    private final List<String> mInterList;
    // 拦截器名称和实例映射集合
    private final Map<String, TransferInterceptor> mInterMap;
    // 请求码，只有 startActivityForResult 时使用
    private int mRequestCode;
    // 路由回调
    private Callback mCallback;
    // Activity 切换动画
    private int mTransEnter;
    private int mTransExit;
    // Activity 共享元素动画
    private Pair<View, String>[] mAnimArr;
    private ActivityOptionsCompat mOptions;

    private ActivityResultCallback<ActivityResult> mActivityResultCallback;

    // Intent Flag List
    private final List<Integer> mFlagList;

    // Intent Category List
    private final List<String> mCategoryList;

    private final AutowiredService mAutowiredService;

    public Transmitter(Context context) {
        this(context, null, null, null, null);
    }

    /**
     * 构造函数
     *
     * @param activity 当前Activity
     */
    public Transmitter(Activity activity) {
        this(null, activity, null, null, null);
    }

    /**
     * 构造函数
     *
     * @param fragmentX 当前Fragment
     */
    public Transmitter(Fragment fragmentX) {
        this(null, null, fragmentX, null, null);
    }

    /**
     * 构造函数
     *
     * @param fragment 当前Fragment
     */
    public Transmitter(android.app.Fragment fragment) {
        this(null, null, null, fragment, null);
    }

    /**
     * 构造函数
     *
     * @param service 当前Service
     */
    public Transmitter(Service service) {
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
    private Transmitter(Context context, Activity activity, Fragment fragmentX, android.app.Fragment fragment, Service service) {
        mContext = context;
        mActivity = activity;
        mFragmentX = fragmentX;
        mFragment = fragment;
        mService = service;

        // 初始化
        mRequestCode = -1;
        mParamBundle = new Bundle();
        mInterList = new ArrayList<>();
        mInterMap = new HashMap<>();
        mFlagList = new ArrayList<>();
        mCategoryList = new ArrayList<>();
        mAutowiredService = new AutowiredServiceImpl();
    }

    /**
     * 初始化Application
     *
     * @param app 当前Application
     * @return 当前对象
     */
    public Transmitter init(Application app) {
        mApp = app;
        return this;
    }

    /**
     * 成员自动注入入口
     *
     * @param target 当前需要自动注入的类，一般传 this 即可
     */
    public void inject(Object target) {
        // 执行自动注入
        mAutowiredService.autowired(target);
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
    public Transmitter to(String url) {
        return to(EUtils.getGroupFromUrl(url), url);
    }

    /**
     * 设置目标路径
     *
     * @param group 分组名称
     * @param url   目标路径
     * @return 当前构造器
     */
    public Transmitter to(String group, String url) {
        mGroup = TextUtils.isEmpty(group) ? EUtils.getGroupFromUrl(url) : group;
        mUrl = url;
        if (EasyRouter.getInstance().isDebugEnable()) {
            LogUtils.i(TAG, "Set url as '" + mUrl + "'.");
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
    public Transmitter param(String name, int value) {
        return setParam(TypeKind.INT.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 当前对象
     */
    public Transmitter param(String name, byte value) {
        return setParam(TypeKind.BYTE.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 当前对象
     */
    public Transmitter param(String name, short value) {
        return setParam(TypeKind.SHORT.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 当前对象
     */
    public Transmitter param(String name, boolean value) {
        return setParam(TypeKind.BOOLEAN.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 当前对象
     */
    public Transmitter param(String name, long value) {
        return setParam(TypeKind.LONG.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 当前对象
     */
    public Transmitter param(String name, float value) {
        return setParam(TypeKind.FLOAT.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 当前对象
     */
    public Transmitter param(String name, double value) {
        return setParam(TypeKind.DOUBLE.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 当前对象
     */
    public Transmitter param(String name, String value) {
        return setParam(TypeKind.STRING.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 当前对象
     */
    public Transmitter param(String name, Serializable value) {
        return setParam(TypeKind.SERIALIZABLE.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 当前对象
     */
    public Transmitter param(String name, Parcelable value) {
        return setParam(TypeKind.PARCELABLE.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 当前对象
     */
    public Transmitter param(String name, Object value) {
        return setParam(TypeKind.OBJECT.ordinal(), name, value);
    }

    /**
     * 设置参数
     *
     * @param bundle bundle参数
     * @return 当前对象
     */
    public Transmitter param(Bundle bundle) {
        if (null == mParamBundle) {
            mParamBundle = bundle;
        } else {
            mParamBundle.putAll(bundle);
        }
        return this;
    }

    /**
     * 添加拦截器
     *
     * @param name 拦截器名称
     * @return 当前对象
     */
    public Transmitter interceptor(String name) {
        if (!mInterList.contains(name)) {
            mInterList.add(name);
            if (EasyRouter.getInstance().isDebugEnable()) {
                LogUtils.i(TAG, "Add interceptor '" + name + "' successfully.");
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
    public Transmitter animate(int enter, int exit) {
        mTransEnter = enter;
        mTransExit = exit;
        if (EasyRouter.getInstance().isDebugEnable()) {
            LogUtils.i(TAG, "Set animation of enter and exit are '" + enter + "' and '" + exit + "' successfully.");
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
    public Transmitter transition(String name, View view) {
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
        if (EasyRouter.getInstance().isDebugEnable()) {
            LogUtils.i(TAG, "Add shared animation '" + name + "' on '" + view + "' successfully.");
        }
        return this;
    }

    /**
     * 添加Intent flag
     *
     * @param flag Intent flag
     * @return 当前对象
     */
    public Transmitter flag(int flag) {
        mFlagList.add(flag);
        if (EasyRouter.getInstance().isDebugEnable()) {
            LogUtils.i(TAG, "Add flag '" + flag + "' successfully.");
        }
        return this;
    }

    /**
     * 添加Intent category
     *
     * @param category Intent category
     * @return 当前对象
     */
    public Transmitter category(String category) {
        mCategoryList.add(category);
        if (EasyRouter.getInstance().isDebugEnable()) {
            LogUtils.i(TAG, category + category + "' successfully.");
        }
        return this;
    }

    /**
     * 设置目标Activity的Uri
     *
     * @param uri 目标Activity的Uri
     * @return 当前对象
     */
    public Transmitter uri(Uri uri) {
        mUri = uri;
        if (EasyRouter.getInstance().isDebugEnable()) {
            LogUtils.i(TAG, "Set uri as '" + uri + "'.");
        }
        return this;
    }

    /**
     * 设置Activity跳转的Action
     *
     * @param action Activity跳转的Action
     * @return 当前对象
     */
    public Transmitter action(String action) {
        mAction = action;
        if (EasyRouter.getInstance().isDebugEnable()) {
            LogUtils.i(TAG, "Set action as '" + action + "'.");
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
     * Activity :: XxActivity.class
     * Fragment :: new XxFragment()
     * Service  :: XxService.class
     */
    public <T> T get() {
        return getTarget();
    }

    /**
     * 转发路由
     *
     * @param <T> 目标对象类型
     * @return 目标对象
     * <p>
     * 值：
     * Activity :: XxActivity.class
     * Fragment :: new XxFragment()
     * Service  :: XxService.class
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
     * Activity :: XxActivity.class
     * Fragment :: new XxFragment()
     * Service  :: XxService.class
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
     * Activity :: XxActivity.class
     * Fragment :: new XxFragment()
     * Service  :: XxService.class
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
     * Activity :: XxActivity.class
     * Fragment :: new XxFragment()
     * Service  :: XxService.class
     */
    public <T> T go(int requestCode, Callback callback) {
        mRequestCode = requestCode;
        mCallback = callback;

        if (EasyRouter.getInstance().isDebugEnable()) {
            LogUtils.i(TAG, "Post to '" + mUrl + "' start.");
        }

        // 优先判断Uri跳转，Uri跳转的目标只有Activity
        if (null != mUri) {
            return (T) postActivity(null);
        }

        // 执行路由
        return post(getMetaMap().get(mUrl));
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
     * 获取当前路由分组
     *
     * @return 当前路由分组
     */
    public String getGroup() {
        return mGroup;
    }

    /**
     * 获取当前路由目标 url
     *
     * @return 当前路由目标 url
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     * 获取当前路由目标 uri
     *
     * @return 当前路由目标 uri
     */
    public Uri getUri() {
        return mUri;
    }

    /**
     * 获取当前路由参数列表
     *
     * @return 当前路由参数列表
     */
    public Bundle getParamBundle() {
        return mParamBundle;
    }

    /**
     * 获取当前路由的 meta 数据集
     *
     * @return 当前路由的 meta 数据集
     */
    private <T> T getTarget() {
        return parseTarget(getMetaMap().get(mUrl));
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
                TransferInterceptor interceptor;
                for (String name : mInterList) {
                    interceptor = mInterMap.get(name);
                    if (null != interceptor) {
                        if (EasyRouter.getInstance().isDebugEnable()) {
                            LogUtils.i(TAG, "Execute interceptor named '" + name + "' that '" + interceptor + "'.");
                        }
                        if (!interceptor.execute(this)) {
                            // 中断路由
                            if (EasyRouter.getInstance().isDebugEnable()) {
                                LogUtils.i(TAG, "The interceptor named '" + name + "' that '" + interceptor + "' interrupted current router.");
                            }
                            return null;
                        }
                    }
                }
            }

            // 所有拦截器都通过后
            // 针对不同的路由类型，选择对应的路由转发
            switch (meta.getType()) {
                case ACTIVITY: {
                    Intent acIntent = postActivity(meta);
                    return null == acIntent ? null : (T) acIntent;
                }
                case SERVICE: {
                    Intent svIntent = postService(meta);
                    return null == svIntent ? null : (T) svIntent;
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
        Class<? extends TransferInterceptor> clazz;
        TransferInterceptor interceptor;
        for (String name : mInterList) {
            // 先从映射器缓存中获取到映射器
            clazz = InterMapCache.getInstance().get(name);
            if (null != clazz) {
                try {
                    interceptor = clazz.newInstance();

                    // 可能需要的参数注入
                    inject(interceptor);
                    // 缓存之
                    mInterMap.put(name, interceptor);
                    if (EasyRouter.getInstance().isDebugEnable()) {
                        LogUtils.i(TAG, "Load interceptor '" + name + "' that '" + interceptor + "'.");
                    }
                } catch (InstantiationException | IllegalAccessException e) {
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
    private void loadInterceptors() {
        if (InterMapCache.getInstance().get().isEmpty()) {
            try {
                // 加载映射器
                List<Class<?>> classList = ClassUtils.getClassListInPackage(mApp, Constant.INTERCEPTOR_PACKAGE, InterceptorMapper.class.getSimpleName() + Constant.SUFFIX_INTERCEPTOR_CLASS + Constant.SEPARATOR);
                Class<?>[] interfaces;
                InterceptorMapper interMapper;
                // 定义接收拦截器映射关系的集合
                Map<String, Class<? extends TransferInterceptor>> interMap = new HashMap<>();

                for (Class<?> clazz : classList) {
                    // 由于按包名获取类，所以必定含有内部类，而内部类中没有加载路由映射的方法
                    // 所以先要判断当前class是否实现了RouterGroupMapper接口，只有实现了该接口的类才能加载路由映射
                    interfaces = clazz.getInterfaces();
                    if (interfaces.length == 0 || interfaces[0] != InterceptorMapper.class) {
                        continue;
                    }
                    interMapper = (InterceptorMapper) clazz.newInstance();
                    // 执行映射器的加载方法
                    interMapper.load(interMap);
                }
                // 将映射关系集合保存到缓存中
                InterMapCache.getInstance().putAll(interMap);
            } catch (InstantiationException | IllegalAccessException e) {
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
            fm.setArguments(mParamBundle);
            if (null != mCallback) {
                mCallback.onSuccess(this);
            }
            if (EasyRouter.getInstance().isDebugEnable()) {
                LogUtils.i(TAG, "Post fragment x.");
            }
            return fm;
        } catch (InstantiationException | IllegalAccessException e) {
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
            fm.setArguments(mParamBundle);
            if (null != mCallback) {
                mCallback.onSuccess(this);
            }
            if (EasyRouter.getInstance().isDebugEnable()) {
                LogUtils.i(TAG, "Post fragment.");
            }
            return fm;
        } catch (InstantiationException | IllegalAccessException e) {
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
                intent.putExtras(mParamBundle);
                mContext.startService(intent);
                if (EasyRouter.getInstance().isDebugEnable()) {
                    LogUtils.i(TAG, "Post to '" + mUrl + "' from '" + mContext + "'.");
                }
            } else if (null != mActivity) {
                // Activity中创建服务
                intent = new Intent(mActivity, meta.getDest());
                addFlags(intent);
                addCategories(intent);
                intent.putExtras(mParamBundle);
                mActivity.startService(intent);
                if (EasyRouter.getInstance().isDebugEnable()) {
                    LogUtils.i(TAG, "Post to '" + mUrl + "' from '" + mActivity + "'.");
                }
            } else if (null != mFragmentX) {
                // Fragment中创建服务
                intent = new Intent(mFragmentX.getActivity(), meta.getDest());
                addFlags(intent);
                addCategories(intent);
                intent.putExtras(mParamBundle);
                if (null != mFragmentX.getActivity()) {
                    mFragmentX.getActivity().startService(intent);
                    if (EasyRouter.getInstance().isDebugEnable()) {
                        LogUtils.i(TAG, "Post to '" + mUrl + "' from '" + mFragmentX + "'.");
                    }
                } else {
                    LogUtils.e(TAG, "The activity which attached '" + mFragmentX + "' is null.");
                }
            } else if (null != mFragment) {
                // Fragment中创建服务
                intent = new Intent(mFragment.getActivity(), meta.getDest());
                addFlags(intent);
                addCategories(intent);
                intent.putExtras(mParamBundle);
                mFragment.getActivity().startService(intent);
                if (EasyRouter.getInstance().isDebugEnable()) {
                    LogUtils.i(TAG, "Post to '" + mUrl + "' from '" + mFragment + "'.");
                }
            } else if (null != mService) {
                // Service中创建服务
                intent = new Intent(mService, meta.getDest());
                addFlags(intent);
                addCategories(intent);
                intent.putExtras(mParamBundle);
                mService.startService(intent);
                if (EasyRouter.getInstance().isDebugEnable()) {
                    LogUtils.i(TAG, "Post to '" + mUrl + "' from '" + mService + "'.");
                }
            }
            // 成功转发回调
            if (null != mCallback) {
                mCallback.onSuccess(this);
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
                intent.putExtras(mParamBundle);

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
                    if (EasyRouter.getInstance().isDebugEnable()) {
                        LogUtils.i(TAG, "Post to uri '" + mUri + "' from '" + mActivity + "' with action '" + mAction + "'.");
                    }
                }
                addFlags(intent);
                addCategories(intent);
                intent.putExtras(mParamBundle);
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
                    if (EasyRouter.getInstance().isDebugEnable()) {
                        LogUtils.i(TAG, "Post to '" + mUri.getPath() + "' from '" + mFragmentX + "' with action '" + mAction + "'.");
                    }
                }
                addFlags(intent);
                addCategories(intent);
                intent.putExtras(mParamBundle);
                makeAnimate(mFragmentX.getActivity());
                if (mRequestCode == -1) {
                    mFragmentX.startActivity(intent, null == mOptions ? null : mOptions.toBundle());
                } else {
                    mFragmentX.startActivityForResult(intent, mRequestCode, null == mOptions ? null : mOptions.toBundle());
                    // TODO 可升级为以下形式
                    // mFragmentX.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    //     if (null != mActivityResultCallback) {
                    //         mActivityResultCallback.onActivityResult(result);
                    //     }
                    // });
                }
                overrideTransition(mFragmentX.getActivity());
            } else if (null != mFragment) {
                // Fragment中跳转Activity
                if (null == mUri) {
                    intent = new Intent(mFragment.getActivity(), meta.getDest());
                } else {
                    intent = new Intent(mAction, mUri);
                    if (EasyRouter.getInstance().isDebugEnable()) {
                        LogUtils.i(TAG, "Post to '" + mUri.getPath() + "' from '" + mFragment + "' with action '" + mAction + "'.");
                    }
                }
                addFlags(intent);
                addCategories(intent);
                intent.putExtras(mParamBundle);
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
                    if (EasyRouter.getInstance().isDebugEnable()) {
                        LogUtils.i(TAG, "Post to '" + mUri.getPath() + "' from '" + mService + "' with action '" + mAction + "'.");
                    }
                }
                addFlags(intent);
                addCategories(intent);
                intent.putExtras(mParamBundle);
                mService.startActivity(intent);
            }
            // 成功转发回调
            if (null != mCallback) {
                mCallback.onSuccess(this);
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
    private <T> Transmitter setParam(Integer type, String name, T value) {
        if (null == type || TextUtils.isEmpty(name) || null == value) {
            return this;
        }

        if (type == TypeKind.SERIALIZABLE.ordinal()) {
            // Serializable 无法从字符串解析
            mParamBundle.putSerializable(name, (Serializable) value);
        } else if (type == TypeKind.PARCELABLE.ordinal()) {
            // Parcelable 无法从字符串解析
            mParamBundle.putParcelable(name, (Parcelable) value);
        } else if (type == TypeKind.OBJECT.ordinal()) {
            // 将对象转换为Json传递
            JsonConverter jsonConverter = EasyRouter.getInstance().getJsonConverter();
            if (null == jsonConverter) {
                throw new IllegalOperationException("If you want to use EJsonParser, must set EJsonParser in initialization of Router!");
            }
            mParamBundle.putString(name, jsonConverter.toJson(value));
        } else {
            String strVal = (value instanceof String) ? (String) value : value.toString();

            if (type == TypeKind.INT.ordinal()) {
                mParamBundle.putInt(name, Integer.parseInt(strVal));
            } else if (type == TypeKind.BYTE.ordinal()) {
                mParamBundle.putByte(name, Byte.parseByte(strVal));
            } else if (type == TypeKind.SHORT.ordinal()) {
                mParamBundle.putShort(name, Short.parseShort(strVal));
            } else if (type == TypeKind.LONG.ordinal()) {
                mParamBundle.putLong(name, Long.parseLong(strVal));
            } else if (type == TypeKind.FLOAT.ordinal()) {
                mParamBundle.putFloat(name, Float.parseFloat(strVal));
            } else if (type == TypeKind.DOUBLE.ordinal()) {
                mParamBundle.putDouble(name, Double.parseDouble(strVal));
            } else if (type == TypeKind.BOOLEAN.ordinal()) {
                mParamBundle.putBoolean(name, Boolean.parseBoolean(strVal));
            } else if (type == TypeKind.STRING.ordinal()) {
                mParamBundle.putString(name, strVal);
            } else {
                // 默认传入字符串
                mParamBundle.putString(name, strVal);
            }
        }
        if (EasyRouter.getInstance().isDebugEnable()) {
            LogUtils.i(TAG, "Add arg '" + name + "' successfully, value is '" + value + "'.");
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
     * Activity :: XxActivity.class
     * Fragment :: new XxFragment()
     * Service  :: XxService.class
     */
    @SuppressWarnings("unchecked")
    private <T> T parseTarget(RouterMeta meta) {
        if (null != meta) {
            switch (meta.getType()) {
                case ACTIVITY:
                case SERVICE: {
                    // Activity 和 Service 都返回 Xx.class
                    return (T) meta.getDest();
                }
                case FRAGMENT_X:
                case FRAGMENT: {
                    // Fragment 返回 new XxFragment()
                    try {
                        return (T) meta.getDest().newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
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
        Map<String, RouterMeta> metaMap = GroupMapCache.getInstance().get(mGroup);
        if (null != metaMap) {
            return metaMap;
        }

        try {
            // 加载当前分组对应的java类
            List<Class<?>> clazzList = ClassUtils.getClassListInPackage(mApp, Constant.GROUP_PACKAGE, Constant.PREFIX_OF_GROUP + EUtils.upCaseFirst(mGroup) + Constant.SEPARATOR);
            if (null != clazzList) {
                // 缓存中不存在时再从路由映射器中获取
                metaMap = new HashMap<>();

                Class<?>[] interfaces;
                Method loadGroup;
                RouterGroupMapper erg;
                if (EasyRouter.getInstance().isDebugEnable()) {
                    LogUtils.i(TAG, clazzList.toString());
                }
                for (Class<?> clazz : clazzList) {
                    // 由于按包名获取类，所以必定含有内部类，而内部类中没有加载路由映射的方法
                    // 所以先要判断当前class是否实现了RouterGroupMapper接口，只有实现了该接口的类才能加载路由映射
                    interfaces = clazz.getInterfaces();
                    if (interfaces.length == 0 || interfaces[0] != RouterGroupMapper.class) {
                        continue;
                    }

                    if (EasyRouter.getInstance().isDebugEnable()) {
                        LogUtils.i(TAG, "Loading class '" + clazz.getName() + "' into router mapper.");
                    }

                    // 开始映射路由
                    // 获取到加载路由的方法
                    loadGroup = clazz.getDeclaredMethod(Constant.METHOD_ROUTER_LOAD, Map.class);
                    // 创建当前分组的路由映射器对象
                    erg = (RouterGroupMapper) clazz.newInstance();
                    // 执行映射器的加载路由方法
                    loadGroup.invoke(erg, metaMap);
                }
            }
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            e.printStackTrace();
        }
        // 存放到缓存中
        if (null != metaMap) {
            GroupMapCache.getInstance().put(mGroup, metaMap);
        }
        return metaMap;
    }
}
