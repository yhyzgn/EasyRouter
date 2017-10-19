package com.yhy.easyrouter.activity;

import android.widget.TextView;

import com.yhy.easyrouter.R;
import com.yhy.easyrouter.base.BaseActivity;
import com.yhy.easyrouter.entity.User;
import com.yhy.erouter.annotation.Autowired;
import com.yhy.erouter.annotation.Router;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-18 10:21
 * version: 1.0.0
 * desc   :
 */
@Router(url = "/activity/autowried")
public class AutowiredActivity extends BaseActivity {
    @Autowired
    public String defParam;
    @Autowired("changed")
    public String chgParam;
    @Autowired
    public User objParam;

    private TextView tvDef;
    private TextView tvChg;
    private TextView tvObj;

    @Override
    protected int getLayout() {
        return R.layout.activity_autowired;
    }

    @Override
    protected void initView() {
        tvDef = $(R.id.tv_def);
        tvChg = $(R.id.tv_chg);
        tvObj = $(R.id.tv_obj);
    }

    @Override
    protected void initData() {
        tvDef.setText("默认参数：" + defParam);
        tvChg.setText("改变过参数：" + chgParam);
        tvObj.setText("对象参数：" + objParam.toString());
    }
}
