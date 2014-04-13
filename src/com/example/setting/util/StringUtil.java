package com.example.setting.util;

public class StringUtil {
	public static final boolean emptyOrNull(String value) {
		return (value != null) && (value.length() != 0);
	}
	public static final String show(final Number value) {
		return value != null ? value.toString() : "";
	}
	
	public static final String show(final String value) {
		return value != null ? value : "";
	}

	public static final String show(final CharSequence value) {
		return value != null ? value.toString() : "";
	}
	
	public static final String show(final Object value) {
		return value != null ? value.toString() : "";
	}

}
