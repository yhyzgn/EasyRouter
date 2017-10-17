package com.yhy.easyrouter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.yhy.erouter.annotation.Router;

@Router(url = "/activitiy/main")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
