package com.yhy.easyrouter.activity;

import android.widget.TextView;

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
@Router(url = "/activity/make/anim")
public class MakeAnimActivity extends BaseActivity {

    private TextView tvAnim;

    @Override
    protected int getLayout() {
        return R.layout.activity_make_anim;
    }

    @Override
    protected void initView() {
        tvAnim = $(R.id.tv_anim);
    }
}
