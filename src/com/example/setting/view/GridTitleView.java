package com.example.setting.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.custom.launcher.R;

public class GridTitleView extends LinearLayout{
	private ImageButton iBtnDelete;
	private TextView textTotal;
	private TextView textDelete;
	private TextView textCancel;
	private View layoutChange;
	private ImageButton iBtnChange;
	
	private boolean isPlayMusic = false;
	private boolean isList = true;
	
	private int count = 0;
	
	public interface GridTitleListener{
		public void onDeteleStart();
		public void onDeteleExecu();
		public void onDeteleCancel();
		public void onChanged();
	}
	private GridTitleListener listener;
	
	public void setListener(GridTitleListener listener){
		this.listener = listener;
	}
	
	public GridTitleView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.grid_title_view, this, true);
		initView();
	}
	
	public GridTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.grid_title_view, this, true);
        initView();
    }
	
	private OnClickListener clickListener = new OnClickListener() {
		
		public void onClick(View v) {
			if(listener == null)
				return;
			if (v.getId() == R.id.btn_title_delete) {
				listener.onDeteleStart();
				iBtnDelete.setVisibility(View.INVISIBLE);
				layoutChange.setVisibility(View.GONE);
				textDelete.setVisibility(View.VISIBLE);
				textCancel.setVisibility(View.VISIBLE);
			} else if (v.getId() == R.id.text_title_delete) {
				//TODO pop is delete?
				listener.onDeteleExecu();
				iBtnDelete.setVisibility(View.VISIBLE);
				layoutChange.setVisibility(isPlayMusic ? View.VISIBLE : View.GONE);
				textDelete.setVisibility(View.INVISIBLE);
				textCancel.setVisibility(View.INVISIBLE);
				textDelete.setText("Delete(0)");
				count = 0;
			} else if (v.getId() == R.id.text_title_cancle) {
				listener.onDeteleCancel();
				iBtnDelete.setVisibility(View.VISIBLE);
				layoutChange.setVisibility(isPlayMusic ? View.VISIBLE : View.GONE);
				textDelete.setVisibility(View.INVISIBLE);
				textCancel.setVisibility(View.INVISIBLE);
				textDelete.setText("Delete(0)");
				count = 0;
			} else if(v.getId() == R.id.btn_title_change){
				isList = !isList;
				if(isList){
					iBtnChange.setBackgroundResource(R.drawable.change_btn_list_selector);
				} else {
					iBtnChange.setBackgroundResource(R.drawable.change_btn_grid_selector);
				}
				listener.onChanged();
			}
			
		}
	};
	
	private void initView(){
		iBtnDelete = (ImageButton)findViewById(R.id.btn_title_delete);
		iBtnDelete.setOnClickListener(clickListener);
		
		textTotal = (TextView)findViewById(R.id.text_title_total);
		
		textDelete = (TextView)findViewById(R.id.text_title_delete);
		textDelete.setOnClickListener(clickListener);
		textDelete.setVisibility(View.INVISIBLE);
		textDelete.setText("Delete(0)");
		textDelete.setEnabled(count == 0 ? false : true);
		textDelete.setFocusable(count == 0 ? false : true);
		
		textCancel = (TextView)findViewById(R.id.text_title_cancle);
		textCancel.setOnClickListener(clickListener);
		textCancel.setVisibility(View.INVISIBLE);
		textCancel.setText("Cancel");
		
		layoutChange = findViewById(R.id.layout_title_change);
		iBtnChange = (ImageButton)findViewById(R.id.btn_title_change);
		iBtnChange.setOnClickListener(clickListener);
		layoutChange.setVisibility(View.GONE);
	}
	
	public void setPlayMusic(boolean isPlayMusic){
		this.isPlayMusic = isPlayMusic;
		layoutChange.setVisibility(isPlayMusic ? View.VISIBLE : View.GONE);
	}
	
	public void reset(){
		listener.onDeteleCancel();
		iBtnDelete.setVisibility(View.VISIBLE);
		layoutChange.setVisibility(isPlayMusic ? View.VISIBLE : View.GONE);
		textDelete.setVisibility(View.INVISIBLE);
		textCancel.setVisibility(View.INVISIBLE);
		textDelete.setText("Delete(0)");
		count = 0;
	}
	
	public void setTotal(int total){
		if(total > 1)
			textTotal.setText(total + " files in total");
		else
			textTotal.setText(total + " file in total");
	}
	
	public void plusCount(){
		textDelete.setText("Delete("+ ++count +")");
		textDelete.setEnabled(count == 0 ? false : true);
		textDelete.setFocusable(count == 0 ? false : true);
	}
	
	public void minusCount(){
		textDelete.setText("Delete("+ --count +")");
		textDelete.setEnabled(count == 0 ? false : true);
		textDelete.setFocusable(count == 0 ? false : true);
	}

	public boolean isList() {
		return isList;
	}
}
