package com.yhy.easyrouter.activity;

import android.widget.TextView;

import com.yhy.easyrouter.R;
import com.yhy.easyrouter.base.BaseActivity;
import com.yhy.easyrouter.entity.SeriaEntity;
import com.yhy.easyrouter.entity.User;
import com.yhy.erouter.ERouter;
import com.yhy.erouter.annotation.Autowired;
import com.yhy.erouter.annotation.Router;

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
    public SeriaEntity seriaParam;
    @Autowired
    private boolean boolTest;
    @Autowired
    private int intTest;
    @Autowired
    private List<SeriaEntity> listTest;

    // 不自动注入
    private String param;

    private TextView tvArgs;

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
        ERouter.getInstance().inject(this);

        StringBuilder sb = new StringBuilder();

        sb
                .append("默认参数：" + defParam).append("\n")
                .append("改变过参数：" + chgParam).append("\n")
                .append("对象参数：" + objParam).append("\n")
                .append("私有成员参数：" + privParam).append("\n")
                .append("私有对象参数：" + privObjParam).append("\n")
                .append("Serializable对象参数：" + seriaParam).append("\n")
                .append("Boolean私有参数：" + boolTest).append("\n")
                .append("Integer私有参数：" + intTest).append("\n")
                .append("List私有参数：" + listTest);

        tvArgs.setText(sb.toString());
    }
}
