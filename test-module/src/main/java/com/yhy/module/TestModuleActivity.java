package com.yhy.module;

import android.os.Bundle;

import com.yhy.erouter.annotation.Router;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2018-03-09 14:58
 * version: 1.0.0
 * desc   :
 */
@Router(url = "/activity/test/module")
public class TestModuleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_module);
    }
}
