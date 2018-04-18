package com.yhy.erouter.reflet;

import java.io.Serializable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2018-04-18 13:14
 * version: 1.0.0
 * desc   : 处理泛型获取的工具类
 */
public class ETypes {
    static final Type[] EMPTY_TYPE_ARRAY = new Type[]{};

    /**
     * 获取具体泛型类型
     *
     * @param type 泛型类型
     * @return 带参数的泛型类型
     */
    public static Type canonicalize(Type type) {
        if (type instanceof Class) {
            Class<?> c = (Class<?>) type;
            return c.isArray() ? new GenericArrayTypeImpl(canonicalize(c.getComponentType())) : c;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType) type;
            return new ParameterizedTypeImpl(p.getOwnerType(), p.getRawType(), p.getActualTypeArguments());
        } else if (type instanceof GenericArrayType) {
            GenericArrayType g = (GenericArrayType) type;
            return new GenericArrayTypeImpl(g.getGenericComponentType());
        } else if (type instanceof WildcardType) {
            WildcardType w = (WildcardType) type;
            return new WildcardTypeImpl(w.getUpperBounds(), w.getLowerBounds());
        } else {
            // type is either serializable as-is or unsupported
            return type;
        }
    }

    /**
     * 判断两个对象是否相等
     *
     * @param a 第一个对象
     * @param b 第二个对象
     * @return 是否相等
     */
    private static boolean equal(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    /**
     * 判断两个类型是否相等
     *
     * @param a 第一个类型
     * @param b 第二个类型
     * @return 是否相等
     */
    private static boolean equals(Type a, Type b) {
        if (a == b) {
            // also handles (a == null && b == null)
            return true;

        } else if (a instanceof Class) {
            // Class already specifies equals().
            return a.equals(b);

        } else if (a instanceof ParameterizedType) {
            if (!(b instanceof ParameterizedType)) {
                return false;
            }

            ParameterizedType pa = (ParameterizedType) a;
            ParameterizedType pb = (ParameterizedType) b;
            return equal(pa.getOwnerType(), pb.getOwnerType()) && pa.getRawType().equals(pb.getRawType()) && Arrays.equals(pa.getActualTypeArguments(), pb.getActualTypeArguments());

        } else if (a instanceof GenericArrayType) {
            if (!(b instanceof GenericArrayType)) {
                return false;
            }

            GenericArrayType ga = (GenericArrayType) a;
            GenericArrayType gb = (GenericArrayType) b;
            return equals(ga.getGenericComponentType(), gb.getGenericComponentType());

        } else if (a instanceof WildcardType) {
            if (!(b instanceof WildcardType)) {
                return false;
            }

            WildcardType wa = (WildcardType) a;
            WildcardType wb = (WildcardType) b;
            return Arrays.equals(wa.getUpperBounds(), wb.getUpperBounds()) && Arrays.equals(wa.getLowerBounds(), wb.getLowerBounds());

        } else if (a instanceof TypeVariable) {
            if (!(b instanceof TypeVariable)) {
                return false;
            }
            TypeVariable<?> va = (TypeVariable<?>) a;
            TypeVariable<?> vb = (TypeVariable<?>) b;
            return va.getGenericDeclaration() == vb.getGenericDeclaration() && va.getName().equals(vb.getName());

        } else {
            // This isn't a type we support. Could be a generic array type, wildcard type, etc.
            return false;
        }
    }

    /**
     * 检查是否是原始类型
     *
     * @param type 泛型
     */
    private static void checkNotPrimitive(Type type) {
        // !(type instanceof Class<?>) 不是字节码对象
        // !((Class<?>) type).isPrimitive() 不是原始类型
        checkArgument(!(type instanceof Class<?>) || !((Class<?>) type).isPrimitive());
    }

    /**
     * 检查是否时空对象
     *
     * @param obj 对象
     * @param <T> 泛型
     * @return 对象
     */
    private static <T> T checkNotNull(T obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        return obj;
    }

    /**
     * 检查泛型参数
     *
     * @param condition 泛型参数错误条件
     */
    private static void checkArgument(boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * 获取hashCode，空对象为0
     *
     * @param o 对象
     * @return hashCode
     */
    private static int hashCodeOrZero(Object o) {
        return o != null ? o.hashCode() : 0;
    }

    /**
     * 将类型转为String
     *
     * @param type 类型
     * @return 字符串
     */
    private static String typeToString(Type type) {
        return type instanceof Class ? ((Class<?>) type).getName() : type.toString();
    }

    /**
     * 数组类型的泛型
     */
    private static final class GenericArrayTypeImpl implements GenericArrayType, Serializable {
        private final Type componentType;

        /**
         * 构造函数
         *
         * @param componentType 外层泛型类型
         */
        GenericArrayTypeImpl(Type componentType) {
            this.componentType = canonicalize(componentType);
        }

        /**
         * 获取外层泛型类型
         *
         * @return 外层泛型类型
         */
        public Type getGenericComponentType() {
            return componentType;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof GenericArrayType && ETypes.equals(this, (GenericArrayType) o);
        }

        @Override
        public int hashCode() {
            return componentType.hashCode();
        }

        @Override
        public String toString() {
            return typeToString(componentType) + "[]";
        }

        private static final long serialVersionUID = 0;
    }

    /**
     * 参数化类型的泛型
     */
    private static final class ParameterizedTypeImpl implements ParameterizedType, Serializable {
        private final Type ownerType;
        private final Type rawType;
        private final Type[] typeArguments;

        /**
         * 构造函数
         *
         * @param ownerType     包装的泛型
         * @param rawType       运行时泛型
         * @param typeArguments 泛型参数列表
         */
        ParameterizedTypeImpl(Type ownerType, Type rawType, Type... typeArguments) {
            // require an owner type if the raw type needs it
            if (rawType instanceof Class<?>) {
                Class<?> rawTypeAsClass = (Class<?>) rawType;
                boolean isStaticOrTopLevelClass = Modifier.isStatic(rawTypeAsClass.getModifiers()) || rawTypeAsClass.getEnclosingClass() == null;
                checkArgument(ownerType != null || isStaticOrTopLevelClass);
            }

            this.ownerType = ownerType == null ? null : canonicalize(ownerType);
            this.rawType = canonicalize(rawType);
            this.typeArguments = typeArguments.clone();
            for (int t = 0, length = this.typeArguments.length; t < length; t++) {
                checkNotNull(this.typeArguments[t]);
                checkNotPrimitive(this.typeArguments[t]);
                this.typeArguments[t] = canonicalize(this.typeArguments[t]);
            }
        }

        /**
         * 获取泛型参数列表
         *
         * @return 参数列表
         */
        public Type[] getActualTypeArguments() {
            return typeArguments.clone();
        }

        /**
         * 获取运行时泛型
         *
         * @return 泛型
         */
        public Type getRawType() {
            return rawType;
        }

        /**
         * 获取包装泛型
         *
         * @return 泛型
         */
        public Type getOwnerType() {
            return ownerType;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof ParameterizedType && ETypes.equals(this, (ParameterizedType) other);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(typeArguments) ^ rawType.hashCode() ^ hashCodeOrZero(ownerType);
        }

        @Override
        public String toString() {
            int length = typeArguments.length;
            if (length == 0) {
                return typeToString(rawType);
            }

            StringBuilder stringBuilder = new StringBuilder(30 * (length + 1));
            stringBuilder.append(typeToString(rawType)).append("<").append(typeToString(typeArguments[0]));
            for (int i = 1; i < length; i++) {
                stringBuilder.append(", ").append(typeToString(typeArguments[i]));
            }
            return stringBuilder.append(">").toString();
        }

        private static final long serialVersionUID = 0;
    }

    /**
     * 通配符表达式泛型
     */
    private static final class WildcardTypeImpl implements WildcardType, Serializable {
        private final Type upperBound;
        private final Type lowerBound;

        /**
         * 构造函数
         *
         * @param upperBounds 通配符上限
         * @param lowerBounds 通配符下限
         */
        WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
            checkArgument(lowerBounds.length <= 1);
            checkArgument(upperBounds.length == 1);

            if (lowerBounds.length == 1) {
                checkNotNull(lowerBounds[0]);
                checkNotPrimitive(lowerBounds[0]);
                checkArgument(upperBounds[0] == Object.class);
                this.lowerBound = canonicalize(lowerBounds[0]);
                this.upperBound = Object.class;

            } else {
                checkNotNull(upperBounds[0]);
                checkNotPrimitive(upperBounds[0]);
                this.lowerBound = null;
                this.upperBound = canonicalize(upperBounds[0]);
            }
        }

        /**
         * 获取通配符上限
         *
         * @return 通配符上限
         */
        public Type[] getUpperBounds() {
            return new Type[]{upperBound};
        }

        /**
         * 获取通配符下限
         *
         * @return 通配符下限
         */
        public Type[] getLowerBounds() {
            return lowerBound != null ? new Type[]{lowerBound} : EMPTY_TYPE_ARRAY;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof WildcardType && ETypes.equals(this, (WildcardType) other);
        }

        @Override
        public int hashCode() {
            // this equals Arrays.hashCode(getLowerBounds()) ^ Arrays.hashCode(getUpperBounds());
            return (lowerBound != null ? 31 + lowerBound.hashCode() : 1) ^ (31 + upperBound.hashCode());
        }

        @Override
        public String toString() {
            if (lowerBound != null) {
                return "? super " + typeToString(lowerBound);
            } else if (upperBound == Object.class) {
                return "?";
            } else {
                return "? extends " + typeToString(upperBound);
            }
        }

        private static final long serialVersionUID = 0;
    }
}
