package com.yjmfortune.app.wxapi;


import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import com.yjmfortune.app.Constants;
import com.yjmfortune.app.R;

public class ShowFromWXActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.show_from_wx);
		initView();
	}

	private void initView() {

		final String title = getIntent().getStringExtra(Constants.STitle);
		final String message = getIntent().getStringExtra(Constants.SMessage);
		final byte[] thumbData = getIntent().getByteArrayExtra(Constants.BAThumbData);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setMessage(message);
		
		if (thumbData != null && thumbData.length > 0) {
			ImageView thumbIv = new ImageView(this);
			thumbIv.setImageBitmap(BitmapFactory.decodeByteArray(thumbData, 0, thumbData.length));
			builder.setView(thumbIv);
		}
		
		builder.show();
	}
}
