package com.yhy.easyrouter.interceptor;

import com.yhy.easyrouter.utils.ToastUtils;
import com.yhy.erouter.annotation.Interceptor;
import com.yhy.erouter.common.EPoster;
import com.yhy.erouter.interceptor.EInterceptor;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-20 15:12
 * version: 1.0.0
 * desc   :
 */
@Interceptor
public class LastInterceptor implements EInterceptor {

    @Override
    public boolean execute(EPoster poster) {
        ToastUtils.toast("最后一个拦截器执行完毕");
        return false;
    }
}
