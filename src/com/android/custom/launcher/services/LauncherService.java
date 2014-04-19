package com.android.custom.launcher.services;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.android.custom.launcher.util.FilesUtil;
import com.android.custom.launcher.util.HttpHelper;
import com.android.custom.launcher.util.Music;

public class LauncherService extends Service {
	private Object lock = new Object();

	public enum PlayMode {
		RANDOM, REPEAT, ALL;
	}

	public static final String ACTION = "com.android.launcher.music";
	public static final int MUSIC_COMPLETE_ACTION = 1;
	public static final int MUSIC_REFRESH_ACTION = 2;
	public static final int MUSIC_REFRESH_BUTTON_ACTION = 3;

	private Handler mHandler = new Handler();

	private List<Music> mMusics = new CopyOnWriteArrayList<Music>();
	private int mCurPosition = -1, mCount = 0;
	private MediaPlayer mPlayer;
	private PlayMode mMode = PlayMode.ALL;
	private boolean isPause = true;
	private int mMesc;
	private DataReciver mReciver = new DataReciver();

	private boolean isRefreshMusicView = true;
	private Runnable mRefreshMusicView = new Runnable() {

		public void run() {
			while (isRefreshMusicView) {
				try {
					if (refreshListener != null) {
						int mesc;
						if (isPause) {
							mesc = mMesc;
						} else {
							mesc = getStartTime();
						}
						refreshListener.refresh(mesc, getMaxTime(), isPause,
								mMode);
					}
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};

	private Runnable mReciverCloud = new Runnable() {

		public void run() {
			new Thread(new Runnable() {
				public void run() {
					getWeather();
				}
			}).start();
		}
	};

	public interface RefreshListener {
		public void refresh(int startTime, int maxTime, boolean isPause,
				PlayMode mode);

		public void setPlayMusicPostion(int position, boolean isNeedPlay);

		public void refreshWether(int code, int temp);
	}

	private RefreshListener refreshListener = null;

	public void setRefreshListener(RefreshListener refreshListener) {
		this.refreshListener = refreshListener;
	}

	@Override
	public IBinder onBind(Intent intent) {
		mMusics = FilesUtil.getDataMusics(this);
		if (mMusics == null) {
			mCount = 0;
		} else {
			mCount = mMusics.size();
		}

		mHandler.post(mReciverCloud);
		return new LocalBinder();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return true;
	}

	@Override
	public void onRebind(Intent intent) {
		mMusics = FilesUtil.getDataMusics(this);
		if (mMusics == null) {
			mCount = 0;
		} else {
			mCount = mMusics.size();
		}
		super.onRebind(intent);
	}

	public class LocalBinder extends Binder {
		public LauncherService getService() {
			return LauncherService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		filter.addDataScheme("file");
		registerReceiver(mReciver, filter);
		isRefreshMusicView = true;
		new Thread(mRefreshMusicView).start();
	}

	@Override
	public void onDestroy() {
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
		}
		unregisterReceiver(mReciver);
		isRefreshMusicView = false;
		super.onDestroy();
	}

	public void play(int position) {
		synchronized (lock) {
			if (isPause && mCurPosition == position && mPlayer != null) {
				isPause = false;
				mPlayer.start();
				mPlayer.seekTo(mMesc);
			} else {
				stop();
				if (mCount == 0) {
					return;
				}
				mCurPosition = (position + mCount) % mCount;
				try {
					mPlayer = new MediaPlayer();
					mPlayer.setDataSource(mMusics.get(mCurPosition).getUrl());
					mPlayer.prepare();
					mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

						public boolean onError(MediaPlayer player, int what,
								int arg2) {
							stop();
							if (refreshListener != null)
							refreshListener.refresh(0, getMaxTime(), true,
									mMode);
							return false;
						}
					});
					mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

						public void onCompletion(MediaPlayer mp) {
							stop();
							int position = 0;
							if (mCount == 0) {
								return;
							}
							if (mMode == PlayMode.ALL) {
								position = (mCurPosition + 1) % mCount;
							} else if (mMode == PlayMode.RANDOM) {
								java.util.Random r = new java.util.Random();
								position = (Math.abs(r.nextInt()) % mCount);
							} else {
								position = mCurPosition;
							}
							if (refreshListener != null) {
								refreshListener.setPlayMusicPostion(position, true);
								mMesc = 0;
							}
						}
					});
					mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

						public void onPrepared(MediaPlayer player) {
							isPause = false;
							player.start();
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
					stop();
				}

			}
		}
	}

	public void playID(long id) {
		for (int i = 0; i < mMusics.size(); i++) {
			if (mMusics.get(i).getId() == id) {
				refreshListener.setPlayMusicPostion(i, true);
				mMesc = 0;
				this.play(i);
			}
		}
	}

	public void playSeekTo(int msec) {
		synchronized (lock) {
			if (mPlayer != null) {
				if (!isPause) {
					mPlayer.seekTo(msec);
				} else {
					mMesc = msec;
				}
			} else {
				mMesc = msec;
			}
		}
	}

	public void deleteMusicForID(int id) {
		synchronized (lock) {
			int deleteId = -1;
			for (int i = 0; i < mMusics.size(); i++) {
				if (mMusics.get(i).getId() == id) {
					deleteId = i;
					break;
				}
			}
			if (deleteId != -1) {
				mMusics.remove(deleteId);
				mCount = mMusics.size();
			}
		}
	}

	public void pause() {
		synchronized (lock) {
			if (mPlayer != null && mPlayer.isPlaying()) {
				isPause = true;
				mMesc = mPlayer.getCurrentPosition();
				mPlayer.pause();
			}
		}
	}

	public void stop() {
		synchronized (lock) {
			if (mPlayer != null) {
				isPause = false;
				mPlayer.stop();
				mPlayer.release();
				mPlayer = null;
			}
		}
	}

	public int getStartTime() {
		synchronized (lock) {
			if (mPlayer != null) {
				return mPlayer.getCurrentPosition();
			}
			return 0;
		}
	}

	public int getMaxTime() {
		if (mPlayer != null) {
			return mPlayer.getDuration();
		}
		if (mCurPosition >= 0 && mCurPosition < mCount)
			return (int) mMusics.get(mCurPosition).getTime();
		return 0;
	}

	public Music getPlayMusic(int position) {
		synchronized (lock) {
			if (mCount == 0) {
				return null;
			}
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

	public void changeMode() {
		if (mMode == PlayMode.ALL) {
			mMode = PlayMode.RANDOM;
		} else if (mMode == PlayMode.RANDOM) {
			mMode = PlayMode.REPEAT;
		} else {
			mMode = PlayMode.ALL;
		}
	}

	public PlayMode getMode() {
		return mMode;
	}

	private void getWeather() {
		String city = HttpHelper.getCity();
		String woeid = null;
		if (city == null) {
			mHandler.postDelayed(mReciverCloud, 3000);// 60 * 1000 * 2);
			return;
		} else {
			woeid = HttpHelper.getWoeid(city);
			if (woeid != null)
				woeid = xmlToString(woeid);
			else {
				mHandler.postDelayed(mReciverCloud, 3000);// 60 * 1000 * 2);
				return;
			}
		}
		try {
			woeid = woeid.substring(woeid.lastIndexOf("woeid") + 6,
					woeid.lastIndexOf("woeid") + 14);
			String wether = HttpHelper.getWeather(woeid);
			if (wether != null) {
				xmlToStrings(wether);
			} else {
				mHandler.postDelayed(mReciverCloud, 60 * 1000 * 2);
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			mHandler.postDelayed(mReciverCloud, 3000);// 60 * 1000 * 2);
			return;
		}
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
			refreshListener.refreshWether(code, temp);
			mHandler.postDelayed(mReciverCloud, 60 * 1000 * 60);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}

	private class DataReciver extends BroadcastReceiver {

		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {

			} else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {

			} else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
				mMusics = FilesUtil.getDataMusics(LauncherService.this);
				if (mMusics == null) {
					mCount = 0;
					refreshListener.setPlayMusicPostion(0, false);
					mMesc = 0;
					stop();
				} else {
					mCount = mMusics.size();
					refreshListener.setPlayMusicPostion(mCurPosition, false);
					mMesc = 0;
				}
			}
		}

	}

}
