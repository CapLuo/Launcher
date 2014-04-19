package com.android.custom.launcher;

import java.io.File;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.custom.launcher.services.LauncherService;
import com.android.custom.launcher.services.LauncherService.PlayMode;
import com.android.custom.launcher.util.DateUtil;
import com.android.custom.launcher.util.Music;
import com.android.custom.launcher.view.MusicView;
import com.android.custom.launcher.view.PicsView;
import com.android.custom.launcher.view.WeatherView;
import com.example.setting.ItemListActivity;
import com.example.setting.util.StoreUtil;

/**
 * TODO 异步加载问题 ko (包括Apps进入加载时异步加载) TODO home视频缩略图 ko TODO home最大化图片 TODO
 * 打开Gallery页面焦点移动到该图片（音乐视频一样）/分页解决内存溢出问题 //FIXME TODO 首页焦点的移动和焦点的图片 TODO
 * home文字的设定 TODO 点击音乐播放界面，可以播放音乐 ko TODO 点击home音乐播放的右下角按钮，直接跳转到播放页面,并传一个path过来
 * KO TODO 天气线程 TODO 时间实现，天气优化 ko TODO
 * 音乐播放第一个不能播放,PlayMusicActivity中音乐要显示总时长,点击静音按钮，按钮会移动 KO TODO Apps
 * 界面优化，实现弹出菜单，点击删除
 *TODO getPositionByPath 
 * 
 * TODO home界面的焦点移动,Text,设置的图片替换, TODO 时间天气的位置 TODO 圆角的图片,视频中间 TODO 时间的格式 luo
 * TODO 播放列表页的音乐的名字错误 luo TODO 测试一下，myusb和页面跳转的一些功能 luo
 * 
 * TODO 音乐播放的列表页，音乐图片和默认图片会错位
 * 
 * 
 * 
 * 
 * TODO 音乐播放下一曲慢，进度条
 * TODO APPS打开系统的Launcher
 * TODO 视频缩略图打开慢
 * TODO 主页面点击二次才能进入
 * 
 * TODO home界面更新UBS设备插入拔出异步,播放列表是否保存播放记录
 * TODO 点击图片最大化
 * 
 * TODO onbackpress 去掉super 天气更新时间
 * 
 */
public class Launcher extends BaseActivity implements View.OnClickListener {

	private static MusicView mMusic;
	private Music mCurrentMusic;

	private LauncherService mService;
	private MyBroadcastReceiver myBroadcastReceiver;

	private ServiceConnection mConn = new ServiceConnection() {

		public void onServiceDisconnected(ComponentName name) {
		}

		public void onServiceConnected(ComponentName name, IBinder binder) {
			mService = ((LauncherService.LocalBinder) binder).getService();
			mService.setRefreshListener(new LauncherService.RefreshListener() {

				public void refresh(final int startTime, final int maxTime,
						final boolean isPause, final PlayMode mode) {
					Launcher.this.runOnUiThread(new Runnable() {

						public void run() {
							mMusic.refreshSeekBar(startTime, maxTime);
							mMusic.refreshPlayButtonAndVolume(!isPause, mode);
						}
					});
				}

				public void setPlayMusicPostion(final int position, final boolean isNeedPlay) {
					Launcher.this.runOnUiThread(new Runnable() {

						public void run() {
							mCurrentMusic = mService.getPlayMusic(position);
							if (mCurrentMusic == null) {
								mMusic.isFirstViewMode(true);
								return;
							}
							mMusic.isFirstViewMode(false);
							mMusic.setCurrentMusic(mCurrentMusic);
							if (isNeedPlay)
								MusicPlay(position);
						}
					});
				}

				public void refreshWether(final int code, final int temp) {
					if (mWeatherView != null) {
						Launcher.this.runOnUiThread(new Runnable() {

							public void run() {
								StoreUtil.saveCodeAndTemp(Launcher.this, code,
										temp);
								mWeatherView.setWeather(code);
								mWeatherView.setTemperature(temp);
							}
						});
					}
				}
			});
			mCurrentMusic = mService.getPlayMusic(mService.getPosition());
			mMusic.isFirstViewMode(mCurrentMusic == null);
			mMusic.setCurrentMusic(mCurrentMusic);
		}
	};

	private BroadcastReceiver mTimerReciver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
				getTimeToRefresh();
			}
		}
	};

	private PicsView picsView;
	private WeatherView mWeatherView;
	private TextView mTime, mDate;
	private ImageView imgVideo;
	private TextView textVideo;
	private ImageView imgVideoPlay;
	private ImageView imgVideoMenu;
	
	private Drawable drawableVideo;
	private ExecutorService threadPool;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.launcher);
		
		threadPool = Executors.newCachedThreadPool();

		mTime = (TextView) findViewById(R.id.home_time);
		mDate = (TextView) findViewById(R.id.home_date);
		mWeatherView = (WeatherView) findViewById(R.id.weather);

		View layoutApps = findViewById(R.id.layout_apps);
		layoutApps.setOnClickListener(this);
		layoutApps.requestFocus();

		picsView = (PicsView) findViewById(R.id.pics);

		mMusic = (MusicView) findViewById(R.id.music);
//		mMusic.setOnClickListener(this);
		mMusic.setOnMusicControl(new MusicView.MusicControl() {

			public void play(int position) {
				MusicPlay(position);
			}

			public void pause() {
				MusicPause();
			}

			public void stop() {
				MusicStop();
			}

			public int getTime() {
				return getStartTime();
			}

			public Music getCurrentMusic(int position) {
				return getMusic(position);
			}

			public int getPosition() {
				return Launcher.this.getPosition();
			}

			public boolean isPlaying() {
				if (mService != null) {
					return mService.isPlaying();
				}
				return false;
			}

			public void changeMode() {
			}

			public PlayMode getPlayMode() {
				return null;
			}

			public void seekTo(int mesc) {
				setMusicSeekTO(mesc);
			}

		});

		findViewById(R.id.img_apps).setOnClickListener(this);
		findViewById(R.id.layout_video).setOnClickListener(this);
		findViewById(R.id.layout_google).setOnClickListener(this);
		findViewById(R.id.img_google).setOnClickListener(this);
		findViewById(R.id.layout_internet).setOnClickListener(this);
		findViewById(R.id.img_internet).setOnClickListener(this);
		findViewById(R.id.layout_usb).setOnClickListener(this);
		findViewById(R.id.img_my_usb).setOnClickListener(this);
		findViewById(R.id.layout_settings).setOnClickListener(this);
		findViewById(R.id.img_settings).setOnClickListener(this);
		findViewById(R.id.layout_pics).setOnClickListener(this);

		imgVideo = (ImageView) findViewById(R.id.img_video);
		imgVideo.setOnClickListener(this);
		imgVideoPlay = (ImageView) findViewById(R.id.img_video_play);
		imgVideoPlay.setOnClickListener(this);
		imgVideoMenu = (ImageView) findViewById(R.id.img_video_menu);
		imgVideoMenu.setOnClickListener(this);

		textVideo = (TextView) findViewById(R.id.text_video_default);

		IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
		registerReceiver(mTimerReciver, filter);
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		if(myBroadcastReceiver == null){
			myBroadcastReceiver = new MyBroadcastReceiver();
			IntentFilter intentfilter = new IntentFilter();
			intentfilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
			intentfilter.addDataScheme("file");
			registerReceiver(myBroadcastReceiver, intentfilter);
		}
		
		int code = StoreUtil.loadCode(this);
		int temp = StoreUtil.loadTemp(this);
		if (code != -100) {
			mWeatherView.setWeather(code);
		} else {
			mWeatherView.setVisibility(View.GONE);
		}
		if (temp != -100) {
			mWeatherView.setTemperature(temp);
		} else {
			mWeatherView.setVisibility(View.GONE);
		}
		refreshView();
		startMusicService();
		getTimeToRefresh();
	}
	
	private void refreshView(){
//		new SampleTask(this, new SampleTask.TaskListener() {
//			
//			public void updatePageUI() {
//				drawableVideo = StoreUtil.loadVideoBitmap(Launcher.this);
//			}
//			
//			public void updatePageData() {
//				picsView.updatePath();
//				if (drawableVideo != null) {
//					imgVideo.setBackgroundDrawable(drawableVideo);
//					textVideo.setVisibility(View.GONE);
//					imgVideoPlay.setVisibility(View.VISIBLE);
//					imgVideoMenu.setVisibility(View.VISIBLE);
//				} else {
//					imgVideo.setBackgroundResource(R.drawable.home_video_default);
//					textVideo.setVisibility(View.VISIBLE);
//					imgVideoPlay.setVisibility(View.GONE);
//					imgVideoMenu.setVisibility(View.GONE);
//				}
//			}
//		}).executeOnExecutor(threadPool);
		
		drawableVideo = StoreUtil.loadVideoBitmap(Launcher.this);
		picsView.updatePath();
		if (drawableVideo != null) {
			imgVideo.setBackgroundDrawable(drawableVideo);
			textVideo.setVisibility(View.GONE);
			imgVideoPlay.setVisibility(View.VISIBLE);
			imgVideoMenu.setVisibility(View.VISIBLE);
		} else {
			imgVideo.setBackgroundResource(R.drawable.home_video_default);
			textVideo.setVisibility(View.VISIBLE);
			imgVideoPlay.setVisibility(View.GONE);
			imgVideoMenu.setVisibility(View.GONE);
		}

	}

	@Override
	protected void onStop() {
		picsView.savePath();
		unbindService(mConn);
		
		if(myBroadcastReceiver != null){
			unregisterReceiver(myBroadcastReceiver);
			myBroadcastReceiver = null;
		}
		
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(mTimerReciver);
		super.onDestroy();
	}

	private void startMusicService() {
		Intent intent = new Intent(this, LauncherService.class);
		this.bindService(intent, mConn, Context.BIND_AUTO_CREATE);
	}

	private void MusicPlay(int position) {
		if (mService != null) {
			mService.play(position);
		}
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

	private int getMaxTime() {
		if (mService != null) {
			return mService.getMaxTime();
		}
		return 0;
	}

	private Music getMusic(int position) {
		if (mService != null) {
			return mService.getPlayMusic(position);
		}
		return null;
	}

	private void setMusicSeekTO(int mesc) {
		if (mService != null) {
			mService.playSeekTo(mesc);
		}

	}

	private int getPosition() {
		if (mService != null) {
			return mService.getPosition();
		}
		return 0;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
				|| keyCode == KeyEvent.KEYCODE_VOLUME_UP
				|| keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
			if (mService != null && mService.isPlaying()) {
				mMusic.onKeyDown(keyCode, event);
			}
		}else if(keyCode == KeyEvent.KEYCODE_BACK){
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	

//	@Override
//	public void onBackPressed() {
//	}

	public void onClick(View v) {
		if (v.getId() == R.id.layout_google || v.getId() == R.id.img_google) {
			boolean isSuccess = startActivityForSafely(Intent.ACTION_MAIN, "com.android.vending",
					"com.android.vending.AssetBrowserActivity");
			if(!isSuccess){
				Toast.makeText(this, "Can not find Google play.", Toast.LENGTH_LONG).show();
			}
		} else if (v.getId() == R.id.layout_internet || v.getId() == R.id.img_internet) {
			startActivityForSafely(Intent.ACTION_MAIN, "com.android.browser",
					"com.android.browser.BrowserActivity");
		} else if (v.getId() == R.id.layout_usb || v.getId() == R.id.img_my_usb) {
//			Intent intent = new Intent();
//			intent.putExtra("position", 0);
//			intent.setClass(this, ItemListActivity.class);
//			startActivity(intent);
			
			Intent intent = new Intent();
			intent.setClassName("com.fb.FileBrower", "com.fb.FileBrower.FileBrower");
			if (getPackageManager().resolveActivity(intent, 0) != null) {
				this.startActivity(intent);
			}
		} else if (v.getId() == R.id.layout_video || v.getId() == R.id.img_video_play || v.getId() == R.id.img_video) {
			String path = StoreUtil.loadVideoPath(this);
			if(path != null){
				File file = new File(path);
				if (file.isFile() && imgVideoPlay.getVisibility() == View.VISIBLE) {
					try {
						Uri uri = Uri.parse("file://" + path);
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setDataAndType(uri,
								StoreUtil.loadVideoMimeType(this));
						startActivity(intent);
					} catch (Exception e) {
						e.printStackTrace();
						Intent intent = new Intent();
						intent.putExtra("position", 0);
						intent.putExtra("path", path);
						intent.setClass(this, ItemListActivity.class);
						startActivity(intent);
					}
					return;
				}
			}
			Intent intent = new Intent();
			intent.putExtra("position", 0);
			intent.putExtra("path", path);
			intent.setClass(this, ItemListActivity.class);
			startActivity(intent);
		} else if(v.getId() == R.id.img_video_menu){
			Intent intent = new Intent();
			intent.putExtra("position", 0);
			intent.putExtra("path", "All");
			intent.setClass(this, ItemListActivity.class);
			startActivity(intent);
		}else if (v.getId() == R.id.layout_settings || v.getId() == R.id.img_settings) {
			try{
				Intent intent = new Intent();
				intent.setClassName("com.android.settings",
						"com.android.settings.DisplaySettings");
				startActivity(intent);
			}catch (Exception e) {
				e.printStackTrace();
			}
		} else if (v.getId() == R.id.layout_pics) {
			picsView.openPic();
		} else if (v.getId() == R.id.layout_music) {
			// TODO
		} else if(v.getId() == R.id.layout_apps || v.getId() == R.id.img_apps){
			Intent intent = new Intent(Launcher.this, Apps.class);
			startActivity(intent);
//			goToApps();
		}
	}

	private boolean startActivityForSafely(String action, String packageName,
			String activityName) {
		Intent intent = new Intent(action);
		intent.setClassName(packageName, activityName);
		if (getPackageManager().resolveActivity(intent, 0) != null) {
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			this.startActivity(intent);
			return true;
		}
		return false;
	}

	private void getTimeToRefresh() {

		long time = System.currentTimeMillis();
		final Calendar mCalendar = Calendar.getInstance();
		mCalendar.setTimeZone(TimeZone.getDefault());
		mCalendar.setTimeInMillis(time);
		mTime.setText(DateUtil.getTime(mCalendar.get(Calendar.HOUR_OF_DAY)) + ":"
				+ DateUtil.getTime(mCalendar.get(Calendar.MINUTE)));
		mDate.setText(DateUtil.getMonth(mCalendar.get(Calendar.MONTH)) + " "
				+ mCalendar.get(Calendar.DAY_OF_MONTH) + ", "
				+ mCalendar.get(Calendar.YEAR) + " "
				+ DateUtil.getDay(mCalendar.get(Calendar.DAY_OF_WEEK)));
	}
	
	private void goToApps(){
		Intent intent = new Intent();
		intent.setClassName("com.android.launcher", "com.android.launcher2.Launcher");
//		if (getPackageManager().resolveActivity(intent, 0) != null) {
//			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			this.startActivity(intent);
//		}
	}
	
	private class MyBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(intent.getAction())) {
				refreshView();
			}
		}

	}
	
}
