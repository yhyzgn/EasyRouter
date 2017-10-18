package com.yhy.easyrouter.activity;

import android.support.v4.app.Fragment;

import com.yhy.easyrouter.base.BaseActivity;
import com.yhy.easyrouter.R;
import com.yhy.erouter.ERouter;
import com.yhy.erouter.annotation.Router;
import com.yhy.fmhelper.FmHelper;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-18 10:40
 * version: 1.0.0
 * desc   :
 */
@Router(url = "/activity/fragment")
public class FMActivity extends BaseActivity {

    private FmHelper mHelper;

    @Override
    protected int getLayout() {
        return R.layout.activity_detail;
    }

    @Override
    protected void initView() {
        mHelper = new FmHelper.Builder(this, R.id.fl_content).build();
        Fragment fm = ERouter.getInstance().from(this).to("/fragment/normal").go();
        mHelper.open(fm);
    }
}
