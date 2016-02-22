package com.yjmfortune.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import com.sina.weibo.sdk.constant.WBConstants;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.utils.Utility;
import com.tencent.connect.share.QQShare;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXWebpageObject;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.yjmfortune.app.weibo.AccessTokenKeeper;
import com.yjmfortune.app.wxapi.Util;

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
        api = WXAPIFactory.createWXAPI(this, Constants.wxShare);

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
        if (data != null) {
            Log.e("onActivityResult", "requestCode->" + requestCode + ",resultCode->" + resultCode + ",data->" + data);
            Tencent.onActivityResultData(requestCode, resultCode, data, this);
        }
    }


    public void MyToast(String s) {
        Toast.makeText(ct, s, Toast.LENGTH_SHORT).show();
    }
}


