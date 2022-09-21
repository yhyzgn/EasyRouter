package com.yhy.easyrouter.fragment;

import com.yhy.easyrouter.R;
import com.yhy.easyrouter.base.BaseFragment;
import com.yhy.easyrouter.entity.SeriaEntity;
import com.yhy.easyrouter.utils.ToastUtils;
import com.yhy.router.annotation.Autowired;
import com.yhy.router.annotation.Router;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-17 15:08
 * version: 1.0.0
 * desc   :
 */
@Router(url = "/fragment/v4/normal")
public class NormalFragment extends BaseFragment {
    @Autowired("fmNomal")
    public String fmNomal;
    @Autowired
    private SeriaEntity seriaParam;

    @Override
    protected int getLayout() {
        return R.layout.fragment_normal;
    }

    @Override
    protected void initView() {
        ToastUtils.toast(seriaParam + "");
    }
}
