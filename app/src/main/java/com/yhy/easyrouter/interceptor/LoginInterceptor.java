package com.yhy.easyrouter.interceptor;

import com.yhy.easyrouter.App;
import com.yhy.easyrouter.entity.User;
import com.yhy.easyrouter.utils.ToastUtils;
import com.yhy.router.Router;
import com.yhy.router.common.Transmitter;
import com.yhy.router.interceptor.TransferInterceptor;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-20 10:49
 * version: 1.0.0
 * desc   :
 */
@com.yhy.router.annotation.Interceptor(name = "login")
public class LoginInterceptor implements TransferInterceptor {

    @Override
    public boolean execute(Transmitter transmitter) {
        User user = App.getInstance().getUser();
        if (null == user) {
            ToastUtils.toast("未登录，先去登录");

            Router.getInstance()
                    .with(transmitter.getContext())
                    .to("/activity/login")
                    .go();
            return true;
        }

        ToastUtils.toast("已经登录，往下执行");
        return false;
    }
}
