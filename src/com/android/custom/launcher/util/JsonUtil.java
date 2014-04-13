package com.android.custom.launcher.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtil {
	public static final String getJsonString(String key, JSONObject jo)
			throws JSONException {
		if (jo.has(key)) {
			return jo.get(key).toString();
		}
		return null;
	}

	public static final int getJsonInt(String key, JSONObject jo)
			throws JSONException {
		if (jo.has(key)) {
			return jo.getInt(key);
		}
		return 0;
	}

	public static final Long getJsonLong(String key, JSONObject jo)
			throws JSONException {
		if (jo.has(key)) {
			return jo.getLong(key);
		}
		return (long) 0;
	}

	public static final double getJsonDouble(String key, JSONObject jo)
			throws JSONException {
		if (jo.has(key)) {
			return jo.getDouble(key);
		}
		return (double) 0;
	}

	public static final JSONArray getJsonArray(String key, JSONObject jo)
			throws JSONException {
		if (jo.has(key)) {
			return jo.getJSONArray(key);
		}
		return null;
	}
	
	public static final JSONObject getJsonObject(String key, JSONObject jo)
			throws JSONException {
		if (jo.has(key)) {
			return jo.getJSONObject(key);
		}
		return null;
	}
	
	public static final Long getAppOrgId(String str){
		try{
			JSONObject jo = new JSONObject(str);
			JSONObject organize = getJsonObject("organize", jo);
			Long orgId = getJsonLong("id", organize);
			return orgId;
		} catch (JSONException e){
			e.printStackTrace();
			return null;
		}
	}

}
