package com.yjmfortune.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends ShareActivityDemo {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button share = (Button) findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //分享
                Share(findViewById(R.id.main_demo));
            }
        });
    }


}
