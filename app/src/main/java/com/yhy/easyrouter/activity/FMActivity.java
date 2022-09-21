package com.yhy.easyrouter.activity;

import androidx.fragment.app.Fragment;

import com.yhy.easyrouter.R;
import com.yhy.easyrouter.base.BaseActivity;
import com.yhy.easyrouter.entity.SerializedEntity;
import com.yhy.fmhelper.FmHelper;
import com.yhy.router.EasyRouter;
import com.yhy.router.annotation.Router;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-18 10:40
 * version: 1.0.0
 * desc   :
 */
@Router(url = "/activity/fragment")
public class FMActivity extends BaseActivity {

    @Override
    protected int getLayout() {
        return R.layout.activity_detail;
    }

    @Override
    protected void initView() {
        FmHelper mHelper = new FmHelper.Builder(this, R.id.fl_content).build();
        Fragment fm = EasyRouter.getInstance()
                .with(this)
                .to("/fragment/v4/normal")
                .param("fmNormal", "FM传参")
                .param("serializedParam", new SerializedEntity("复杂对象传参"))
                .go();
        mHelper.open(fm);
    }
}
