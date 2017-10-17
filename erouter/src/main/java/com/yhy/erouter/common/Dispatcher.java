package com.yhy.erouter.common;

import android.app.Activity;
import android.app.Service;
import android.support.v4.app.Fragment;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-17 19:46
 * version: 1.0.0
 * desc   :
 */
public class Dispatcher {

    private Activity mActivity;
    private Fragment mFragment;
    private Service mService;
    private String mGroup;
    private String mUrl;

    private Dispatcher(Builder builder) {
        mActivity = builder.mActivity;
        mFragment = builder.mFragment;
        mService = builder.mService;
        mGroup = builder.mGroup;
        mUrl = builder.mUrl;
    }

    public <T> T dispatch() {
        return null;
    }

    public static class Builder {
        private Activity mActivity;
        private Fragment mFragment;
        private Service mService;
        private String mGroup;
        private String mUrl;

        public Builder with(Activity activity) {
            mActivity = activity;
            return this;
        }

        public Builder with(Fragment fragment) {
            mFragment = fragment;
            return this;
        }

        public Builder with(Service service) {
            mService = service;
            return this;
        }

        public Builder target(String group, String url) {
            mGroup = group;
            mUrl = url;
            return this;
        }

        public Dispatcher build() {
            return new Dispatcher(this);
        }
    }
}
