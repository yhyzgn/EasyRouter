package com.yhy.erouter.utils;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2018-03-09 17:10
 * version: 1.0.0
 * desc   :
 */
public class ClassUtils {

    private ClassUtils() {
        throw new UnsupportedOperationException("Can not be instantiate.");
    }

    public static List<Class<?>> getClassInPackage(Context context, String packageName) {
        return getClassInPackage(context, packageName, null);
    }

    public static List<Class<?>> getClassInPackage(Context context, String packageName, String clazzNamePrefix) {
        if (!TextUtils.isEmpty(packageName)) {
            List<Class<?>> result = new ArrayList<>();

            try {
                DexFile df = new DexFile(context.getPackageCodePath());
                Enumeration<String> entries = df.entries();

                String className;
                while (entries.hasMoreElements()) {
                    className = entries.nextElement();
                    LogUtils.i("RouterClass", className);
                    if (className.contains(TextUtils.isEmpty(clazzNamePrefix) ? packageName : packageName + "." + clazzNamePrefix)) {
                        result.add(Class.forName(className));
                    }
                }
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
