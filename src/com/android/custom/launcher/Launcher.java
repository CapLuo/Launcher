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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.android.custom.launcher.services.LauncherService;
import com.android.custom.launcher.util.HttpHelper;
import com.android.custom.launcher.util.Music;
import com.android.custom.launcher.view.MusicView;
import com.android.custom.launcher.view.PicsView;
import com.android.custom.launcher.view.WeatherView;
import com.example.setting.util.StoreUtil;

public class Launcher extends BaseActivity {

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == LauncherService.MUSIC_COMPLETE_ACTION) {
				int position = msg.getData().getInt("position", -1);
				if (position == -1) {
					position = mService.getPosition() + 1;
				}
				mMusic.setCurrentMusic(mService.getPlayMusic(position));
				MusicPlay(position);
			}
			if (msg.what == LauncherService.MUSIC_REFRESH_ACTION) {
				mMusic.refreshSeekBar(getStartTime(), getMaxTime());
			}
        }
	};

	private Messenger mMessenger = null;
    private MusicView mMusic;
    private Music mCurrentMusic;

    private LauncherService mService;
    private ServiceConnection mConn = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
        }

        public void onServiceConnected(ComponentName name, IBinder binder) {
            mService = ((LauncherService.LocalBinder) binder).getService();
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
                Intent intent = new Intent(Launcher.this,Applications.class);
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

		});

        startMusicService();
        getWeather();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mService != null) {
        	mCurrentMusic = mService.getPlayMusic(mService.getPosition());
        	mMusic.isFirstViewMode(mCurrentMusic == null);
        	mMusic.setCurrentMusic(mCurrentMusic);
        }
    }

    @Override
	protected void onStart() {
		super.onStart();
		picsView.updatePath();
	}

	@Override
	protected void onStop() {
		picsView.savePath();
		super.onStop();
	}

	private void startMusicService() {
		mMessenger = new Messenger(mHandler);
        Intent intent = new Intent(this, LauncherService.class);
        //intent.setAction(LauncherService.ACTION);
        intent.putExtra("Messenger", mMessenger);
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
			@Override
			public void run() {
				String city = HttpHelper.getCity();
				String woeid = HttpHelper.getWoeid(city);
				woeid = xmlToString(woeid);
				woeid = woeid.substring(woeid.lastIndexOf("woeid") + 6, woeid.lastIndexOf("woeid") + 14);
				String wether = HttpHelper.getWeather(woeid);
				xmlToStrings(wether);
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

				@Override
				public void run() {
					mWeatherView.setWeather(code);
					mWeatherView.setTemperature(temp);
					StoreUtil.saveCodeAndTemp(Launcher.this, code, temp);
					mHandler.postDelayed(mRefreshWeather, 60 * 1000 * 60);
				}
			});
		} catch (DocumentException e) {
			e.printStackTrace();
		}
    }
    private Runnable mRefreshWeather = new Runnable() {
		
		@Override
		public void run() {
			getWeather();
		}
	};
}
