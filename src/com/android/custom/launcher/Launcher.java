package com.android.custom.launcher;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;

import com.anddroid.custom.launcher.util.FilesUtil;
import com.anddroid.custom.launcher.util.Music;
import com.android.custom.launcher.services.LauncherService;
import com.android.custom.launcher.view.MusicView;
import com.android.custom.launcher.view.PicsView;
import com.android.custom.launcher.view.WeatherView;

public class Launcher extends BaseActivity {


	private MusicView mMusic;

	private LauncherService mService;
	private ServiceConnection mConn = new ServiceConnection() {
		
		public void onServiceDisconnected(ComponentName name) {
		}
		
		public void onServiceConnected(ComponentName name, IBinder binder) {
			mService = ((LauncherService.LocalBinder) binder).getService();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.launcher);

		WeatherView weatherView = (WeatherView)findViewById(R.id.weather);
		weatherView.setWeather(18);
		weatherView.setTemperature(22);
		
		ImageView imageView = (ImageView)findViewById(R.id.img_apps);
		imageView.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(Launcher.this,Applications.class);
				startActivity(intent);
			}
		});
		
		PicsView picsView = (PicsView)findViewById(R.id.pics);

//		mMusicList = FilesUtil.getDataMusics(this);
//		mMusic = (MusicView) findViewById(R.id.music);
//		mMusic.setOnMusicControl(new MusicView.MusicControl() {
//
//			public void play(int position) {
//			    MusicPlay(mMusicList.get(position));
//			}
//
//			public void pause() {
//				MusicPause();
//			}
//
//			public void stop() {
//				MusicStop();
//			}
//
//			public int getTime() {
//				return getStartTime();
//			}
//			
//		});
	}

	
	@Override
	protected void onResume() {
		super.onResume();
		startMusicService();
	}


	private void startMusicService() {
		Intent intent = new Intent(this, LauncherService.class);
		this.bindService(intent, mConn, Context.BIND_AUTO_CREATE);
	}

	private void MusicPlay(Music music) {
//		if (mService != null) {
//			mService.play(music.getUrl());
//		}
	}

	private void MusicStop() {
		if (mService != null) {
			mService.stop();
		}
	}

	private void MusicPause() {
		if (mService != null) {
			mService.pause();
		}
	}

	private int getStartTime() {
		if (mService != null) {
			return mService.getStartTime();
		}
		return 0;
	}
}
