package com.example.setting.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class StoreUtil {
	private final static String KEY = "Desk";
	
	public static void savePicPath(Context context,String picPath){
		SharedPreferences sharedPreferences = context.getSharedPreferences(KEY, 0);
		Editor edit = sharedPreferences.edit();
		edit.putString("picPath", picPath);
		edit.commit();
	}
	
	public static String loadPicPath(Context context){
		SharedPreferences sharedPreferences = context.getSharedPreferences(KEY, 0);
		return sharedPreferences.getString("picPath", null);
	}
}
