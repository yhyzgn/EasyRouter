package com.yhy.easyrouter.interceptor;

import com.yhy.easyrouter.App;
import com.yhy.easyrouter.entity.User;
import com.yhy.easyrouter.utils.ToastUtils;
import com.yhy.router.EasyRouter;
import com.yhy.router.annotation.Interceptor;
import com.yhy.router.common.Transmitter;
import com.yhy.router.interceptor.TransferInterceptor;
import com.yhy.router.utils.LogUtils;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-20 10:49
 * version: 1.0.0
 * desc   :
 */
@Interceptor(name = "login")
public class LoginInterceptor implements TransferInterceptor {

    @Override
    public boolean execute(Transmitter transmitter) {
        LogUtils.i("LoginInterceptor", "URL = " + transmitter.getUrl());
        User user = App.getInstance().getUser();
        if (null == user) {
            ToastUtils.toast("未登录，先去登录");

            EasyRouter.getInstance()
                    .with(transmitter)
                    .to("/activity/login")
                    .param("nextRoute", transmitter.getUrl())
                    .go();

            return false;
        }

        ToastUtils.toast("已经登录，往下执行");
        return true;
    }
}
