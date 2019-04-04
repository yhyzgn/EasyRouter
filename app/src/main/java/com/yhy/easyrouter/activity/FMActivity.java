package com.yhy.easyrouter.activity;

import com.yhy.easyrouter.R;
import com.yhy.easyrouter.base.BaseActivity;
import com.yhy.erouter.ERouter;
import com.yhy.erouter.annotation.Router;
import com.yhy.fmhelper.FmHelper;

import androidx.fragment.app.Fragment;

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
        Fragment fm = ERouter.getInstance().with(this).to("/fragment/v4/normal").go();
        mHelper.open(fm);
    }
}
