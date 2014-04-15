package com.android.custom.launcher;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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
 * TODO 异步加载问题(包括Apps进入加载时异步加载)
 * TODO home视频缩略图
 * TODO home最大化图片
 * TODO 打开Gallery页面焦点移动到该图片（音乐视频一样）/分页解决内存溢出问题
 * TODO 首页焦点的移动和焦点的图片
 * TODO home文字的设定
 * TODO 点击音乐播放界面，可以播放音乐   FIX
 * TODO 点击home音乐播放的右下角按钮，直接跳转到播放页面,并传一个path过来   KO
 * 
 * TODO 时间实现，天气优化  ko
 * TODO 音乐播放第一个不能播放,PlayMusicActivity中音乐要显示总时长,点击静音按钮，按钮会移动 KO
 * TODO Apps 界面优化，实现弹出菜单，点击删除
 */
public class Launcher extends BaseActivity implements View.OnClickListener {

    private static MusicView mMusic;
    private Music mCurrentMusic;

    private LauncherService mService;

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

				public void setPlayMusicPostion(final int position) {
					Launcher.this.runOnUiThread(new Runnable() {
						
						public void run() {
							mCurrentMusic = mService.getPlayMusic(position);
							mMusic.setCurrentMusic(mCurrentMusic);
							MusicPlay(position);
						}
					});	
				}

				public void refreshWether(final int code, final int temp) {
					if (mWeatherView != null) {
						Launcher.this.runOnUiThread(new Runnable() {
							
							public void run() {
								StoreUtil.saveCodeAndTemp(Launcher.this, code, temp);
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.launcher);

        mTime = (TextView) findViewById(R.id.home_time);
        mDate = (TextView) findViewById(R.id.home_date);
        mWeatherView = (WeatherView)findViewById(R.id.weather);

        ImageView imageView = (ImageView)findViewById(R.id.img_apps);
        imageView.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent intent = new Intent(Launcher.this,Apps.class);
                startActivity(intent);
            }
        });

        picsView = (PicsView)findViewById(R.id.pics);
        picsView.requestFocus();

		mMusic = (MusicView) findViewById(R.id.music);
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

		});

		View googlePlay = findViewById(R.id.google_play);
		googlePlay.setOnClickListener(this);
		
		View browser = findViewById(R.id.browser);
		browser.setOnClickListener(this);
		
		View usb = findViewById(R.id.img_my_usb);
		usb.setOnClickListener(this);

		View settings = findViewById(R.id.settings);
		settings.setOnClickListener(this);

		IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
		registerReceiver(mTimerReciver, filter);
    }

	@Override
	protected void onStart() {
		super.onStart();
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
		picsView.updatePath();
		startMusicService();
		getTimeToRefresh();
	}

	@Override
	protected void onStop() {
		picsView.savePath();
		unbindService(mConn);
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

    private int getPosition() {
    	if (mService != null) {
            return mService.getPosition();
        }
        return 0;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
                keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
                keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            if (mService != null && mService.isPlaying()) {
                mMusic.onKeyDown(keyCode, event);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

	public void onClick(View v) {
		if (v.getId() == R.id.google_play) {
            startActivityForSafely(Intent.ACTION_MAIN,
            		"com.android.vending",
            		"com.android.vending.AssetBrowserActivity");
		}
		if (v.getId() == R.id.browser) {
			startActivityForSafely(Intent.ACTION_MAIN,
					"com.android.browser","com.android.browser.BrowserActivity");
		}
		if (v.getId() == R.id.img_my_usb) {
			Intent intent = new Intent();
	        intent.setClass(this, ItemListActivity.class);
	        startActivity(intent);
		}
		if (v.getId() == R.id.settings) {
			Intent intent = new Intent();
			intent.setClassName("com.android.settings", "com.android.settings.DisplaySettings");
			startActivity(intent);
		}
	}

	private void startActivityForSafely(String action, String packageName, String activityName) {
		Intent intent = new Intent(action);
		intent.setClassName(packageName, activityName);
		if (getPackageManager().resolveActivity(intent, 0) != null) {
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
        	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        	this.startActivity(intent);
		}
	}

	private void getTimeToRefresh() {

		long time=System.currentTimeMillis();
		final Calendar mCalendar=Calendar.getInstance();
		mCalendar.setTimeInMillis(time);
		mTime.setText(mCalendar.get(Calendar.HOUR) + ":"
		        + mCalendar.get(Calendar.MINUTE));
		Log.e("@@@@", mCalendar.get(Calendar.HOUR) + ":"
		        + mCalendar.get(Calendar.MINUTE));
		mDate.setText(DateUtil.getMonth(mCalendar.get(Calendar.MONTH)) + " " + mCalendar.get(Calendar.DAY_OF_MONTH) + ", " + mCalendar.get(Calendar.YEAR) + " " + DateUtil.getDay(mCalendar.get(Calendar.DAY_OF_WEEK)));
		Log.e("@@@@", (DateUtil.getMonth(mCalendar.get(Calendar.MONTH))) + " " + mCalendar.get(Calendar.DAY_OF_MONTH) + " " + mCalendar.get(Calendar.YEAR) + " " + DateUtil.getDay(mCalendar.get(Calendar.DAY_OF_WEEK)));
	}
}
