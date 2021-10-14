package com.yhy.erouter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-19 10:59
 * version: 1.0.0
 * desc   : 字段值自动注入注解
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface Autowired {

    /**
     * 参数名称
     * <p>
     * 当参数名称和字段名称相同时，可不传
     *
     * @return 参数名称
     */
    String value() default "";
}
