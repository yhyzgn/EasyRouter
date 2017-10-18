package com.yhy.easyrouter.base;

import android.app.Service;
import android.widget.Toast;

/**
 * author : 颜洪毅
 * e-mail : yhyzgn@gmail.com
 * time   : 2017-10-18 10:59
 * version: 1.0.0
 * desc   :
 */
public abstract class BaseService extends Service {

    public void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
