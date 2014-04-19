package com.example.setting.util;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;

import com.android.custom.launcher.R;

public class Util {
	public static Integer ObjToInt(Object obj) {
		Integer ret = 0;
		try {
			if (obj != null && !obj.toString().equals("null"))
				ret = Integer.valueOf(obj.toString().trim());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ret;
	}
	
	public static String getRootFilePath() {
		return "/storage/external_storage";//"/mnt/sdcard";
	}

	public static ArrayList<HashMap<String, Object>> getLeftMainMenuDatas() {
		ArrayList<HashMap<String, Object>> datas = new ArrayList<HashMap<String, Object>>();

		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("Text", R.string.video);
		map.put("Resource", R.drawable.video_default);
		map.put("ResourceFucus", R.drawable.video_focus);
		datas.add(map);

		map = new HashMap<String, Object>();
		map.put("Text", R.string.music);
		map.put("Resource", R.drawable.music_default);
		map.put("ResourceFucus", R.drawable.music_focus);
		datas.add(map);

		map = new HashMap<String, Object>();
		map.put("Text", R.string.gallery);
		map.put("Resource", R.drawable.gallery_default);
		map.put("ResourceFucus", R.drawable.gallery_focus);
		datas.add(map);

		map = new HashMap<String, Object>();
		map.put("Text", R.string.other);
		map.put("Resource", R.drawable.otherfiles_default);
		map.put("ResourceFucus", R.drawable.otherfiles_fucus);
		datas.add(map);

		return datas;
	}
	
	public static Dialog progressDialog(Context context, String title, String str) {
		  LayoutInflater flater = LayoutInflater.from(context);
		  View view = flater.inflate(R.layout.custom_progress_dialog, null);
		  LayoutParams params = new LayoutParams();
		  Dialog dialog = new Dialog(context, R.style.custom_dialog);
		  dialog.setCancelable(false);
		  dialog.addContentView(view, params);
		  return dialog;
		 
	}
}
