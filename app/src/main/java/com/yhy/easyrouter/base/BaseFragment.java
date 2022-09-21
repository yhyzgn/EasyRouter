package com.yhy.easyrouter.base;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yhy.router.EasyRouter;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

        EasyRouter.getInstance().inject(this);

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
}
