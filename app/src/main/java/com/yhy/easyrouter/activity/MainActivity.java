package com.yhy.easyrouter.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.yhy.easyrouter.R;
import com.yhy.easyrouter.base.BaseActivity;
import com.yhy.easyrouter.entity.SeriaEntity;
import com.yhy.easyrouter.entity.Simple;
import com.yhy.easyrouter.entity.User;
import com.yhy.easyrouter.utils.ToastUtils;
import com.yhy.erouter.ERouter;
import com.yhy.erouter.annotation.Router;
import com.yhy.erouter.callback.Callback;
import com.yhy.erouter.common.EPoster;

import java.util.ArrayList;
import java.util.List;

@Router(url = "/activity/main")
public class MainActivity extends BaseActivity {

    private ListView lvSimples;

    private List<Simple> mSimpleList;

    private TestCallback mCallback;

    @Override
    protected int getLayout() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        lvSimples = $(R.id.lv_simples);
    }

    @Override
    protected void initData() {
        mSimpleList = new ArrayList<>();
        mSimpleList.add(new Simple("普通Activity", "/activity/normal", ""));
        mSimpleList.add(new Simple("普通Fragment", "/activity/fragment", ""));
        mSimpleList.add(new Simple("普通Service", "/service/normal", ""));
        mSimpleList.add(new Simple("分组Activity", "/activity/group", "acgp"));
        mSimpleList.add(new Simple("参数自动注入Activity", "/activity/autowried", ""));
        mSimpleList.add(new Simple("拦截器Activity", "/activity/interceptor", ""));
        mSimpleList.add(new Simple("Activity切换动画", "/activity/transition", ""));
        mSimpleList.add(new Simple("Activity共享元素动画", "/activity/make/anim", ""));
        mSimpleList.add(new Simple("Uri跳转", null, null));
        mSimpleList.add(new Simple("另一个Module", "/activity/test/module", null));

        mCallback = new TestCallback();

        lvSimples.setAdapter(new SimpleAdapter());
    }

    @Override
    protected void initEvent() {
        lvSimples.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Simple simple = mSimpleList.get(position);

                if (position == 4) {
                    List<SeriaEntity> listParam = new ArrayList<>();
                    listParam.add(new SeriaEntity("list param 01"));
                    listParam.add(new SeriaEntity("list param 02"));

                    // 携带参数
                    ERouter.getInstance()
                            .with(MainActivity.this)
                            .to(simple.mUrl)
                            .param("defParam", "默认名称参数")
                            .param("changed", "修改过名称参数")
                            .param("objParam", new User("张三", 25, "男"))
                            .param("privParam", "private参数")
                            .param("privObjParam", new User("李四", 33, "女"))
                            .param("seriaParam", new SeriaEntity("test-test"))
                            .param("boolTest", true)
                            .param("intTest", 6666)
                            .param("listTest", listParam)
                            .go();
                } else if (position == 5) {
                    // 拦截器
                    ERouter.getInstance()
                            .with(MainActivity.this)
                            .to(simple.mUrl)
                            .interceptor("login")
                            .interceptor("LastInterceptor")
                            .go();
                } else if (position == 6) {
                    // 切换动画
                    ERouter.getInstance()
                            .with(MainActivity.this)
                            .to(simple.mGroup, simple.mUrl)
                            .animate(R.anim.slide_in_right, R.anim.slide_out_right)
                            .go();
                } else if (position == 7) {
                    // 共享元素动画
                    ERouter.getInstance()
                            .with(MainActivity.this)
                            .to(simple.mGroup, simple.mUrl)
                            .transition("tvAnim", view)
                            .go(mCallback); // 设置回调
                } else if (position == 8) {
                    // Uri跳转
                    ERouter.getInstance()
                            .with(MainActivity.this)
                            .uri(Uri.parse("http://www.baidu.com"))
                            .action(Intent.ACTION_VIEW)
                            .animate(R.anim.slide_in_right, R.anim.slide_out_right)
                            .go(mCallback); // 设置回调
                } else {
                    // 普通跳转
                    ERouter.getInstance()
                            .with(MainActivity.this)
                            .to(simple.mGroup, simple.mUrl)
                            .go();
                }
            }
        });
    }

    private class SimpleAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mSimpleList.size();
        }

        @Override
        public Simple getItem(int position) {
            return mSimpleList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv = new TextView(MainActivity.this);
            tv.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120));
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(18);
            tv.setTextColor(Color.parseColor("#ff4400"));
            tv.setBackgroundColor(Color.WHITE);
            tv.setText(getItem(position).mName);
            return tv;
        }
    }

    private class TestCallback implements Callback {
        @Override
        public void onPosted(EPoster poster) {
            // 路由转发成功
            ToastUtils.toast("路由转发成功");
        }

        @Override
        public void onError(EPoster poster, Throwable e) {
            // 发生错误
            e.printStackTrace();
        }
    }
}
