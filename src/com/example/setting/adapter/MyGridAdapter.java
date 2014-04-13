package com.example.setting.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.custom.launcher.R;

public class MyGridAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<MyMedia> mList;

	private boolean isDelete = false;
	private boolean isPlayMusic = false;
	private boolean isList = false;

	static class ListItemView {
		public ImageView imgLogo;
		public TextView textInfo;
		public ImageView imgLogoPlay;
		public ImageView imgCheck;
		public TextView textTotal;
	}

	public MyGridAdapter(Context context, List<MyMedia> list) {
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.mList = list;
	}

	public int getCount() {
		if (mList != null)
			return mList.size();
		return 0;
	}

	public Object getItem(int position) {
		if (mList != null && position < mList.size())
			return mList.get(position);
		return null;
	}

	public long getItemId(int position) {
		return position;
	}

	public boolean isDelete() {
		return isDelete;
	}

	public void setDelete(boolean isDelete) {
		this.isDelete = isDelete;
	}

	public boolean isPlayMusic() {
		return isPlayMusic;
	}

	public void setPlayMusic(boolean isPlayMusic) {
		this.isPlayMusic = isPlayMusic;
	}

	public boolean isList() {
		return isList;
	}

	public void setList(boolean isList) {
		this.isList = isList;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		final ListItemView listItemView;
		if (convertView == null) {
			if(isList)
				convertView = mInflater.inflate(R.layout.grid_list_item, null);
			else
				convertView = mInflater.inflate(R.layout.grid_item, null);

			listItemView = new ListItemView();
			listItemView.imgLogo = (ImageView) convertView
					.findViewById(R.id.img_logo);
			listItemView.imgLogoPlay = (ImageView) convertView
					.findViewById(R.id.img_logo_play);
			listItemView.textInfo = (TextView) convertView
					.findViewById(R.id.text_info);
			listItemView.textTotal = (TextView) convertView
					.findViewById(R.id.text_item_total);
			listItemView.imgCheck = (ImageView) convertView
					.findViewById(R.id.img_delete_check);
			convertView.setTag(listItemView);
		} else {
			listItemView = (ListItemView) convertView.getTag();
		}

		MyMedia myMedia = mList.get(position);
		Object img = myMedia.getImage();
		if (img != null) {
			if (img instanceof Integer)
				listItemView.imgLogo.setBackgroundResource((Integer) img);
			else if (img instanceof Drawable)
				listItemView.imgLogo.setBackgroundDrawable((Drawable) img);
		}

		// video file show logo play
		Integer type = myMedia.getMediaType();
		if (type == MyMedia.TYPE_VIDEO) {
			listItemView.imgLogoPlay.setVisibility(View.VISIBLE);
			listItemView.textTotal.setVisibility(View.INVISIBLE);
		} else if (type == MyMedia.TYPE_DIR || type == MyMedia.TYPE_ALL) {
			listItemView.textTotal.setText(myMedia.getTotal()
					+ " in total");
			listItemView.textTotal.setVisibility(View.VISIBLE);
			listItemView.imgLogoPlay.setVisibility(View.GONE);
		} else {
			listItemView.imgLogoPlay.setVisibility(View.GONE);
			if (isPlayMusic && !isList && (type == MyMedia.TYPE_MUSIC || type == MyMedia.TYPE_MUSIC_N)) {
				listItemView.textTotal.setText(convertTime(myMedia.getDuration()));
				listItemView.textTotal.setVisibility(View.VISIBLE);
			} else
				listItemView.textTotal.setVisibility(View.INVISIBLE);
		}

		if (isDelete) {
			if (myMedia.isCheck())
				listItemView.imgCheck.setBackgroundResource(R.drawable.check1);
			else
				listItemView.imgCheck.setBackgroundResource(R.drawable.check);
			listItemView.imgCheck.setVisibility(View.VISIBLE);
		} else {
			listItemView.imgCheck.setVisibility(View.GONE);
		}

		listItemView.textInfo.setText(myMedia.getName());

		return convertView;
	}

	private static String convertTime(int time) {
		int hour = time / 60 / 60 / 1000;
		int min = time / 1000 / 60 % 60;
		int sec = time / 1000 % 60;
		if (hour == 0) {
			return min + ":" + sec;
		} else {
			return hour + ":" + min + ":" + sec;
		}
	}

}
