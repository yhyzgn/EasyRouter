package com.yhy.easyrouter.activity;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.yhy.easyrouter.base.BaseActivity;
import com.yhy.easyrouter.R;
import com.yhy.easyrouter.entity.Simple;
import com.yhy.easyrouter.entity.User;
import com.yhy.erouter.ERouter;
import com.yhy.erouter.annotation.Router;

import java.util.ArrayList;
import java.util.List;

@Router(url = "/activity/main")
public class MainActivity extends BaseActivity {

    private ListView lvSimples;

    private List<Simple> mSimpleList;

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
        mSimpleList.add(new Simple("Normal Activity", "/activity/normal", ""));
        mSimpleList.add(new Simple("Normal Fragment", "/activity/fragment", ""));
        mSimpleList.add(new Simple("Normal Service", "/service/normal", ""));
        mSimpleList.add(new Simple("Group Activity", "/activity/group", "acgp"));
        mSimpleList.add(new Simple("Autowired Activity", "/activity/autowried", ""));

        lvSimples.setAdapter(new SimpleAdapter());
    }

    @Override
    protected void initEvent() {
        lvSimples.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Simple simple = mSimpleList.get(position);

                if (position == 4) {
                    // 携带参数
                    User user = new User("张三", 25, "男");

                    ERouter.getInstance()
                            .with(MainActivity.this)
                            .to(simple.mGroup, simple.mUrl)
                            .param("defParam", "默认名称参数")
                            .param("changed", "修改过名称参数")
                            .param("objParam", user)
                            .go();
                } else {
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
}
