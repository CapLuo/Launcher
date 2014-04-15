package com.example.setting;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import cn.ireliance.android.common.ui.SwitchCallbackFragmentActivity;

import com.android.custom.launcher.R;
import com.android.custom.launcher.services.LauncherService;
import com.android.custom.launcher.services.LauncherService.PlayMode;
import com.android.custom.launcher.util.Music;
import com.android.custom.launcher.view.MusicView;
import com.example.fragment.GridItemCallBack;
import com.example.fragment.MyGridFragment;
import com.example.setting.adapter.MyMedia;
import com.example.setting.listener.KeyBackListener;
import com.example.setting.util.StoreUtil;
import com.example.setting.view.UsbTitleView;

/**
 * An activity representing a list of Items. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link ItemDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ItemListFragment} and the item details (if present) is a
 * {@link ItemDetailFragment}.
 * <p>
 * This activity also implements the required {@link ItemListFragment.Callbacks}
 * interface to listen for item selections.
 */
public class PlayMusicActivity extends SwitchCallbackFragmentActivity {

    private static MusicView mMusic;
    private Music mCurrentMusic;

    private LauncherService mService;
    private MyBroadcastReceiver myBroadcastReceiver;
    private String url;
    
    private ServiceConnection mConn = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
        }

        public void onServiceConnected(ComponentName name, IBinder binder) {
            mService = ((LauncherService.LocalBinder) binder).getService();
            mService.setRefreshListener(new LauncherService.RefreshListener() {
				
				public void refresh(final int startTime, final int maxTime,
						final boolean isPause, final PlayMode mode) {
					PlayMusicActivity.this.runOnUiThread(new Runnable() {
						
						public void run() {
							mMusic.refreshSeekBar(startTime, maxTime);
							mMusic.refreshPlayButtonAndVolume(!isPause, mode);
						}
					});
				}

				public void setPlayMusicPostion(final int position) {
					PlayMusicActivity.this.runOnUiThread(new Runnable() {
						
						public void run() {
							mCurrentMusic = mService.getPlayMusic(position);
							mMusic.setCurrentMusic(mCurrentMusic);
							MusicPlay(position);
						}
					});	
				}

				public void refreshWether(int code, int temp) {
					StoreUtil.saveCodeAndTemp(PlayMusicActivity.this, code, temp);
				}
			});
            mCurrentMusic = mService.getPlayMusic(mService.getPosition());
        	mMusic.isFirstViewMode(mCurrentMusic == null);
        	mMusic.setCurrentMusic(mCurrentMusic);
        }
    };

	private MyGridFragment currFragment;
	private UsbTitleView usbTitleView;

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_play_music);
		usbTitleView = (UsbTitleView)findViewById(R.id.usb_title);
		currFragment = selectFragment(MyMedia.TYPE_MUSIC);
		
		mMusic = (MusicView) findViewById(R.id.music_big);
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
				return PlayMusicActivity.this.getPosition();
			}

			public boolean isPlaying() {
				if (mService != null) {
					return mService.isPlaying();
				}
				return false;
			}

			public void changeMode() {
				mService.changeMode();
			}

			public PlayMode getPlayMode() {
				return mService.getMode();
			}

		});

		Intent intent = getIntent();
		if (intent != null) {
			url = intent.getStringExtra("path");
		}
	}


	@Override
	protected void onStart() {
		startMusicService();
		if(myBroadcastReceiver == null){
			IntentFilter intentfilter = new IntentFilter(Intent.ACTION_MEDIA_SCANNER_STARTED);
			intentfilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
			intentfilter.addDataScheme("file");
			registerReceiver(myBroadcastReceiver, intentfilter);
		}
		super.onStart();
	}


	@Override
	protected void onStop() {
		super.onStop();
		unbindService(mConn);
		if(myBroadcastReceiver != null){
			unregisterReceiver(myBroadcastReceiver);
			myBroadcastReceiver = null;
		}
	}


	private MyGridFragment selectFragment(int type) {
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		Bundle arguments = new Bundle();
		arguments.putInt("mediaType", type);
		arguments.putBoolean("isPlayMusic", true);
		arguments.putString("path", url);
		MyGridFragment fragment = new MyGridFragment(usbTitleView);
		fragment.setArguments(arguments);
		transaction.replace(R.id.item_detail_container, fragment);
		transaction.commit();
		GridItemCallBack gridItemCallBack = new GridItemCallBack(this, fragment);
		putItemSelectedCallback(MyGridFragment.class, gridItemCallBack);
		return fragment;
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
    
    public void MusicPlay(String id){
    	if (mService != null) {
    		long d = 0;
    		try{
    			d = Long.parseLong(id);
    		}catch (NumberFormatException e) {
    			e.printStackTrace();
			}
			mService.playID(d);
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
        }else if(keyCode == KeyEvent.KEYCODE_BACK){
			if (currFragment instanceof KeyBackListener) {
				if (((KeyBackListener) currFragment).onKeyBackDown())
					return true;
			}
		}
        return super.onKeyDown(keyCode, event);
    }
    
    private class MyBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(intent.getAction())) {
				Log.i("Catch","ACTION_MEDIA_SCANNER_FINISHED");
				currFragment.reLoadData();
				currFragment.updateUI();
			} else if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(intent.getAction())) {
				Log.i("Catch","ACTION_MEDIA_SCANNER_STARTED");
			}else if (Intent.ACTION_MEDIA_MOUNTED.equals(intent.getAction())) {
				Log.i("Catch","ACTION_MEDIA_MOUNTED");
			}else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(intent.getAction())) {
				Log.i("Catch","ACTION_MEDIA_UNMOUNTED");
			}else if (Intent.ACTION_MEDIA_EJECT.equals(intent.getAction())) {
				Log.i("Catch","ACTION_MEDIA_EJECT");
				currFragment.reLoadData();
				currFragment.updateUI();
			}

		}

	}

}
