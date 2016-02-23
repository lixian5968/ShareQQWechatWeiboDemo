package com.yjmfortune.app.TestDemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.yjmfortune.app.R;
import com.yjmfortune.app.Utils.OkHttpUtils;

public class Test extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
    }

    public void TestClick(View v) {
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=wx60b442bbdbb789e4&secret=be4981df994d4a6e8f8fbce623946593&code=001f16e488784f2155133cab2bdac7ep&grant_type=authorization_code";
        OkHttpUtils.RequestCallBack<String> request = new OkHttpUtils.RequestCallBack<String>() {
            @Override
            public void onSuccess(String response) {
                Log.e("wxresponse", response);
                Toast.makeText(Test.this, response, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("wxException", e.getMessage());
            }
        };
        OkHttpUtils.get(url, request);
    }

}
