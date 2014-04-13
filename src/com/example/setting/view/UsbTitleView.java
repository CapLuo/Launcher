package com.example.setting.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.custom.launcher.R;

public class UsbTitleView extends LinearLayout {
	private ImageButton iBtnBack;
	private TextView textLeft;
	private TextView textRight;

	public interface OnBackListener {
		public void onBack();
	}

	private OnBackListener listener;

	public void setListener(OnBackListener listener) {
		this.listener = listener;
	}

	public UsbTitleView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.usb_title_view, this,
				true);
		initView();
	}

	public UsbTitleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.usb_title_view, this,
				true);
		initView();
	}

	private OnClickListener clickListener = new OnClickListener() {

		public void onClick(View v) {
			if (listener != null)
				listener.onBack();
		}
	};

	private void initView() {
		iBtnBack = (ImageButton) findViewById(R.id.btn_usb_back);
		iBtnBack.setOnClickListener(clickListener);
		textLeft = (TextView) findViewById(R.id.text_usb_left);
		textRight = (TextView) findViewById(R.id.text_usb_right);
	}
	
	public void setLeftGone(){
		findViewById(R.id.layout_usb_left).setVisibility(View.GONE);
	}

	public void setLeftText(String text) {
		textLeft.setText(text);
	}

	public void setRightText(String text) {
		textRight.setText(text);
	}

	public void setShowBack(boolean b) {
		iBtnBack.setVisibility(b ? View.VISIBLE : View.INVISIBLE);
	}

}
