package com.example.setting.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.provider.MediaStore.Images.Thumbnails;
import android.view.View;
import android.widget.ImageView;

import com.example.setting.adapter.MyMedia;

public class ImageAsyncTask extends AsyncTask<Void, Void, Boolean> {
	private MyMedia myMedia;
	private int imageWidth;
	private int imageHeight;
	private Context mContext;
	private String tag;
	private ImageView imageView;
	private ImageView imgLogoPlay;

	public ImageAsyncTask(Context context, MyMedia myMedia, int imageWidth,
			int imageHeight, ImageView imageView, ImageView imgLogoPlay) {
		this.mContext = context;
		this.myMedia = myMedia;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		this.imageView = imageView;
		this.imgLogoPlay = imgLogoPlay;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		// if(tag != null)
		// Log.i("Catch", "doInBackground:"+tag);
		if(myMedia == null)
			return false;
		
		if (myMedia.getMediaType() == MyMedia.TYPE_VIDEO_N) {
			String path = myMedia.getPath();
			if(path == null)
				return false;
			Bitmap bitmap = MediaHelper.getVideoThumbnail(imageWidth,
					imageHeight, path, Thumbnails.MICRO_KIND);
			if (bitmap != null) {
				BitmapDrawable bd = new BitmapDrawable(bitmap);
				myMedia.setImage(bd);
				myMedia.setMediaType(MyMedia.TYPE_VIDEO);
				return true;
			}
		} else if(myMedia.getMediaType() == MyMedia.TYPE_MUSIC_N){
			String albumArtPath = myMedia.getAlbumArtPath();
			if(albumArtPath == null)
				return false;
			Bitmap bitmap = MediaHelper.getImageThumbnail(imageWidth, imageHeight, albumArtPath);
			if (bitmap != null) {
				BitmapDrawable bd = new BitmapDrawable(bitmap);
				myMedia.setImage(bd);
				myMedia.setMediaType(MyMedia.TYPE_MUSIC);
				return true;
			}
		} else if(myMedia.getMediaType() == MyMedia.TYPE_GALLERY_N){
			String path = myMedia.getPath();
			if(path == null)
				return false;
			Bitmap bitmap = MediaHelper.getImageThumbnail(imageWidth, imageHeight,
					path);
			if (bitmap != null) {
				BitmapDrawable bd = new BitmapDrawable(bitmap);
				myMedia.setImage(bd);
				myMedia.setMediaType(MyMedia.TYPE_GALLERY);
				return true;
			}
		}
		return false;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		// if(tag != null)
		// Log.i("Catch", "onPostExecute:"+tag+"  "+result);
		if(result){
			imageView.setBackgroundDrawable((BitmapDrawable)myMedia.getImage());
		}
		
		if(myMedia.getMediaType() == MyMedia.TYPE_VIDEO){
			imgLogoPlay.setVisibility(View.VISIBLE);
		}
		super.onPostExecute(result);
	}

}