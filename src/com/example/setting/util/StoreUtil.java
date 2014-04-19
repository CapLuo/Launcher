package com.example.setting.util;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore.Images.Thumbnails;

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
	
	public static void saveVideoPath(Context context,String videoPath){
		SharedPreferences sharedPreferences = context.getSharedPreferences(KEY, 0);
		Editor edit = sharedPreferences.edit();
		edit.putString("videoPath", videoPath);
		edit.commit();
	}
	
	public static void saveVideoMimeType(Context context,String mimeType){
		SharedPreferences sharedPreferences = context.getSharedPreferences(KEY, 0);
		Editor edit = sharedPreferences.edit();
		edit.putString("videoMimeType", mimeType);
		edit.commit();
	}
	
	public static Drawable loadVideoBitmap(Context context){
		SharedPreferences sharedPreferences = context.getSharedPreferences(KEY, 0);
		String videoPath = sharedPreferences.getString("videoPath", null);
		if(videoPath == null)
			return null;
		File file = new File(videoPath);
		if(!file.isFile())
			return null;
		Bitmap bitmap =  MediaHelper.getVideoThumbnail(480, 270, videoPath,Thumbnails.FULL_SCREEN_KIND);
		if(bitmap == null)
			return null;
		Bitmap bitmap2 = MediaHelper.getRoundedBitmap(bitmap, 12);
		if(bitmap2 == null)
			bitmap2 = bitmap;
		return new BitmapDrawable(bitmap2);
	}
	
	public static String loadVideoPath(Context context){
		SharedPreferences sharedPreferences = context.getSharedPreferences(KEY, 0);
		return sharedPreferences.getString("videoPath", null);
	}
	
	public static String loadVideoMimeType(Context context){
		SharedPreferences sharedPreferences = context.getSharedPreferences(KEY, 0);
		return sharedPreferences.getString("videoMimeType", null);
	}

	public static void saveCodeAndTemp(Context context, int code, int temp) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(KEY, 0);
		Editor edit = sharedPreferences.edit();
		edit.putInt("code", code);
		edit.putInt("temp", temp);
		edit.commit();
	}

	public static int loadCode (Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(KEY, 0);
		return sharedPreferences.getInt("code", -100);
	}

	public static int loadTemp (Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(KEY, 0);
		return sharedPreferences.getInt("temp", -100);
	}
}
