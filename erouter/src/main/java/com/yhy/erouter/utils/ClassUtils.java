package com.yhy.erouter.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import com.yhy.erouter.BuildConfig;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dalvik.system.DexFile;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2018-03-09 17:10
 * version: 1.0.0
 * desc   :
 */
public class ClassUtils {
    private static final String TAG = "ClassUtils";

    private static final String EXTRACTED_NAME_EXT = ".classes";
    private static final String EXTRACTED_SUFFIX = ".zip";

    private static final String SECONDARY_FOLDER_NAME = "code_cache" + File.separator + "secondary-dexes";

    private static final String PREFS_FILE = "multidex.version";
    private static final String KEY_DEX_NUMBER = "dex.number";

    private static final int VM_WITH_MULTIDEX_VERSION_MAJOR = 2;
    private static final int VM_WITH_MULTIDEX_VERSION_MINOR = 1;

    private ClassUtils() {
        throw new UnsupportedOperationException("Can not be instantiate.");
    }

    private static SharedPreferences getMultiDexSP(Context context) {
        return context.getSharedPreferences(PREFS_FILE, Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ? Context.MODE_PRIVATE : Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
    }


    public static List<Class<?>> getClassListInPackage(Context context, String packageName) {
        return getClassListInPackage(context, packageName, null);
    }

    public static List<Class<?>> getClassListInPackage(Context context, String packageName, String classNamePrefix) {
        if (null != context && !TextUtils.isEmpty(packageName)) {
            try {
                Set<String> classNameSet = getClassNameInPackage(context, packageName, classNamePrefix);
                if (null != classNameSet && !classNameSet.isEmpty()) {
                    List<Class<?>> classList = new ArrayList<>();
                    for (String className : classNameSet) {
                        classList.add(Class.forName(className));
                        LogUtils.i(TAG, "Loaded class '" + className + "' successful.");
                    }
                    return classList;
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Set<String> getClassNameInPackage(Context context, String packageName) throws PackageManager.NameNotFoundException, IOException, InterruptedException {
        return getClassNameInPackage(context, packageName, null);
    }

    public static Set<String> getClassNameInPackage(Context context, final String packageName, String classNamePrefix) throws PackageManager.NameNotFoundException, IOException, InterruptedException {
        if (null != context && !TextUtils.isEmpty(packageName)) {
            final Set<String> classNameSet = new HashSet<>();
            List<String> pathList = getSourcePaths(context);
            final CountDownLatch parserCdl = new CountDownLatch(pathList.size());

            // 包名+类名前缀，如：com.yhy.erouter.group.RouterGroupActivity_$$_
            final String packageClassNamePrefix = TextUtils.isEmpty(classNamePrefix) ? packageName : packageName + "." + classNamePrefix;

            for (final String path : pathList) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DexFile df = null;
                        try {
                            if (path.endsWith(EXTRACTED_SUFFIX)) {
                                df = DexFile.loadDex(path, path + ".tmp", 0);
                            } else {
                                df = new DexFile(path);
                            }

                            Enumeration<String> dexEntries = df.entries();
                            String className;
                            while (dexEntries.hasMoreElements()) {
                                className = dexEntries.nextElement();
                                if (className.startsWith(packageClassNamePrefix)) {
                                    classNameSet.add(className);
                                    LogUtils.i(TAG, "Find className: '" + className + "' in package '" + packageName + "'.");
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (null != df) {
                                try {
                                    df.close();
                                } catch (Throwable ignore) {
                                }
                            }
                            parserCdl.countDown();
                        }
                    }
                }).start();
            }
            parserCdl.await();
            return classNameSet;
        }
        return null;
    }

    public static List<String> getSourcePaths(Context context) throws PackageManager.NameNotFoundException, IOException {
        ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
        File sourceApk = new File(applicationInfo.sourceDir);

        List<String> sourcePaths = new ArrayList<>();
        // 添加默认的apk路径
        sourcePaths.add(applicationInfo.sourceDir);

        // 解压出来的文件前缀，如: test.classes
        String extractedFilePrefix = sourceApk.getName() + EXTRACTED_NAME_EXT;

        // 如果VM已经支持了MultiDex，就不要去Secondary Folder加载 Classesx.zip了，那里已经么有了
        // 通过是否存在sp中的multidex.version是不准确的，因为从低版本升级上来的用户，是包含这个sp配置的
        if (!isVMMultidexCapable()) {
            // dex文件数量
            int totalDexNumber = getMultiDexSP(context).getInt(KEY_DEX_NUMBER, 1);
            File dexDir = new File(applicationInfo.dataDir, SECONDARY_FOLDER_NAME);

            for (int secondaryNumber = 2; secondaryNumber <= totalDexNumber; secondaryNumber++) {
                // 遍历所有的dex和zip文件
                String fileName = extractedFilePrefix + secondaryNumber + EXTRACTED_SUFFIX;
                File extractedFile = new File(dexDir, fileName);
                if (extractedFile.isFile()) {
                    // 将遍历到的dex和zip文件添加到结果集中
                    sourcePaths.add(extractedFile.getAbsolutePath());
                } else {
                    throw new IOException("Missing extracted secondary dex file '" + extractedFile.getPath() + "'");
                }
            }
        }

        if (BuildConfig.DEBUG) {
            // 只有debug模式下才启用InstantRun
            sourcePaths.addAll(tryLoadInstantRunDexFile(applicationInfo));
        }
        return sourcePaths;
    }

    private static List<String> tryLoadInstantRunDexFile(ApplicationInfo applicationInfo) {
        List<String> instantRunSourcePaths = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && null != applicationInfo.splitSourceDirs) {
            // 添加所有被分割开的apk文件，针对InstantRun模式
            instantRunSourcePaths.addAll(Arrays.asList(applicationInfo.splitSourceDirs));
            LogUtils.i(TAG, "Found InstantRun support");
        } else {
            try {
                // 通过反射获取InstantRun的路径，一遍获取到该模式下所加载的类
                Class pathsByInstantRun = Class.forName("com.android.tools.fd.runtime.Paths");
                Method getDexFileDirectory = pathsByInstantRun.getMethod("getDexFileDirectory", String.class);
                String instantRunDexPath = (String) getDexFileDirectory.invoke(null, applicationInfo.packageName);

                File instantRunFilePath = new File(instantRunDexPath);
                if (instantRunFilePath.exists() && instantRunFilePath.isDirectory()) {
                    File[] dexFile = instantRunFilePath.listFiles();
                    for (File file : dexFile) {
                        if (null != file && file.exists() && file.isFile() && file.getName().endsWith(".dex")) {
                            instantRunSourcePaths.add(file.getAbsolutePath());
                        }
                    }
                    LogUtils.i(TAG, "Found InstantRun support");
                }

            } catch (Exception e) {
                LogUtils.e(TAG, "InstantRun support error, " + e.getMessage());
            }
        }
        return instantRunSourcePaths;
    }

    private static boolean isVMMultidexCapable() {
        boolean isMultidexCapable = false;
        String vmName = null;

        try {
            if (isYunOS()) {    // YunOS需要特殊判断
                vmName = "'YunOS'";
                isMultidexCapable = Integer.valueOf(System.getProperty("ro.build.version.sdk")) >= 21;
            } else {    // 非YunOS，原生Android
                vmName = "'Android'";
                String versionString = System.getProperty("java.vm.version");
                if (versionString != null) {
                    Matcher matcher = Pattern.compile("(\\d+)\\.(\\d+)(\\.\\d+)?").matcher(versionString);
                    if (matcher.matches()) {
                        try {
                            int major = Integer.parseInt(matcher.group(1));
                            int minor = Integer.parseInt(matcher.group(2));
                            isMultidexCapable = (major > VM_WITH_MULTIDEX_VERSION_MAJOR)
                                    || ((major == VM_WITH_MULTIDEX_VERSION_MAJOR)
                                    && (minor >= VM_WITH_MULTIDEX_VERSION_MINOR));
                        } catch (NumberFormatException ignore) {
                            isMultidexCapable = false;
                        }
                    }
                }
            }
        } catch (Exception ignore) {
            isMultidexCapable = false;
        }

        LogUtils.i(TAG, "VM with name " + vmName + (isMultidexCapable ? " has multidex support" : " does not have multidex support"));
        return isMultidexCapable;
    }

    /**
     * 判断系统是否为YunOS系统
     */
    private static boolean isYunOS() {
        try {
            String version = System.getProperty("ro.yunos.version");
            String vmName = System.getProperty("java.vm.name");
            return (vmName != null && vmName.toLowerCase().contains("lemur")) || (version != null && version.trim().length() > 0);
        } catch (Exception ignore) {
            return false;
        }
    }
}
