package com.android.custom.launcher.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class HttpHelper {
	public static final String BASE_URL = "http://192.168.1.187:8080/rest-api";// "http://183.63.212.75:7080/bdyun"

	public static final String IPTOCITY = "http://int.dpool.sina.com.cn/iplookup/iplookup.php?format=json&ip=";

	public static final String WEATHER_URL = "http://weather.yahooapis.com/forecastrss?w=";
	public static final String WEATHER_END = "&u=c";

	private static final String ACCEPT_JSON = "application/json";
	private static final String ACCEPT_TEXT = "text/plain";

	public static final String getCity() {
		String ret = get(IPTOCITY, null);
		String city = null;
		try {
			JSONObject jo = new JSONObject(ret);
			city = JsonUtil.getJsonString("city", jo);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (city != null) {
			city = unicodeToGB(city);
		}
		return city;
	}

	public static final String getWoeid(String city) {
		String ret = get("http://sugg.us.search.yahoo.net/gossip-gl-location/?appid=weather&output=xml&command="
				+ city, null);

		return ret;
	}

	public static final String getWeather(String id) {
		String ret = get(WEATHER_URL + id +WEATHER_END, null);
		return ret;
	}

	public static String unicodeToGB(String s) {
		StringTokenizer st = new StringTokenizer(s, "\\u");
		return st.nextToken();
	}

	public static final String get(String url, String accept) {
		return get(url, accept, null);
	}

	public static final String get(String url, String accept, String ticketId) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);

		if (accept != null)
			httpGet.addHeader("Accept", accept);

		if (ticketId != null)
			httpGet.addHeader("Cookie", "ticketId=" + ticketId);

		try {
			HttpResponse httpResponse = httpclient.execute(httpGet);
			// 璇锋眰鎴愬姛
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// 鍙栧緱杩斿洖鐨勫瓧绗︿覆
				String strResult = EntityUtils.toString(
						httpResponse.getEntity(), "UTF-8");
				// Log.i("Catch", "get:"+strResult);
				return strResult;
			} else {
				// Log.e("Catch",
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static final InputStream get(String url) {
		HttpURLConnection conn = null;
		try {
			URL mUrl = new URL(url);
			conn = (HttpURLConnection) mUrl.openConnection();
			conn.setConnectTimeout(60 * 1000);
			conn.setReadTimeout(60 * 1000);
			return conn.getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
		}
		return null;
	}

	public static final String post(String url, String accept,
			String contentType, String ticketId,
			List<NameValuePair> nameValuePairs) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(url);

		httpPost.addHeader("Accept", accept);
		httpPost.addHeader("Content-Type", contentType);

		if (ticketId != null)
			httpPost.addHeader("Cookie", "ticketId=" + ticketId);

		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
			HttpResponse httpResponse = httpclient.execute(httpPost);

			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				// 鍙栧緱杩斿洖鐨勫瓧绗︿覆
				String strResult = EntityUtils.toString(httpResponse
						.getEntity());
				// Log.i("Catch", "post:"+strResult);
				return strResult;
			} else {
				// Log.e("Catch",
				// "璇锋眰閿欒!Code:"+httpResponse.getStatusLine().getStatusCode());
			}

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

}
