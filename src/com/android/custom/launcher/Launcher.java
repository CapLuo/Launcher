package com.android.custom.launcher;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.android.custom.launcher.services.LauncherService;
import com.android.custom.launcher.services.LauncherService.PlayMode;
import com.android.custom.launcher.util.HttpHelper;
import com.android.custom.launcher.util.Music;
import com.android.custom.launcher.view.MusicView;
import com.android.custom.launcher.view.PicsView;
import com.android.custom.launcher.view.WeatherView;
import com.example.setting.ItemListActivity;
import com.example.setting.PlayMusicActivity;
import com.example.setting.util.StoreUtil;

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
						}
					});	
				}
			});
            mCurrentMusic = mService.getPlayMusic(mService.getPosition());
        	mMusic.isFirstViewMode(mCurrentMusic == null);
        	mMusic.setCurrentMusic(mCurrentMusic);
        }
    };

	private PicsView picsView;
	private WeatherView mWeatherView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.launcher);

        mWeatherView = (WeatherView)findViewById(R.id.weather);
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

        getWeather();
    }

	@Override
	protected void onStart() {
		super.onStart();
		picsView.updatePath();
		startMusicService();
	}

	@Override
	protected void onStop() {
		picsView.savePath();
		unbindService(mConn);
		super.onStop();
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

    private void getWeather() {
        new Thread(new Runnable() {
			public void run() {
				String city = HttpHelper.getCity();
				String woeid = HttpHelper.getWoeid(city);
				if (woeid != null) {
					woeid = xmlToString(woeid);
				} else {
					return;
				}
				woeid = woeid.substring(woeid.lastIndexOf("woeid") + 6, woeid.lastIndexOf("woeid") + 14);
				String wether = HttpHelper.getWeather(woeid);
				if (wether != null) {
					xmlToStrings(wether);
				} else {
					return;
				}
			}
		}).start();
    }

    private String xmlToString(String woeid) {
        Document doc = null;
        try {
			doc = DocumentHelper.parseText(woeid);
			Element root = doc.getRootElement();
			Element iters = root.element("s");
			return iters.attributeValue("d");
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
    }

    private void xmlToStrings(String woeid) {
        Document doc = null;
        try {
			doc = DocumentHelper.parseText(woeid);
			Element root = doc.getRootElement();
			Element cancel = root.element("channel");
			Element item = cancel.element("item");
			Element iters = item.element("condition");
			final int code = Integer.parseInt(iters.attributeValue("code"));
			final int temp = Integer.parseInt(iters.attributeValue("temp"));
			runOnUiThread(new Runnable() {

				public void run() {
					mWeatherView.setWeather(code);
					mWeatherView.setTemperature(temp);
					StoreUtil.saveCodeAndTemp(Launcher.this, code, temp);
					//mHandler.postDelayed(mRefreshWeather, 60 * 1000 * 60);
				}
			});
		} catch (DocumentException e) {
			e.printStackTrace();
		}
    }
    private Runnable mRefreshWeather = new Runnable() {
		
		public void run() {
			getWeather();
		}
	};

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
}
