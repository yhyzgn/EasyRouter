package com.yhy.easyrouter.activity;

import android.widget.TextView;

import com.yhy.easyrouter.R;
import com.yhy.easyrouter.base.BaseActivity;
import com.yhy.easyrouter.entity.SerializedEntity;
import com.yhy.easyrouter.entity.User;
import com.yhy.router.EasyRouter;
import com.yhy.router.annotation.Autowired;
import com.yhy.router.annotation.Router;

import java.util.List;

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
    @Autowired
    private String privParam;
    @Autowired
    private User privObjParam;
    @Autowired
    public SerializedEntity seriaParam;
    @Autowired
    private boolean boolTest;
    @Autowired
    private int intTest;
    @Autowired
    private List<SerializedEntity> listTest;

    // 不自动注入
    private String param;

    private TextView tvArgs;

    public AutowiredActivity() {
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_autowired;
    }

    @Override
    protected void initView() {
        tvArgs = $(R.id.tv_args);
    }

    @Override
    protected void initData() {
        EasyRouter.getInstance().inject(this);

        String sb = "默认参数：" + defParam + "\n" +
                "改变过参数：" + chgParam + "\n" +
                "对象参数：" + objParam + "\n" +
                "私有成员参数：" + privParam + "\n" +
                "私有对象参数：" + privObjParam + "\n" +
                "Serializable对象参数：" + seriaParam + "\n" +
                "Boolean私有参数：" + boolTest + "\n" +
                "Integer私有参数：" + intTest + "\n" +
                "List私有参数：" + listTest;

        tvArgs.setText(sb);
    }
}
