package com.yhy.easyrouter.fragment;

import com.yhy.easyrouter.R;
import com.yhy.easyrouter.base.BaseFragment;
import com.yhy.erouter.annotation.Autowired;
import com.yhy.erouter.annotation.Router;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-17 15:08
 * version: 1.0.0
 * desc   :
 */
@Router(url = "/fragment/normal")
public class NormalFragment extends BaseFragment {
    @Autowired("fmNomal")
    public String fmNomal;

    @Override
    protected int getLayout() {
        return R.layout.fragment_normal;
    }

    @Override
    protected void initView() {
    }
}
