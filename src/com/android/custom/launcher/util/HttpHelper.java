package com.android.custom.launcher.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.StringTokenizer;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class HttpHelper {
	public static final String IPTOCITY = "http://int.dpool.sina.com.cn/iplookup/iplookup.php?format=json&ip=";

	public static final String WEATHER_URL = "http://weather.yahooapis.com/forecastrss?w=";
	public static final String WEATHER_END = "&u=c";

	public static final String getCity() {
		String ret = get(IPTOCITY);
		String city = null;
		if (ret == null) {
			return null;
		}
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

	/**
	 * @param path
	 * @return
	 */
	public static final String get1(String path) {
		URL url;
		HttpURLConnection conn = null;
		String result = null;
		try {
			url = new URL(path);

			// 2)利用HttpURLConnection对象从网络中获取网页数据
			conn = (HttpURLConnection) url.openConnection();
			// 3)设置连接超时
			conn.setConnectTimeout(6 * 1000);
			// 4)对响应码进行判断
			if (conn.getResponseCode() != 200) { // 从Internet获取网页,发送请求,将网页以流的形式读回来
				throw new RuntimeException("request url failed");
			}
			// 5)得到网络返回的输入流
			InputStream is = conn.getInputStream();

			StringBuilder b = new StringBuilder();

			BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			char buffer[] = new char[4096];
			int len;
			while ((len = br.read(buffer)) > 0)
				b.append(new String(buffer, 0, len));
			conn.disconnect();

			result = b.toString(); // 文件流输入出文件用outStream.write
			conn.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		} finally {
			if (conn != null)
				conn.disconnect();
		}
		return result;
	}

	public static final String getWoeid(String city) {
		String ret = null;
		try {
			city = URLEncoder.encode(city, "UTF-8");
			ret = get("http://sugg.us.search.yahoo.net/gossip-gl-location/?appid=weather&output=xml&command=" + city);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return ret;
	}

	public static final String getWeather(String id) {
		String ret = get(WEATHER_URL + id + WEATHER_END);
		return ret;
	}

	public static String unicodeToGB(String s) {
		StringTokenizer st = new StringTokenizer(s, "\\u");
		return st.nextToken();
	}

	public static final String get(String url) {
		return get1(url);// get(url, null, null);
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
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String strResult = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
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

	// public static final InputStream get(String url) {
	// HttpURLConnection conn = null;
	// try {
	// URL mUrl = new URL(url);
	// conn = (HttpURLConnection) mUrl.openConnection();
	// conn.setConnectTimeout(60 * 1000);
	// conn.setReadTimeout(60 * 1000);
	// return conn.getInputStream();
	// } catch (IOException e) {
	// e.printStackTrace();
	// } finally {
	// }
	// return null;
	// }

	// public static final String post(String url, String accept, String
	// contentType, String ticketId,
	// List<NameValuePair> nameValuePairs) {
	// HttpClient httpclient = new DefaultHttpClient();
	// HttpPost httpPost = new HttpPost(url);
	//
	// httpPost.addHeader("Accept", accept);
	// httpPost.addHeader("Content-Type", contentType);
	//
	// if (ticketId != null)
	// httpPost.addHeader("Cookie", "ticketId=" + ticketId);
	//
	// try {
	// httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));
	// HttpResponse httpResponse = httpclient.execute(httpPost);
	//
	// if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	// String strResult = EntityUtils.toString(httpResponse.getEntity());
	// // Log.i("Catch", "post:"+strResult);
	// return strResult;
	// } else {
	// // Log.e("Catch",
	// // "璇锋眰閿欒!Code:"+httpResponse.getStatusLine().getStatusCode());
	// }
	//
	// } catch (UnsupportedEncodingException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (ClientProtocolException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// return null;
	// }

}
