package com.yjmfortune.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WebpageObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.BaseResponse;
import com.sina.weibo.sdk.api.share.IWeiboHandler;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;
import com.sina.weibo.sdk.utils.LogUtil;
import com.sina.weibo.sdk.utils.Utility;
import com.tencent.connect.UserInfo;
import com.tencent.connect.share.QQShare;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.yjmfortune.app.weibo.AccessTokenKeeper;
import com.yjmfortune.app.weibo.User;
import com.yjmfortune.app.weibo.UsersAPI;
import com.yjmfortune.app.wxapi.Util;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ShareActivityDemo extends AppCompatActivity implements IUiListener, IShareView, IWeiboHandler.Response {

    Context ct;

    //QQ 分享
    Tencent mTencent;
    File fYJM;

    //微博
    private IWeiboShareAPI mWeiboShareAPI = null;
    boolean isInstalledWeibo;
    int supportApiLevel;

    //微信
    IWXAPI api;


    //判断QQ 是否回掉
    private boolean QQCallBack = false;
    //判断微博   是否回掉
    private boolean WeiboCallBack = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_activity_demo);
        ct = this;
        //QQ 分享
        mTencent = Tencent.createInstance(Constants.QQShare, this.getApplicationContext());
        fYJM = new File("/sdcard/yjm.png");
        if (!fYJM.exists()) {
            Bitmap logoBitmap = BitmapFactory.decodeResource(ShareActivityDemo.this.getResources(), R.drawable.ic_logo);
            ByteArrayOutputStream logoStream = new ByteArrayOutputStream();
            boolean res = logoBitmap.compress(Bitmap.CompressFormat.PNG, 100, logoStream);
            byte[] logoBuf = logoStream.toByteArray();
            Bitmap temp = BitmapFactory.decodeByteArray(logoBuf, 0, logoBuf.length);
            saveMyBitmap("yjm", temp);
        }

        //微博
        mWeiboShareAPI = WeiboShareSDK.createWeiboAPI(this, Constants.APP_KEY);
        isInstalledWeibo = mWeiboShareAPI.isWeiboAppInstalled();
        supportApiLevel = mWeiboShareAPI.getWeiboAppSupportAPI();
        mWeiboShareAPI.registerApp();
        if (savedInstanceState != null) {
            mWeiboShareAPI.handleWeiboResponse(getIntent(), this);
        }

        //维信
        api = WXAPIFactory.createWXAPI(this, Constants.wxShare, true);
        if (!api.isWXAppInstalled()) {
            Toast.makeText(ct, "请先安装微信应用", Toast.LENGTH_SHORT).show();
        }
        if (!api.isWXAppSupportAPI()) {
            Toast.makeText(ct, "请先更新微信应用", Toast.LENGTH_SHORT).show();
        }
        api.registerApp(Constants.wxShare);


    }

    public void Share(View v) {
        String title = "涌金门-涌金门很不错，一起来买吧！";
        String Description = "我在涌金门发现了一个好项目，收益高又可靠，一起来赚钱吧~";
        String url = "http://app.yjmfortune.com/";

        //实例化SelectPicPopupWindow
        SharePopupWindow menuWindow = new SharePopupWindow(ct, url, api, Description, title);
        //显示窗口findViewById(R.id.main_demo)
        menuWindow.showAtLocation(v, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); //设置layout在PopupWindow中显示的位置
    }


    //QQ 登录 登出
    public void QQ_login(final ILoginShow view) {
        IUiListener loginListener = new IUiListener() {
            @Override
            public void onError(UiError arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onComplete(Object value) {
                // TODO Auto-generated method stub
                System.out.println("有数据返回..");
                if (value == null) {
                    return;
                }
                try {
                    JSONObject jo = (JSONObject) value;
                    int ret = jo.getInt("ret");
                    System.out.println("json=" + String.valueOf(jo));
                    if (ret == 0) {
                        Toast.makeText(ShareActivityDemo.this, "登录成功", Toast.LENGTH_LONG).show();
                        String openID = jo.getString("openid");
                        view.Show(MainActivity.QQ, "QQ用户openID=" + openID);
                        String accessToken = jo.getString("access_token");
                        String expires = jo.getString("expires_in");
                        mTencent.setOpenId(openID);
                        mTencent.setAccessToken(accessToken, expires);
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }

            @Override
            public void onCancel() {
                // TODO Auto-generated method stub

            }
        };


        if (!mTencent.isSessionValid()) {
            mTencent.login(this, "all", loginListener);
        }
        QQCallBack = true;

    }

    public void QQ_getUserInf(final ILoginShow view) {

        IUiListener userInfoListener = new IUiListener() {

            @Override
            public void onError(UiError arg0) {

            }

            @Override
            public void onComplete(Object arg0) {
                // TODO Auto-generated method stub
                if (arg0 == null) {
                    return;
                }
                try {
                    JSONObject jo = (JSONObject) arg0;
                    int ret = jo.getInt("ret");
                    System.out.println("json=" + String.valueOf(jo));
                    String nickName = jo.getString("nickname");
                    String gender = jo.getString("gender");
                    view.Show(MainActivity.QQ, "QQ用户名：" + nickName);
                    Toast.makeText(ShareActivityDemo.this.ct, "你好，" + nickName, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }

            @Override
            public void onCancel() {
            }
        };

        UserInfo userInfo = new UserInfo(ShareActivityDemo.this, mTencent.getQQToken());
        userInfo.getUserInfo(userInfoListener);
    }

    public void QQ_logout(final ILoginShow view) {
        mTencent.logout(this);
        view.Show(MainActivity.QQ, "");
    }

    //    QQ 消息回掉
    @Override
    public void onComplete(Object o) {
        MyToast(o + "QQ成功");
        Log.e("QQShare,onComplete", o + "");
    }

    @Override
    public void onError(UiError uiError) {
        Log.e("QQShare,onError", uiError.errorMessage + "");
        MyToast(uiError.errorMessage + "QQ失败");
    }

    @Override
    public void onCancel() {
        Log.e("QQShare,onCancel", "onCancel");
        MyToast("onCancel" + "QQ失败");
    }


    //微信登录
    private SsoHandler mSsoHandler;
    private AuthInfo mWeiboAuth;
    private Oauth2AccessToken weiboAccessToken;
    private UsersAPI usersAPI;


    class AuthListener implements WeiboAuthListener {
        ILoginShow view;

        public AuthListener(ILoginShow view) {
            this.view = view;
        }

        @Override
        public void onCancel() {
            MyToast("授权取消");
        }

        @Override
        public void onComplete(Bundle arg0) {
            // 获取到uid,token等信息
            weiboAccessToken = Oauth2AccessToken.parseAccessToken(arg0);
            if (weiboAccessToken.isSessionValid()) {
                // 登录成功，获取用户信息
                MyToast("授权成功,微博用户Uid=" + weiboAccessToken.getUid());
                view.Show(MainActivity.Weibo, "微博用户Uid=" + weiboAccessToken.getUid());
            } else {
                // 登录失败
                MyToast("登录失败");
                System.out.println(arg0.get("code"));
            }
        }

        @Override
        public void onWeiboException(WeiboException arg0) {
            MyToast("授权出错=" + arg0.getMessage());
        }
    }

    class WeiboRequestListener implements RequestListener {
        ILoginShow view;

        public WeiboRequestListener(ILoginShow view) {
            this.view = view;
        }

        @Override
        public void onWeiboException(WeiboException arg0) {
            MyToast("授权出错=" + arg0.getMessage());
        }

        @Override
        public void onComplete(String response) {
            if (!TextUtils.isEmpty(response)) {
                LogUtil.i("lxweibo", response);
                // 调用 User#parse 将JSON串解析成User对象
                User user = User.parse(response);
                if (user != null) {
                    Toast.makeText(ct, "获取User信息成功，用户昵称：" + user.screen_name, Toast.LENGTH_LONG).show();
                    view.Show(MainActivity.Weibo, "获取微博信息成功，用户昵称：" + user.screen_name);
                } else {
                    Toast.makeText(ct, response, Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    public void WeiBo_login(ILoginShow view) {
        mWeiboAuth = new AuthInfo(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
        mSsoHandler = new SsoHandler(ShareActivityDemo.this, mWeiboAuth);
        mSsoHandler.authorize(new AuthListener(view));
        WeiboCallBack = true;
    }

    public void WeiBo_getUserInf(ILoginShow view) {
        usersAPI = new UsersAPI(ct, Constants.APP_KEY, weiboAccessToken);
        if (weiboAccessToken != null && weiboAccessToken.getUid() != null && !weiboAccessToken.getUid().isEmpty()) {
            long uid = Long.parseLong(weiboAccessToken.getUid());
            usersAPI.show(uid, new WeiboRequestListener(view));
        }

    }

    public void WeiBo_logout(ILoginShow view) {
        AccessTokenKeeper.clear(getApplicationContext());
        weiboAccessToken = new Oauth2AccessToken();
        view.Show(MainActivity.Weibo, "");
    }


    //微博回掉
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e("wb", "onNewIntent");
        mWeiboShareAPI.handleWeiboResponse(intent, this);
    }

    @Override
    public void onResponse(BaseResponse baseResp) {
        Log.i("zhangqi", "Demo   WB Shear  Activity onResponse ");
        if (baseResp != null) {
            switch (baseResp.errCode) {
                case WBConstants.ErrorCode.ERR_OK:
                    Toast.makeText(this, "分享成功", Toast.LENGTH_LONG).show();
                    break;
                case WBConstants.ErrorCode.ERR_CANCEL:
                    Toast.makeText(this, "取消分享", Toast.LENGTH_LONG).show();
                    break;
                case WBConstants.ErrorCode.ERR_FAIL:
                    Toast.makeText(this,
                            "分享失败" + "Error Message: " + baseResp.errMsg,
                            Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }


    //微信登录
    public void WeChat_login() {
        final SendAuth.Req req = new SendAuth.Req();
        req.state = Constants.WX_APP_STATE;
        req.scope = Constants.WX_APP_SCOPE;
        api.sendReq(req);
    }


    @Override
    public void WeiXinShare(Context ct, IWXAPI api, Bitmap shareBitmap, String shareDescription, String shareTitle, String shareWebpageUrl, String type) {
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = shareWebpageUrl;
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = shareTitle;
        msg.description = shareDescription;
        Bitmap thumb;
        if (shareBitmap != null) {
            thumb = shareBitmap;
        } else {
            thumb = BitmapFactory.decodeResource(ct.getResources(), R.drawable.ic_logo);
        }
        msg.thumbData = Util.bmpToByteArray(thumb, true);
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = msg;
        if ("wx".equals(type)) {
            req.scene = SendMessageToWX.Req.WXSceneSession;
            // 调用api接口发送数据到微信
            api.sendReq(req);
        } else if ("pyq".equals(type)) {
            req.scene = SendMessageToWX.Req.WXSceneTimeline;
            // 调用api接口发送数据到微信
            api.sendReq(req);
        }
    }


    @Override
    public void QQShare(Context context, String url, String shareDescription, String shareTitle, String type) {
        final Bundle params = new Bundle();
        params.putString(QQShare.SHARE_TO_QQ_TITLE, shareTitle);
        params.putString(QQShare.SHARE_TO_QQ_SUMMARY, shareDescription);
        params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, fYJM.getPath());
        params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, url);
        if ("kj".endsWith(type)) {
            params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN);
        } else {
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
        }
        mTencent.shareToQQ(ShareActivityDemo.this, params, ShareActivityDemo.this);


    }


    @Override
    public void showWb(Context context, String url, String shareDescription, String shareTitle) {
        // 1. 初始化微博的分享消息
        WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
        weiboMessage.textObject = getTextObj(shareTitle + shareDescription);
        // 用户可以分享其它媒体资源（网页、音乐、视频、声音中的一种）
        weiboMessage.mediaObject = getWebpageObj(shareTitle, shareDescription, url, shareTitle + shareDescription);
        // 2. 初始化从第三方到微博的消息请求
        SendMultiMessageToWeiboRequest request = new SendMultiMessageToWeiboRequest();
        // 用transaction唯一标识一个请求
        request.transaction = String.valueOf(System.currentTimeMillis());
        request.multiMessage = weiboMessage;
        if (isInstalledWeibo && (supportApiLevel != -1)) {
            mWeiboShareAPI.sendRequest(ShareActivityDemo.this, request);
        } else {
            AuthInfo authInfo = new AuthInfo(this, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE);
            Oauth2AccessToken accessToken = AccessTokenKeeper.readAccessToken(getApplicationContext());
            String token = "";
            if (accessToken != null) {
                token = accessToken.getToken();
            }
            mWeiboShareAPI.sendRequest(this, request, authInfo, token, new WeiboAuthListener() {
                @Override
                public void onWeiboException(WeiboException arg0) {
                    Log.e("wbShare,onWeibo", arg0.getMessage());
                    MyToast(arg0.getMessage() + "微博失败");
                }

                @Override
                public void onComplete(Bundle bundle) {
                    // TODO Auto-generated method stub
                    Oauth2AccessToken newToken = Oauth2AccessToken.parseAccessToken(bundle);
                    AccessTokenKeeper.writeAccessToken(getApplicationContext(), newToken);
                    Log.e("wbShare,onComplete", "onAuthorizeComplete token = " + newToken.getToken());
                    MyToast("onComplete" + "微博成功");
                    //微博网页分享成功在这里查看
                }

                @Override
                public void onCancel() {
                    Log.e("wbShare,onCancel", "onCancel");
                    MyToast("onCancel" + "微博失败");
                }
            });
        }


    }


    //将图像保存到SD卡中  方面分享
    public void saveMyBitmap(String bitName, Bitmap mBitmap) {
        File f = new File("/sdcard/" + bitName + ".png");
        try {
            f.createNewFile();
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //得到微博文字描述
    private TextObject getTextObj(String text) {
        TextObject textObject = new TextObject();
        textObject.text = text;
        return textObject;
    }

    //得到微博网页描述
    private WebpageObject getWebpageObj(String title, String description, String actionUrl, String defaultText) {
        WebpageObject mediaObject = new WebpageObject();
        mediaObject.identify = Utility.generateGUID();
        mediaObject.title = title;
        mediaObject.description = description;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_logo);
        Matrix matrix = new Matrix();
        matrix.postScale(0.3f, 0.3f); //长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        mediaObject.setThumbImage(resizeBmp);
        mediaObject.actionUrl = actionUrl;
        mediaObject.defaultText = defaultText;
        return mediaObject;
    }


    //微信获取时间表戳
    public static String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (mSsoHandler != null && WeiboCallBack) {
            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
            WeiboCallBack = false;
        }

        if (mTencent != null && QQCallBack) {
            if (data != null) {
                Log.e("onActivityResult", "requestCode->" + requestCode + ",resultCode->" + resultCode + ",data->" + data);
                Tencent.onActivityResultData(requestCode, resultCode, data, this);
                QQCallBack = false;
            }
        }


    }


    public void MyToast(String s) {
        Toast.makeText(ct, s, Toast.LENGTH_SHORT).show();
    }
}


