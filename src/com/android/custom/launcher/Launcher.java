package com.android.custom.launcher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.android.custom.launcher.services.LauncherService;
import com.android.custom.launcher.util.Music;
import com.android.custom.launcher.view.MusicView;
import com.android.custom.launcher.view.PicsView;
import com.android.custom.launcher.view.WeatherView;

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
            Log.e("@@@@", "1@@@" + (mCurrentMusic == null));
        	mMusic.isFirstViewMode(mCurrentMusic == null);
        	mMusic.setCurrentMusic(mCurrentMusic);
        }
    };

	private PicsView picsView;

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

			@Override
			public boolean isPlaying() {
				if (mService != null) {
					return mService.isPlaying();
				}
				return false;
			}

		});

        startMusicService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("@@@@", "@@@" + (mService == null));
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
		Log.e("@@@@", "@@@2");
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

}
