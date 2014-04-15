package com.android.custom.launcher.util;

public class DateUtil {

	public static String getDay(int position) {
		String day = "";
		if (position == 2) {  
			day = "Mon.";  
        } else if (position == 3) {  
        	day = "Tue.";  
        } else if (position == 4) {  
        	day = "Wed.";  
        } else if (position == 5) {  
        	day = "Thu.";  
        } else if (position == 6) {  
        	day = "Fri.";  
        } else if (position == 7) {  
        	day = "Sat.";  
        } else if (position == 1) {  
        	day = "Sun.";  
        }  
		return day;
	}

	public static String getMonth(int position) {
		String month = "";
		if (position == 0) {  
			month = "January";  
        } else if (position == 1) {  
        	month = "February";  
        } else if (position == 2) {  
        	month = "March";  
        } else if (position == 3) {  
        	month = "April";  
        } else if (position == 4) {  
        	month = "May";  
        } else if (position == 5) {  
        	month = "June";  
        } else if (position == 6) {  
        	month = "July";  
        } else if (position == 7) {
        	month = "August"; 
        } else if (position == 8) {
        	month = "September"; 
        } else if (position == 9) {
        	month = "October"; 
        } else if (position == 10) {
        	month = "November"; 
        } else if (position == 11) {
        	month = "December"; 
        }
		return month;
	}

}
