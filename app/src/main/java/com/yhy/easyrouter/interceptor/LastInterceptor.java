package com.yhy.easyrouter.interceptor;

import com.yhy.easyrouter.utils.ToastUtils;
import com.yhy.router.common.Transmitter;
import com.yhy.router.interceptor.TransferInterceptor;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-20 15:12
 * version: 1.0.0
 * desc   :
 */
@com.yhy.router.annotation.Interceptor
public class LastInterceptor implements TransferInterceptor {

    @Override
    public boolean execute(Transmitter transmitter) {
        ToastUtils.toast("最后一个拦截器执行完毕");
        return false;
    }
}
