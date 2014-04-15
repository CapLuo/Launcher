package com.android.custom.launcher.services;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

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
	private boolean isPause = false;

	private boolean isRefreshMusicView = true;
	private Runnable mRefreshMusicView = new Runnable() {

		public void run() {
			while (isRefreshMusicView) {
				try {
					if(refreshListener != null){
						refreshListener.refresh(getStartTime(), getMaxTime(), isPause, mMode);
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
			new GetWetherInNet().execute();
		}
	};

	public interface RefreshListener{
		public void refresh(int startTime, int maxTime, boolean isPause, PlayMode mode);
		public void setPlayMusicPostion(int position);
		public void refreshWether(int code, int temp);
	}
	private RefreshListener refreshListener = null;
	public void setRefreshListener(RefreshListener refreshListener){
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
		if (mPlayer != null && mPlayer.isPlaying()) {
			isRefreshMusicView = true;
			new Thread(mRefreshMusicView).start();
		}
		super.onRebind(intent);
	}

	public class LocalBinder extends Binder {
		public LauncherService getService() {
			return LauncherService.this;
		}
	}
	
	@Override
	public void onDestroy() {
		mPlayer.release();
		mPlayer = null;
		super.onDestroy();
	}


	public void play(int position) {
		synchronized (lock) {

			if (isPause && mCurPosition == position) {
				isPause = false;
				mPlayer.start();
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
							refreshListener.setPlayMusicPostion(position);
						}
					}
				});
				isPause = false;
				mPlayer.start();
				isRefreshMusicView = true;
				new Thread(mRefreshMusicView).start();

			}
		}
	}

	public void playID(long id) {
		for (int i = 0; i < mMusics.size(); i++) {
			if (mMusics.get(i).getId() == id) {
				this.play(i);
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
				mPlayer.pause();
			}
		}
	}

	public void stop() {
		synchronized (lock) {
			isRefreshMusicView = false;
			if (mPlayer != null && mPlayer.isPlaying()) {
				isPause = false;
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

	public void changeMode () {
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
		String woeid = HttpHelper.getWoeid(city);
		if (woeid != null) {
			woeid = xmlToString(woeid);
		} else {
			mHandler.postDelayed(mReciverCloud, 60 * 1000 * 2);
			return;
		}
		woeid = woeid.substring(woeid.lastIndexOf("woeid") + 6, woeid.lastIndexOf("woeid") + 14);
		String wether = HttpHelper.getWeather(woeid);
		if (wether != null) {
			xmlToStrings(wether);
		} else {
			mHandler.postDelayed(mReciverCloud, 60 * 1000 * 2);
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
		} catch (DocumentException e) {
			e.printStackTrace();
		}
    }
   
    private class GetWetherInNet extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			getWeather();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mHandler.postDelayed(mReciverCloud, 60 * 1000 * 60);
			super.onPostExecute(result);
		}
    	
    }
}
