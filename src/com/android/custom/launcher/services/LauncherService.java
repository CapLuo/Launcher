package com.android.custom.launcher.services;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.android.custom.launcher.util.FilesUtil;
import com.android.custom.launcher.util.Music;

public class LauncherService extends Service {
	private Object lock = new Object();

	public enum PlayMode {
		RANDOM, REPEAT, ALL;
	}

	public static final String ACTION = "com.android.launcher.music";
	public static final int MUSIC_COMPLETE_ACTION = 1;
	public static final int MUSIC_REFRESH_ACTION = 2;

	private List<Music> mMusics = new CopyOnWriteArrayList<Music>();
	private int mCurPosition = -1, mCount = 0;
	private MediaPlayer mPlayer;
	private Messenger mMessenger = null;
	private PlayMode mMode = PlayMode.ALL;

	private boolean isRefreshMusicView = true;
	private Runnable mRefreshMusicView = new Runnable() {

		public void run() {
			while (isRefreshMusicView) {
				Message msg = Message.obtain();
				msg.what = MUSIC_REFRESH_ACTION;
				try {
					mMessenger.send(msg);
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		Log.e("@@@@", "3@@@");
		mMusics = FilesUtil.getDataMusics(this);
		mCount = mMusics.size();
		mMessenger = (Messenger) intent.getExtras().get("Messenger");
		return new LocalBinder();
	}

	public class LocalBinder extends Binder {
		public LauncherService getService() {
			return LauncherService.this;
		}
	}

	public void play(int position) {
		synchronized (lock) {

			if (mCurPosition == position) {
				mPlayer.start();
			} else {
				stop();
				mCurPosition = (position + mMusics.size()) % mMusics.size();
				try {
					mPlayer = new MediaPlayer();

					mPlayer.setDataSource(mMusics.get(mCurPosition).getUrl());
					Log.e("@@@@", "" + mMusics.get(mCurPosition).getUrl());
					mPlayer.prepare();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

					public void onCompletion(MediaPlayer mp) {
						stop();
						int position = 0;
						if (mMode == PlayMode.ALL) {
							position = (mCurPosition + 1) % mCount;
						} else if (mMode == PlayMode.RANDOM) {
							java.util.Random r = new java.util.Random();
							position = (Math.abs(r.nextInt()) % mCount);
						} else {
							position = mCurPosition;
						}
						sendMusicCompletionMSG(position);
					}
				});
				mPlayer.start();
				isRefreshMusicView = true;
				new Thread(mRefreshMusicView).start();

			}
		}
	}

	private void sendMusicCompletionMSG(int position) {
		if (mMessenger != null) {
			Message msg = Message.obtain();
			msg.what = MUSIC_COMPLETE_ACTION;
			Bundle b = new Bundle();
			b.putInt("position", position);
			msg.setData(b);
			try {
				mMessenger.send(msg);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void pause() {
		synchronized (lock) {
			if (mPlayer != null && mPlayer.isPlaying()) {
				mPlayer.pause();
			}
		}
	}

	public void stop() {
		synchronized (lock) {
			isRefreshMusicView = false;
			if (mPlayer != null) {
				mPlayer.stop();
				mPlayer.release();
				mPlayer = null;
			}
		}
	}

	public int getStartTime() {
		if (mPlayer != null) {
			return mPlayer.getCurrentPosition();
		}
		return 0;
	}

	public int getMaxTime() {
		if (mPlayer != null) {
			return mPlayer.getDuration();
		}
		return 0;
	}

	public Music getPlayMusic(int position) {
		synchronized (lock) {
			position = (position + mCount) % mCount;
			return mMusics.get(position);
		}
	}

	public boolean isPlaying() {
		if (mPlayer != null) {
			return mPlayer.isPlaying();
		} else {
			return false;
		}
	}

	public int getPosition() {
		if (mCurPosition < 0) {
			return 0;
		}
		return mCurPosition;
	}

}
