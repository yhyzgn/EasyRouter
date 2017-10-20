package com.yhy.easyrouter.activity;

import android.widget.TextView;

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
@Router(url = "/activity/interceptor")
public class InterceptorActivity extends BaseActivity {

    private TextView tvInter;

    @Override
    protected int getLayout() {
        return R.layout.activity_interceptor;
    }

    @Override
    protected void initView() {
        tvInter = $(R.id.tv_interceptor);
    }

    @Override
    protected void initData() {
        User user = App.getInstance().getUser();
        tvInter.setText(tvInter.getText() + "\r\n" + user.toString());
    }
}
