package com.android.custom.launcher.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.android.custom.launcher.util.BitmapUtils.ImageHolder;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Audio.AudioColumns;

public class FilesUtil {

	public static ArrayList<Music> getDataMusics(Context context){
		ArrayList<Music> list = new ArrayList<Music>();
		ContentResolver cr = context.getContentResolver();
		if(cr != null){
			Cursor cursor = cr.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
					null, null, null, null);
			if (cursor == null) {
				return null;
			}
			cursor.moveToFirst();
			while(cursor.moveToNext()){
				
				int id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
				String title = cursor.getString(cursor
						.getColumnIndex(MediaColumns.TITLE));
				String name = cursor.getString(cursor.
						getColumnIndex(MediaColumns.DISPLAY_NAME));
				String singer = cursor.getString(cursor
						.getColumnIndex(AudioColumns.ARTIST));
				if(singer.equals("<unknown>")){
					singer = "unknown";
				}
				String album = cursor.getString(cursor
						.getColumnIndex(AudioColumns.ALBUM));
				String albumId = cursor.getString(cursor
						.getColumnIndexOrThrow(AudioColumns.ALBUM_ID));
				long size = cursor.getLong(cursor
						.getColumnIndex(MediaColumns.SIZE));
				long time = cursor.getLong(cursor
						.getColumnIndex(AudioColumns.DURATION));
				String url = cursor.getString(cursor
						.getColumnIndex(MediaColumns.DATA));
				Music m = new Music();
				m.setId(id);
				m.setName(name);
				m.setSinger(singer);
				m.setSize(size);
				m.setTime(time);
				m.setTitle(title);
				m.setAlbum(album);
				m.setAlbumID(albumId);
				m.setUrl(url);
				list.add(m);
			}
			cursor.close();
		}
		return list;
	}

	/**
	 * delete music
	 */
	public static void delete(Context context, Music music){
		ContentResolver cr = context.getContentResolver();
		Uri uri = MediaStore.Audio.Media.getContentUriForPath(music.getUrl());
		long s = cr.delete(uri, BaseColumns._ID + "=" + music.getId(), null);
		System.out.println("data..........ok"+s);
	}

	public static void getDataVideos(Context context) {
		ContentResolver cr = context.getContentResolver();
		if (cr != null) {
			Cursor cursor = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
			        null, null, null, null);
			if (cursor == null) {
				//FIXME 
				return;
			}
			cursor.moveToFirst();
			while(cursor.moveToNext()) {
				String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
				String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
				String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
			}
			cursor.close();
		}
	}

	public static List<HashMap<String, String>> getDataImages(Context context) {
		ContentResolver cr = context.getContentResolver();
		String[] projection = { MediaStore.Images.Media._ID, 
	            MediaStore.Images.Media.DISPLAY_NAME, 
	            MediaStore.Images.Media.DATA, MediaStore.Images.Media.SIZE };
		String selection = MediaStore.Images.Media.MIME_TYPE + "=?";
		String[] selectionArgs = { "image/jpeg" };
		String sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " desc";
		List<HashMap<String, String>> imageList = new ArrayList<HashMap<String, String>>();
		if (cr != null) {
			Cursor cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
					projection, selection, selectionArgs, sortOrder);
			if (cursor == null) {
				return null;
			}
			cursor.moveToFirst();
			while(cursor.moveToNext()) {
				HashMap<String, String> imageMap = null;
				imageMap = new HashMap<String, String>();
				imageMap.put(BitmapUtils.IMAGE_ID, cursor.getString(cursor 
                        .getColumnIndex(MediaStore.Images.Media._ID)));
				imageMap.put(BitmapUtils.IMAGE_NAME, cursor.getString(cursor 
                        .getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)));
				imageMap.put(BitmapUtils.IMAGE_SIZE, "" + cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media.SIZE) / 1024));
				imageMap.put(BitmapUtils.IMAGE_PATH, cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA)));
				imageList.add(imageMap);
			}
			cursor.close();
		}
		return imageList;
	}

	public static void delete(Context context, ImageHolder image){
		ContentResolver cr = context.getContentResolver();
		Uri uri = MediaStore.Audio.Media.getContentUriForPath(image.getPath());
		long s = cr.delete(uri, BaseColumns._ID + "=" + image.getID(), null);
		System.out.println("data..........ok"+s);
	}

	public  static Bitmap getAlbumArt(Context context, String album_id) {
		String mUriAlbums = "content://media/external/audio/albums";
		String[] projection = new String[] { "album_art" };
		Cursor cur = context.getContentResolver().query(
				Uri.parse(mUriAlbums + "/" + album_id), projection, null, null,
				null);
		String album_art = null;
		if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
			cur.moveToNext();
			album_art = cur.getString(0);
		}
		cur.close();
		cur = null;

		if (album_art == null)
			return null;

		Bitmap bm = BitmapFactory.decodeFile(album_art);
		return bm;
	}
}

