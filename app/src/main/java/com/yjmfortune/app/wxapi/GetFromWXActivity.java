package com.yjmfortune.app.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
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

                    Toast.makeText(GetFromWXActivity.this, "onActivityResult" + data.toString(), Toast.LENGTH_SHORT).show();
                }
                break;
            }

            default:
                break;
        }
    }

}
