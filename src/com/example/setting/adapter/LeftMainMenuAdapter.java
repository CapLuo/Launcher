package com.example.setting.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.custom.launcher.R;
import com.example.setting.util.Util;

public class LeftMainMenuAdapter extends BaseAdapter{
	private ArrayList<HashMap<String,Object>> mList;
	private Context mContext;
	private LayoutInflater listContainer;//视图容器
	private int currPosition = 0;
	
	static class ListItemView{				//自定义控件集合  
		public ImageView imgItem;  
        public TextView textItem;
        public LinearLayout layoutItem;
	}
	
	public LeftMainMenuAdapter(Context context,ArrayList<HashMap<String,Object>> list){
		this.mContext = context;
		this.mList = list;
		this.listContainer = LayoutInflater.from(context);
	}
	public int getCount() {
		if(mList!=null)
			return mList.size();
		return 0;
	}

	public Object getItem(int position) {
		if(mList!=null&&position<mList.size())
			return mList.get(position);
		return null;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		final ListItemView  listItemView;
		if (convertView == null) {
			//获取list_item布局文件的视图
			convertView = listContainer.inflate(R.layout.left_list_item, null);
			
			listItemView = new ListItemView();
			//获取控件对象
			listItemView.imgItem = (ImageView)convertView.findViewById(R.id.img_left_list_item);
			listItemView.textItem = (TextView)convertView.findViewById(R.id.text_left_list_item);
			listItemView.layoutItem = (LinearLayout)convertView.findViewById(R.id.layout_left_item);
			//设置控件集到convertView
			convertView.setTag(listItemView);
		}else {
			listItemView = (ListItemView)convertView.getTag();
		}
		
		listItemView.textItem.setText(Util.ObjToInt(mList.get(position).get("Text")));
		
		int res = Util.ObjToInt(mList.get(position).get("Resource"));
		int resFucus = Util.ObjToInt(mList.get(position).get("ResourceFucus"));
		
		if(currPosition == position){
			listItemView.textItem.setTextColor(mContext.getResources().getColor(android.R.color.white));
			if(resFucus != 0)
				listItemView.imgItem.setBackgroundResource(resFucus);
			listItemView.layoutItem.setBackgroundResource(R.drawable.list_focus);
		} else {
			listItemView.textItem.setTextColor(mContext.getResources().getColor(android.R.color.darker_gray));
			if(res != 0)
				listItemView.imgItem.setBackgroundResource(res);
			listItemView.layoutItem.setBackgroundResource(android.R.color.transparent);
		}

		return convertView;
	}
	
	public void setSelector(int position){
		currPosition = position;
	}
}
