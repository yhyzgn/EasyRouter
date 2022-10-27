package com.yhy.easyrouter.activity;

import android.view.View;

import com.yhy.easyrouter.App;
import com.yhy.easyrouter.R;
import com.yhy.easyrouter.base.BaseActivity;
import com.yhy.easyrouter.entity.User;
import com.yhy.easyrouter.utils.ToastUtils;
import com.yhy.router.EasyRouter;
import com.yhy.router.annotation.Autowired;
import com.yhy.router.annotation.Router;
import com.yhy.router.callback.Callback;
import com.yhy.router.common.Transmitter;
import com.yhy.router.utils.LogUtils;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-18 10:21
 * version: 1.0.0
 * desc   :
 */
@Router(url = "/activity/login")
public class LoginActivity extends BaseActivity {
    private View tvText;

    @Autowired
    private String nextRoute;

    @Override
    protected int getLayout() {
        return R.layout.activity_login;
    }

    @Override
    protected void initView() {
        tvText = $(R.id.tv_text);
    }

    @Override
    protected void initData() {
        EasyRouter.getInstance().inject(this);
        LogUtils.d("LoginActivity", nextRoute);
        App.getInstance().setUser(new User("张三", 26, "男"));
    }

    @Override
    protected void initEvent() {
        tvText.setOnClickListener(v -> EasyRouter.getInstance()
                .with(LoginActivity.this)
                .to(nextRoute)
                .go(new Callback() {
                    @Override
                    public void onSuccess(Transmitter transmitter) {
                        // 跳转成功，关闭该页面
                        finish();
                    }

                    @Override
                    public void onError(Transmitter transmitter, Throwable e) {
                        ToastUtils.toast("跳转失败啦");
                    }
                }));
    }
}
