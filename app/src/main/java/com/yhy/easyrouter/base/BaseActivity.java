package com.yhy.easyrouter.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.yhy.erouter.ERouter;

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

        ERouter.getInstance().inject(this);

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
        return (T) findViewById(id);
    }

    public void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
