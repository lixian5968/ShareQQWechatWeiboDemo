package com.yjmfortune.app.wxapi;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.ShowMessageFromWX;
import com.tencent.mm.sdk.modelmsg.WXAppExtendObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.yjmfortune.app.Constants;
import com.yjmfortune.app.ILoginShow;
import com.yjmfortune.app.MainActivity;
import com.yjmfortune.app.R;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    private IWXAPI api;


    private static String TAG = "WXEntryActivity";
    public static BaseResp mResp = null;
    // 是否有新的认证请求
    public static boolean hasNewAuth = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entry);
        api = WXAPIFactory.createWXAPI(this, Constants.wxShare, false);
        api.handleIntent(getIntent(), this);
        Log.e("wxWXEntryActivity", "WXEntryActivity");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
        finish();
    }

    // 微信发送请求到第三方应用时，会回调到该方法
    @Override
    public void onReq(BaseReq req) {
        Log.e("wxcallback", "req");
        switch (req.getType()) {
            case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:
                goToGetMsg();
                Log.e("wxcallback", "goToGetMsg");
                break;
            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
                goToShowMsg((ShowMessageFromWX.Req) req);
                Log.e("wxcallback", "goToShowMsg");
                break;
            default:
                break;
        }
    }


    // 第三方应用发送到微信的请求处理后的响应结果，会回调到该方法
    @Override
    public void onResp(BaseResp resp) {
        int result = 0;
        Log.e("wxresp.errCode", resp.errCode + "");
        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:

                if (resp instanceof SendAuth.Resp) {
                    String code = ((SendAuth.Resp) resp).code;
                    String access_token_url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + Constants.wxShare + "&secret=" + Constants.AppSecret
                            + "&code=" + code + "&grant_type=authorization_code";
                    Log.e("wxaccess_token_url", access_token_url);
                    if (MainActivity.instance != null) {
                        ((ILoginShow) (MainActivity.instance)).GetWeChat(access_token_url);
                    }


                    result = R.string.errcode_login_success;
                } else {
                    result = R.string.errcode_success;
                }


                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                result = R.string.errcode_cancel;
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                result = R.string.errcode_deny;
                break;
            default:
                result = R.string.errcode_unknown;
                break;
        }


        Toast.makeText(this, getString(result) + "", Toast.LENGTH_LONG).show();
        finish();
    }


    private void goToGetMsg() {
        Log.e("wx", "goToGetMsg");
        Intent intent = new Intent(this, GetFromWXActivity.class);
        intent.putExtras(getIntent());
        startActivity(intent);
        finish();
    }


    private void goToShowMsg(ShowMessageFromWX.Req showReq) {

        Log.e("wx", "showReq");
        WXMediaMessage wxMsg = showReq.message;
        WXAppExtendObject obj = (WXAppExtendObject) wxMsg.mediaObject;
        StringBuffer msg = new StringBuffer(); // 组织一个待显示的消息内容
        msg.append("description: ");
        msg.append(wxMsg.description);
        msg.append("\n");
        msg.append("extInfo: ");
        msg.append(obj.extInfo);
        msg.append("\n");
        msg.append("filePath: ");
        msg.append(obj.filePath);
        Intent intent = new Intent(this, ShowFromWXActivity.class);
        intent.putExtra(Constants.STitle, wxMsg.title);
        intent.putExtra(Constants.SMessage, msg.toString());
        intent.putExtra(Constants.BAThumbData, wxMsg.thumbData);
        startActivity(intent);
        finish();
    }
}