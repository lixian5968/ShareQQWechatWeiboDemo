package com.yjmfortune.app;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.yjmfortune.app.Utils.OkHttpUtils;

import org.json.JSONObject;

public class MainActivity extends ShareActivityDemo implements ILoginShow {

    TextView QQ_message;
    TextView weibo_message;
    TextView WeChat_message;

    public static final int QQ = 1;

    public static final int Weibo = 2;

    public static final int WeChat = 3;

    Context ct;

    public static MainActivity instance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ct = this;
        instance = this;
        Button share = (Button) findViewById(R.id.share);
        QQ_message = (TextView) findViewById(R.id.QQ_message);
        weibo_message = (TextView) findViewById(R.id.weibo_message);
        WeChat_message = (TextView) findViewById(R.id.WeChat_message);

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //分享
                Share(findViewById(R.id.main_demo));
            }
        });

        findViewById(R.id.QQ_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QQ_login((ILoginShow) ct);
            }
        });

        findViewById(R.id.QQ_getUserInf).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QQ_getUserInf((ILoginShow) ct);
            }
        });

        findViewById(R.id.QQ_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QQ_logout((ILoginShow) ct);
            }
        });


        findViewById(R.id.WeiBo_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WeiBo_login((ILoginShow) ct);
            }
        });

        findViewById(R.id.WeiBo_getUserInf).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WeiBo_getUserInf((ILoginShow) ct);
            }
        });

        findViewById(R.id.WeiBo_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WeiBo_logout((ILoginShow) ct);
            }
        });


        findViewById(R.id.WeChat_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WeChat_login();
            }
        });


    }


    @Override
    public void Show(int type, String s) {
        switch (type) {
            case QQ:
                QQ_message.setText(s);
                break;
            case Weibo:
                weibo_message.setText(s);
                break;
            case WeChat:
                WeChat_message.setText(s);
                break;
        }
    }

    String openid = "";
    String access_token = "";
    String refresh_token = "";

    @Override
    public void GetWeChat(String url) {
        Log.e("wxGetWeChat", url);
        OkHttpUtils.RequestCallBack<String> request = new OkHttpUtils.RequestCallBack<String>() {
            @Override
            public void onSuccess(String response) {
                Log.e("wxresponse", response);
                try {
                    JSONObject obj = new JSONObject(response);
                    openid = (String) obj.opt("openid");
                    access_token = (String) obj.opt("access_token");
                    refresh_token = (String) obj.opt("refresh_token");
                    Log.e("wxUseropenid", openid);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                CheckToken(access_token, openid);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("wxException", e.getMessage());
            }
        };
        OkHttpUtils.get(url, request);
    }

    //检查 Token
    private void CheckToken(final String access_token, final String openid) {
        String CheckTokenUrl = "https://api.weixin.qq.com/sns/auth?access_token=" + access_token + "&openid=" + openid + "";
        Log.e("wxCheckTokenUrl", CheckTokenUrl);
        OkHttpUtils.RequestCallBack<String> request = new OkHttpUtils.RequestCallBack<String>() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    if (obj.opt("errcode").toString().equals("0")) {
                        Log.e("wxCheck", "ok");
                        GetUserInfor(access_token, openid);
                    } else if (obj.opt("errcode").equals("40003")) {
                        Log.e("wxCheck", "error");
                    } else {
                        Log.e("wxCheck", "wxNodata," + response);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("wxException", e.getMessage());
            }
        };
        OkHttpUtils.get(CheckTokenUrl, request);
    }

    //获取用户信息
    public void GetUserInfor(String access_token, final String openid) {
        String UserInfo = "https://api.weixin.qq.com/sns/userinfo?access_token=" + access_token + "&openid=" + openid + "";
        Log.e("wxUserInfourl", UserInfo);
        OkHttpUtils.RequestCallBack<String> request = new OkHttpUtils.RequestCallBack<String>() {
            @Override
            public void onSuccess(String response) {
                Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
                Log.e("wxUserInfo", response);
                Show(MainActivity.WeChat, "用户的openid：" + openid + ",response" + response);
            }

            @Override
            public void onFailure(Exception e) {
            }
        };
        OkHttpUtils.get(UserInfo, request);
    }


}
