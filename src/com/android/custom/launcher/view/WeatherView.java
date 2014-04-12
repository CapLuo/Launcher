package com.android.custom.launcher.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.custom.launcher.R;

public class WeatherView extends LinearLayout{
	private ImageView img;
	private TextView text;
	
	public WeatherView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.weather_view, this, true);
		initView();
	}
	public WeatherView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.weather_view, this, true);
        initView();
    }
	private void initView(){
		img = (ImageView)findViewById(R.id.img_weather);
		text = (TextView)findViewById(R.id.text_weather);
	}
	
	public void setWeather(int code){
		switch (code) {
		case 0:
		case 1: 
		case 2:
		case 23:
		case 24:
		case 25:
			//cloud.png
			img.setBackgroundResource(R.drawable.cloud);
			break;
		case 3:
		case 4:
		case 37:
		case 38:
		case 39:
		case 45:
		case 47:
			//cloud+thunder
			img.setBackgroundResource(R.drawable.cloud_thunder);
			break;
		case 5:
		case 6:
		case 7:
			//cloud+snow.png
			img.setBackgroundResource(R.drawable.cloud_snow);
			break;
		case 8:
		case 9:
		case 10:
			//cloud+rain
			img.setBackgroundResource(R.drawable.cloud_rain);
			break;
		case 11:
		case 12:
			//cloud_sun_heavy_rain
			img.setBackgroundResource(R.drawable.cloud_sun_heavy_rain);
			break;
		case 13:
		case 14:
		case 46:
			img.setBackgroundResource(R.drawable.cloud_sun_snow);
			//cloud+sun+snow
			break;
		case 15:
		case 16:
		case 41:
		case 43:
			img.setBackgroundResource(R.drawable.cloud_heavy_snow);
			//cloud+heavy snow
			break;
		case 17:
		case 18:
			img.setBackgroundResource(R.drawable.cloud_heavy_snow);
			//cloud+heavy snow
			break;
		case 19:
		case 20:
		case 21:
		case 22:
			//fog
			img.setBackgroundResource(R.drawable.fog);
			break;
		case 26:
		case 44:
			//cloud+sun
			img.setBackgroundResource(R.drawable.cloud_sun);
			break;
		case 27:
		case 29:
			//cloud+moon
			img.setBackgroundResource(R.drawable.cloud_moon);
			break;
		case 28:
		case 30:
			//cloud+light
			img.setBackgroundResource(R.drawable.cloud_light);
			break;
		case 31:
		case 33:
			//moon
			img.setBackgroundResource(R.drawable.moon);
			break;
		case 32:
		case 34:
		case 36:
			//sunny
			img.setBackgroundResource(R.drawable.sunny);
			break;
		case 35:
			//cloud+heavy rain
			img.setBackgroundResource(R.drawable.cloud_heavy_rain);
			break;
		case 40:
			//cloud+sun+rain
			img.setBackgroundResource(R.drawable.cloud_sun_rain);
			break;
		case 42:
			//cloud+sun+snow
			img.setBackgroundResource(R.drawable.cloud_sun_snow);
			break;

		default:
			img.setBackground(null);
			break;
		}
	}
	public void setTemperature(int temperature){
		text.setText(temperature+"°");
	}
}
