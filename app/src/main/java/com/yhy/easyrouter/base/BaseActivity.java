package com.yhy.easyrouter.base;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-18 9:50
 * version: 1.0.0
 * desc   :
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int layout = getLayout();
        if (layout > 0) {
            setContentView(layout);
        }

        initView();
        initData();
        initEvent();
    }

    protected abstract int getLayout();

    protected abstract void initView();

    protected void initData() {
    }

    protected void initEvent() {
    }

    public <T extends View> T $(int id) {
        return findViewById(id);
    }
}
