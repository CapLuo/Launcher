package com.android.custom.launcher.view;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.custom.launcher.R;
import com.android.custom.launcher.services.LauncherService;

public class MusicView extends LinearLayout implements OnClickListener {

    private TextView mMusicName, mSinger, mTime;
    private ImageView mPlay, mNext, mPrev, mList, mVolume;
    private SeekBar mVolumeBar, mPlayBar;
    private int position = 0;
    private boolean mSoundEnabled = true;
    private boolean isPlaying = false;
    private int volume;

    private MusicControl mControl = new MusicControl() {
        public void stop() {}
        public void play(int position) {}
        public void pause() {}
        public int getTime() { return 0;}
        public LauncherService  
    };

    public void setOnMusicControl(MusicControl c) {
        mControl = c;
    }

    public interface MusicControl {
        public void play(int position);
        public void pause();
        public void stop();
        public int getTime();
    }

    public MusicView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.music_view, this);
    }

    public MusicView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.music_view, this);
    }

    public MusicView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.music_view, this);
    }

    private void initView() {
        mMusicName = (TextView) findViewById(R.id.music_name);
        mSinger = (TextView) findViewById(R.id.music_singer);
        mTime = (TextView) findViewById(R.id.music_time);

        mPlayBar = (SeekBar) findViewById(R.id.music_seekbar);
        mVolumeBar = (SeekBar) findViewById(R.id.music_volume_seekbar);

        mPlay = (ImageView) findViewById(R.id.music_play);
        mPlay.setOnClickListener(this);
        mNext = (ImageView) findViewById(R.id.music_next);
        mNext.setOnClickListener(this);
        mPrev = (ImageView) findViewById(R.id.music_prev);
        mPrev.setOnClickListener(this);
        mList = (ImageView) findViewById(R.id.music_list);
        mList.setOnClickListener(this);
        mVolume = (ImageView) findViewById(R.id.music_volume);
        mVolume.setOnClickListener(this);
        getSoundEnable();
    }

    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.music_list:
            gotoMusicList();
            break;
        case R.id.music_play:
            if (isPlaying) {
                mControl.pause();
            } 
            break;
        case R.id.music_next:
            position++;
            break;
        case R.id.music_prev:
            position--;
            break;
        case R.id.music_volume:
            adjustSoundEnable();
            break;
        default:
            break;
        }
    }

    private void gotoMusicList() {
        Intent intent = new Intent();
        getContext().startActivity(intent);
    }

    private void adjustSoundEnable() {
        AudioManager mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        if (!mSoundEnabled) {
            if (volume == 0) {
                volume = (int) (mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 4);
                if (volume == 0) {
                    volume = 1;
                }
            }
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
            changeViewSrc(mVolume, R.drawable.drawable_music_volume_button);
        } else {
            volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            changeViewSrc(mVolume, R.drawable.drawable_music_volume_button_mute);
        }
        mSoundEnabled = !mSoundEnabled;
    }

    //init view
    private void getSoundEnable() {
        AudioManager mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        int progress = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        volume = progress;
        int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mVolumeBar.setMax(max);
        mVolumeBar.setProgress(progress);
        if (progress == 0) {
            mSoundEnabled = false;
            changeViewSrc(mVolume, R.drawable.drawable_music_volume_button_mute);
        } else {
            mSoundEnabled = true;
            changeViewSrc(mVolume, R.drawable.drawable_music_volume_button);
        }
    }

    private void adjustVolumeUp() {
        AudioManager mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,
                AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
	}

    private void adjustVolumeDown() {
        AudioManager mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,
                AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            adjustSoundEnable();
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            adjustVolumeDown();
            getSoundEnable();
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            adjustVolumeUp();
            getSoundEnable();
        }
        return super.onKeyDown(keyCode, event);
    }

    private void changeViewSrc(ImageView view, int resources) {
        view.setImageResource(resources);
    }

}
