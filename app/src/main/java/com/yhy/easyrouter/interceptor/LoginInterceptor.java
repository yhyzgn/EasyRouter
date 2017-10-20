package com.yhy.easyrouter.interceptor;

import android.widget.Toast;

import com.yhy.easyrouter.App;
import com.yhy.easyrouter.entity.User;
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
            Toast.makeText(poster.getContext(), "未登录，先去登录", Toast.LENGTH_SHORT).show();

            ERouter.getInstance()
                    .with(poster.getContext())
                    .to("/activity/login")
                    .go();
            return true;
        }

        Toast.makeText(poster.getContext(), "已经登录，往下执行", Toast.LENGTH_SHORT).show();
        return false;
    }
}
