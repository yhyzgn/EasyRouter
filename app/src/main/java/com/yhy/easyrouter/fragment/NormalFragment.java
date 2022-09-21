package com.yhy.easyrouter.fragment;

import com.yhy.easyrouter.R;
import com.yhy.easyrouter.base.BaseFragment;
import com.yhy.easyrouter.entity.SerializedEntity;
import com.yhy.easyrouter.utils.ToastUtils;
import com.yhy.router.annotation.Autowired;
import com.yhy.router.annotation.Router;
import com.yhy.router.utils.LogUtils;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-17 15:08
 * version: 1.0.0
 * desc   :
 */
@Router(url = "/fragment/v4/normal")
public class NormalFragment extends BaseFragment {
    @Autowired("fmNormal")
    public String fmNormal;
    @Autowired
    private SerializedEntity serializedParam;

    @Override
    protected int getLayout() {
        return R.layout.fragment_normal;
    }

    @Override
    protected void initView() {
        LogUtils.i("NormalFragment", serializedParam);
        ToastUtils.toast(fmNormal + "");
    }
}
