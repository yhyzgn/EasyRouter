package com.yhy.erouter.common;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static com.yhy.erouter.common.EConsts.BOOLEAN;
import static com.yhy.erouter.common.EConsts.BYTE;
import static com.yhy.erouter.common.EConsts.DOUBEL;
import static com.yhy.erouter.common.EConsts.FLOAT;
import static com.yhy.erouter.common.EConsts.INTEGER;
import static com.yhy.erouter.common.EConsts.LONG;
import static com.yhy.erouter.common.EConsts.PARCELABLE;
import static com.yhy.erouter.common.EConsts.SHORT;
import static com.yhy.erouter.common.EConsts.STRING;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-19 13:59
 * version: 1.0.0
 * desc   : Java类型判断工具
 */
public class TypeExchanger {

    private Types mTypeUtils;
    private Elements mEltUtils;
    private TypeMirror mParcelableType;

    public TypeExchanger(Types typeUtils, Elements eltUtils) {
        this.mTypeUtils = typeUtils;
        this.mEltUtils = eltUtils;

        mParcelableType = mEltUtils.getTypeElement(PARCELABLE).asType();
    }

    /**
     * 判断元素对应的类型
     *
     * @param element 元素
     * @return Java类型
     */
    public int exchange(Element element) {
        TypeMirror typeMirror = element.asType();

        // Primitive
        if (typeMirror.getKind().isPrimitive()) {
            return element.asType().getKind().ordinal();
        }

        switch (typeMirror.toString()) {
            case BYTE:
                return TypeKind.BYTE.ordinal();
            case SHORT:
                return TypeKind.SHORT.ordinal();
            case INTEGER:
                return TypeKind.INT.ordinal();
            case LONG:
                return TypeKind.LONG.ordinal();
            case FLOAT:
                return TypeKind.FLOAT.ordinal();
            case DOUBEL:
                return TypeKind.DOUBLE.ordinal();
            case BOOLEAN:
                return TypeKind.BOOLEAN.ordinal();
            case STRING:
                return TypeKind.STRING.ordinal();
            default:
                // 其他类型，包括Parcelable
                if (mTypeUtils.isSubtype(typeMirror, mParcelableType)) {
                    // Parcelable
                    return TypeKind.PARCELABLE.ordinal();
                } else {
                    // 普通对象类型
                    return TypeKind.OBJECT.ordinal();
                }
        }
    }
}
