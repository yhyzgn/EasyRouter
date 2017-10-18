package com.yhy.easyrouter.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-18 10:49
 * version: 1.0.0
 * desc   :
 */
public abstract class BaseFragment extends Fragment {

    public Activity mActivity;
    private View mView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        int layout = getLayout();
        if (layout > 0) {
            mView = inflater.inflate(layout, container, false);
        }
        initView();
        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
        initEvent();
    }

    protected abstract int getLayout();

    protected abstract void initView();

    protected void initData() {
    }

    protected void initEvent() {
    }

    public <T extends View> T $(int id) {
        return mView.findViewById(id);
    }

    public void toast(String text) {
        Toast.makeText(mActivity, text, Toast.LENGTH_SHORT).show();
    }
}
