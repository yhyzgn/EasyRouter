package com.yhy.router.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-20 10:15
 * version: 1.0.0
 * desc   : 拦截器注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Interceptor {

    /**
     * 拦截器名称，唯一
     *
     * @return 拦截器名称
     */
    String name() default "";
}
