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
 * desc   :
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface Autowired {

    String value() default "";
}
