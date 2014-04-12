package com.android.custom.launcher.services;

import java.io.IOException;
import java.util.ArrayList;

import com.anddroid.custom.launcher.util.FilesUtil;
import com.anddroid.custom.launcher.util.Music;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

public class LauncherService extends Service {

    public static final String ACTION = "com.android.launcher.music";

    private ArrayList<Music> mMusics = new ArrayList<Music>();
    private int mCurPosition = -1, mCount = 0;
    private MediaPlayer mPlayer;

    @Override
    public IBinder onBind(Intent intent) {
        mMusics = FilesUtil.getDataMusics(this);
        mCount = mMusics.size();
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
            mCurPosition = position;
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
                    mPlayer.stop();
                    int position = (mCurPosition + 1) % mCount;
                    play(position);
                }
            });
            mPlayer.start();
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

    public Music getPlayMusic() {
        return mMusics.get(mCurPosition);
    }

    public boolean isPlaying() {
        if (mPlayer != null) {
            return mPlayer.isPlaying();
        } else {
            return false;
        }
    }

}
