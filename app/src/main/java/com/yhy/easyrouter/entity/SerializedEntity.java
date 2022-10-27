package com.yhy.easyrouter.entity;

import androidx.annotation.NonNull;

import java.io.Serializable;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-23 8:41
 * version: 1.0.0
 * desc   :
 */
public class SerializedEntity implements Serializable {

    public String test;

    public SerializedEntity(String test) {
        this.test = test;
    }

    @NonNull
    @Override
    public String toString() {
        return "SerializedEntity{" +
                "test='" + test + '\'' +
                '}';
    }
}
