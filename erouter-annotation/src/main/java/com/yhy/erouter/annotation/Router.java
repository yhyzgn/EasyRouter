package com.yhy.erouter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-17 10:39
 * version: 1.0.0
 * desc   : 路由注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Router {

    /**
     * 路由路径
     *
     * @return 路由路径
     */
    String url();

    /**
     * 路由分组
     *
     * @return 路由分组
     */
    String group() default "";
}
