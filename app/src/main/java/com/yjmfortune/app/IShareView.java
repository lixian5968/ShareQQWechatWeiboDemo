package com.yjmfortune.app;

import android.content.Context;
import android.graphics.Bitmap;

import com.tencent.mm.sdk.openapi.IWXAPI;

/**
 * Created by lixian on 2016/2/19.
 */
public interface IShareView {

    void WeiXinShare(Context ct, IWXAPI api, Bitmap shareBitmap, String shareDescription, String shareTitle, String shareWebpageUrl, String type);

    void QQShare(Context context, String url, String shareDescription, String shareTitle, String type);

    void showWb(Context context, String url, String shareDescription, String shareTitle);
}
