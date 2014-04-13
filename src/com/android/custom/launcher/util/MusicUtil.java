package com.android.custom.launcher.util;

public class MusicUtil {

	public static String formatTime(long time){
		String m = "";
		String n = "";
		//å¾—åˆ°æ‰?œ‰ç§?
		long t = time/1000;
		long mm = t/60;
		long ss = t%60;
		if(mm<10){
			m = "0"+mm;
		}else{
			m = mm+"";
		}
		if(ss<10){
			n = "0"+ss;
		}else{
			n = ss+"";
		}
		return m+":"+n;
	}
}
