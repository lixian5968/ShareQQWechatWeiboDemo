package com.yjmfortune.app;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.tencent.mm.sdk.openapi.IWXAPI;

public class SharePopupWindow extends PopupWindow implements View.OnClickListener {

    private View mMenuView;
    private LinearLayout pop_share_pyq;
    private LinearLayout pop_share_wx;
    private LinearLayout pop_pro_close;
    private LinearLayout pop_share_wb;
    private LinearLayout pop_share_qq;
    private LinearLayout pop_share_qqkj;
    private Context ct;
    String url;
    IWXAPI api;
    String shareDescription;
    String shareTitle;
    IShareView MyShareView;

    public SharePopupWindow(Context context, String url, IWXAPI api, String shareDescription, String shareTitle) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.activity_share_popup_window, null);
        pop_share_pyq = (LinearLayout) mMenuView.findViewById(R.id.pop_share_pyq);
        pop_share_wx = (LinearLayout) mMenuView.findViewById(R.id.pop_share_wx);
        pop_pro_close = (LinearLayout) mMenuView.findViewById(R.id.pop_pro_close);
        pop_share_wb = (LinearLayout) mMenuView.findViewById(R.id.pop_share_wb);
        pop_share_qq = (LinearLayout) mMenuView.findViewById(R.id.pop_share_qq);
        pop_share_qqkj = (LinearLayout) mMenuView.findViewById(R.id.pop_share_qqkj);
        ct = context;
        MyShareView = (IShareView) ct;
        this.url = url;
        this.api = api;
        this.shareDescription = shareDescription;
        this.shareTitle = shareTitle;

        pop_pro_close.setOnClickListener(this);
        pop_share_wx.setOnClickListener(this);
        pop_share_pyq.setOnClickListener(this);
        pop_share_wb.setOnClickListener(this);
        pop_share_qq.setOnClickListener(this);
        pop_share_qqkj.setOnClickListener(this);


//        pay_popwindow_checkbox.setOnClickListener(itemsOnClick);
        //设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        //设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LayoutParams.FILL_PARENT);
        //设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        //设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        //设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.AnimBottom);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        mMenuView.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                int height = mMenuView.findViewById(R.id.pop_layout).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pop_pro_close:
                dismiss();
                break;
            case R.id.pop_share_wx:
                MyShareView.WeiXinShare(ct, api, null, shareDescription, shareTitle, url, "wx");
                dismiss();
                break;
            case R.id.pop_share_pyq:
                MyShareView.WeiXinShare(ct, api, null, shareDescription, shareTitle, url, "pyq");
                dismiss();
                break;
            case R.id.pop_share_wb:
                MyShareView.showWb(ct, url, shareDescription, shareTitle);
                dismiss();
                break;
            case R.id.pop_share_qq:
                MyShareView.QQShare(ct, url, shareDescription, shareTitle, "qq");
                dismiss();
                break;
            case R.id.pop_share_qqkj:
                MyShareView.QQShare(ct, url, shareDescription, shareTitle, "kj");
                dismiss();
                break;


        }
    }


}
