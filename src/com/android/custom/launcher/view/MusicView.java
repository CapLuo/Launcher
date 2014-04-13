package com.android.custom.launcher.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.anddroid.custom.launcher.util.FilesUtil;
import com.anddroid.custom.launcher.util.Music;
import com.anddroid.custom.launcher.util.MusicUtil;
import com.android.custom.launcher.R;
import com.example.setting.ItemListActivity;

public class MusicView extends LinearLayout implements OnClickListener {

    private TextView mMusicName, mSinger, mTime;
    private ImageView mDrawable;
    private ImageButton mPlay, mNext, mPrev, mList, mVolume;
    private SeekBar mVolumeBar, mPlayBar;
    private boolean mSoundEnabled = true;
    private boolean isPlaying = false;
    private int volume;
    private Music mCurrentMusic = null;

    private MusicControl mControl = new MusicControl() {
        public void stop() {}
        public void play(int position) {}
        public void pause() {}
        public int getTime() { return 0;}
		public Music getCurrentMusic(int position) { return null;}
		public int getPosition() { return 0;}
    };

    public void setOnMusicControl(MusicControl c) {
        mControl = c;
    }

    public interface MusicControl {
        public void play(int position);
        public void pause();
        public void stop();
        public int getTime();
        public int getPosition();
        public Music getCurrentMusic(int position);
    }

    public MusicView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.music_view, this);
        initView();
    }

    public MusicView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.music_view, this);
        initView();
    }

    public MusicView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.music_view, this);
        initView();
    }

    private void initView() {
        mMusicName = (TextView) findViewById(R.id.music_name);
        mSinger = (TextView) findViewById(R.id.music_singer);
        mTime = (TextView) findViewById(R.id.music_time);

        mDrawable = (ImageView) findViewById(R.id.music_image);

        mPlayBar = (SeekBar) findViewById(R.id.music_seekbar);
        mVolumeBar = (SeekBar) findViewById(R.id.music_volume_seekbar);

        mPlay = (ImageButton) findViewById(R.id.music_play);
        mPlay.setOnClickListener(this);
        mNext = (ImageButton) findViewById(R.id.music_next);
        mNext.setOnClickListener(this);
        mPrev = (ImageButton) findViewById(R.id.music_prev);
        mPrev.setOnClickListener(this);
        mList = (ImageButton) findViewById(R.id.music_list);
        mList.setOnClickListener(this);
        mVolume = (ImageButton) findViewById(R.id.music_volume);
        mVolume.setOnClickListener(this);
        getSoundEnable();
    }

    public void setCurrentMusic(Music music) {
        if (music != null) {
        	mCurrentMusic = music;
        	fillMusicInfo();
        }
    }

    private void fillMusicInfo() {
    	mMusicName.setText(mCurrentMusic.getName());
    	mSinger.setText("- " + mCurrentMusic.getSinger());
    	Bitmap bm = FilesUtil.getAlbumArt(getContext(), mCurrentMusic.getAlbumID());
    	if (bm == null) {
    		mDrawable.setImageResource(R.drawable.music_play_picture_default);
    	} else {
    		mDrawable.setImageBitmap(bm);
    	}
    	mTime.setText(MusicUtil.formatTime(mCurrentMusic.getTime()));
    	mPlayBar.setMax((int) mCurrentMusic.getTime());
    	mPlayBar.setProgress(0);
    }

    public void onClick(View v) {
        if (v.getId() == R.id.music_list) {
			gotoMusicList();
		} else if (v.getId() == R.id.music_play) {
			if (isPlaying) {
				mPlay.setImageResource(R.drawable.drawable_music_pause_button);
                mControl.pause();
            } else {
            	mPlay.setImageResource(R.drawable.drawable_music_paly_button);
            	mControl.play(mControl.getPosition());
            }
		} else if (v.getId() == R.id.music_next) {
			int position = mControl.getPosition() + 1;
			setCurrentMusic(mControl.getCurrentMusic(position));
			mControl.play(position);
		} else if (v.getId() == R.id.music_prev) {
			int position = mControl.getPosition() + 1;
			setCurrentMusic(mControl.getCurrentMusic(position));
			mControl.play(position);
		} else if (v.getId() == R.id.music_volume) {
			adjustSoundEnable();
		} else {
		}
    }

    private void gotoMusicList() {
        Intent intent = new Intent();
        intent.setClass(getContext(), ItemListActivity.class);
        intent.putExtra("position", 1);
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
            mVolumeBar.setProgress(volume);
            changeViewSrc(mVolume, R.drawable.drawable_music_volume_button);
        } else {
            volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            mVolumeBar.setProgress(0);
            changeViewSrc(mVolume, R.drawable.drawable_music_volume_button_mute);
        }
        mSoundEnabled = !mSoundEnabled;
    }

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

    public void isFirstViewMode(boolean mode) {
    	if (mode) {
    		findViewById(R.id.music_view).setVisibility(View.INVISIBLE);
    		findViewById(R.id.music_view_default).setVisibility(View.GONE);
    	} else {
    		findViewById(R.id.music_view).setVisibility(View.GONE);
    		findViewById(R.id.music_view_default).setVisibility(View.INVISIBLE);
    	}
    }

    public void refreshSeekBar(int milliseconds) {
        mPlayBar.setProgress(milliseconds);
        mTime.setText(MusicUtil.formatTime(milliseconds));
    }

}
