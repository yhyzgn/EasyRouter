package com.yhy.easyrouter.activity;

import com.yhy.easyrouter.R;
import com.yhy.easyrouter.base.BaseActivity;
import com.yhy.router.annotation.Router;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-18 10:21
 * version: 1.0.0
 * desc   :
 */
@Router(url = "/activity/transition")
public class TransitionActivity extends BaseActivity {

    @Override
    protected int getLayout() {
        return R.layout.activity_normal;
    }

    @Override
    protected void initView() {
    }
}
