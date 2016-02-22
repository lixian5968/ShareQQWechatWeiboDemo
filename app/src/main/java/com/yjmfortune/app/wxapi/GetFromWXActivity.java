package com.yjmfortune.app.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.tencent.mm.sdk.openapi.GetMessageFromWX;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXAppExtendObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.yjmfortune.app.Constants;
import com.yjmfortune.app.R;

public class GetFromWXActivity extends Activity {

	private static final int THUMB_SIZE = 150;

	private IWXAPI api;
	private Bundle bundle;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// acquire wxapi
		api = WXAPIFactory.createWXAPI(this, Constants.wxShare);
		bundle = getIntent().getExtras();

		setContentView(R.layout.get_from_wx);
	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		bundle = intent.getExtras();
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
			case 0x100: {
				if (resultCode == RESULT_OK) {
					final WXAppExtendObject appdata = new WXAppExtendObject();
					final String path = CameraUtil.getResultPhotoPath(this, data, "/mnt/sdcard/tencent/");
					appdata.filePath = path;
					appdata.extInfo = "this is ext info";

					final WXMediaMessage msg = new WXMediaMessage();
					msg.setThumbImage(com.yjmfortune.app.wxapi.Util.extractThumbNail(path, 150, 150, true));
					msg.title = "this is title";
					msg.description = "this is description";
					msg.mediaObject = appdata;


					GetMessageFromWX.Resp resp = new GetMessageFromWX.Resp();
					resp.transaction = getTransaction();
					resp.message = msg;

					api.sendResp(resp);
					finish();
				}
				break;
			}

			default:
				break;
		}
	}

	private String getTransaction() {
		final GetMessageFromWX.Req req = new GetMessageFromWX.Req(bundle);
		return req.transaction;
	}
}
