package com.yhy.easyrouter.activity;

import com.yhy.easyrouter.App;
import com.yhy.easyrouter.R;
import com.yhy.easyrouter.base.BaseActivity;
import com.yhy.easyrouter.entity.User;
import com.yhy.erouter.annotation.Router;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-18 10:21
 * version: 1.0.0
 * desc   :
 */
@Router(url = "/activity/login")
public class LoginActivity extends BaseActivity {

    @Override
    protected int getLayout() {
        return R.layout.activity_login;
    }

    @Override
    protected void initView() {
    }

    @Override
    protected void initData() {
        App.getInstance().setUser(new User("张三", 26, "男"));
    }
}
