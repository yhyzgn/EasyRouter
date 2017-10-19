package com.yhy.easyrouter.activity;

import com.yhy.easyrouter.R;
import com.yhy.easyrouter.base.BaseActivity;
import com.yhy.erouter.annotation.Autowired;
import com.yhy.erouter.annotation.Router;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-18 10:21
 * version: 1.0.0
 * desc   :
 */
@Router(url = "/activity/group", group = "acgp")
public class GroupActivity extends BaseActivity {

    @Override
    protected int getLayout() {
        return R.layout.activity_group;
    }

    @Override
    protected void initView() {
    }
}
