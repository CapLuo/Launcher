package com.android.custom.launcher.services;

import java.io.IOException;
import java.util.ArrayList;

import com.anddroid.custom.launcher.util.FilesUtil;
import com.anddroid.custom.launcher.util.Music;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

public class LauncherService extends Service {

    public static final String ACTION = "com.android.launcher.music";
    public static final int MUSIC_COMPLETE_ACTION = 1;

    private ArrayList<Music> mMusics = new ArrayList<Music>();
    private int mCurPosition = 0, mCount = 0;
    private MediaPlayer mPlayer;
    private Messenger mMessenger = null;

    @Override
    public IBinder onBind(Intent intent) {
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
        if (mCurPosition == position) {
            mPlayer.start();
        } else {
            stop();
            mCurPosition = (position + mMusics.size()) % mMusics.size();
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
                    int position = (mCurPosition + 1) % mCount;
                	sendMusicCompletionMSG(position);
                }
            });
            mPlayer.start();
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
        if (mPlayer != null && mPlayer.isPlaying()) {
            mPlayer.pause();
        }
    }

    public void stop() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    public int getStartTime() {
        if (mPlayer != null) {
            return mPlayer.getCurrentPosition();
        }
        return 0;
    }

    public Music getPlayMusic(int position) {
    	if (mMusics.size() <= 0 || mMusics.size() <= position) {
    		return null;
    	}
        return mMusics.get(position);
    }

    public boolean isPlaying() {
        if (mPlayer != null) {
            return mPlayer.isPlaying();
        } else {
            return false;
        }
    }

    public int getPosition() {
    	return mCurPosition;
    }

}
