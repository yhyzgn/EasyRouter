package com.yhy.easyrouter.interceptor;

import com.yhy.easyrouter.App;
import com.yhy.easyrouter.entity.User;
import com.yhy.easyrouter.utils.ToastUtils;
import com.yhy.erouter.ERouter;
import com.yhy.erouter.annotation.Interceptor;
import com.yhy.erouter.common.EPoster;
import com.yhy.erouter.interceptor.EInterceptor;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-20 10:49
 * version: 1.0.0
 * desc   :
 */
@Interceptor(name = "login")
public class LoginInterceptor implements EInterceptor {

    @Override
    public boolean execute(EPoster poster) {
        User user = App.getInstance().getUser();
        if (null == user) {
            ToastUtils.toast("未登录，先去登录");

            ERouter.getInstance()
                    .with(poster.getContext())
                    .to("/activity/login")
                    .go();
            return true;
        }

        ToastUtils.toast("已经登录，往下执行");
        return false;
    }
}
