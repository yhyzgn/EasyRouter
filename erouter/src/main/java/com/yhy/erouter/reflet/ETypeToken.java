package com.yhy.erouter.reflet;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2018-04-18 13:04
 * version: 1.0.0
 * desc   : 解决Json-对象转换时的泛型擦除问题
 */
public class ETypeToken<T> {
    // 具体泛型类型
    private Type type;

    protected ETypeToken() {
        type = getSuperclassTypeParameter(getClass());
    }

    /**
     * 获取泛型类型
     *
     * @return 泛型类型
     */
    public final Type getType() {
        return type;
    }

    @Override
    public final int hashCode() {
        return type.hashCode();
    }

    /**
     * 获取某个类的泛型类型
     *
     * @param subClass 具体类
     * @return 泛型类型
     */
    private static Type getSuperclassTypeParameter(Class<?> subClass) {
        Type superClass = subClass.getGenericSuperclass();
        if (superClass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameterized = (ParameterizedType) superClass;
        return ETypes.canonicalize(parameterized.getActualTypeArguments()[0]);
    }
}
