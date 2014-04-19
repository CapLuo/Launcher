package com.example.setting.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.android.custom.launcher.R;
import com.example.setting.PlayMusicActivity;
import com.example.setting.adapter.MyMedia;

public class MediaHelper {
	private Context mContext;
	private ContentResolver mContentResolver;
	public final int imageWidth;
	public final int imageHeight;

	public MediaHelper(Context context, int imageWidth, int imageHeight) {
		this.mContext = context;
		this.mContentResolver = context.getContentResolver();
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
	}

	private String getAlbumArtPath(String album_id) {
		Cursor cur = null;
		String album_art = null;
		try {
			String mUriAlbums = "content://media/external/audio/albums";
			String[] projection = new String[] { "album_art" };
			cur = mContentResolver.query(
					Uri.parse(mUriAlbums + "/" + album_id), projection, null,
					null, null);
			if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
				cur.moveToNext();
				album_art = cur.getString(0);
			}
		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			if (cur != null)
				cur.close();
		}
		return album_art;
	}

	/**
	 * 根据指定的图像路径和大小来获取缩略图 此方法有两点好处： 1.
	 * 使用较小的内存空间，第一次获取的bitmap实际上为null，只是为了读取宽度和高度，
	 * 第二次读取的bitmap是根据比例压缩过的图像，第三次读取的bitmap是所要的缩略图。 2.
	 * 缩略图对于原图像来讲没有拉伸，这里使用了2.2版本的新工具ThumbnailUtils，使 用这个工具生成的图像不会被拉伸。
	 * 
	 * @param imagePath
	 *            图像的路径
	 * @param width
	 *            指定输出图像的宽度
	 * @param height
	 *            指定输出图像的高度
	 * @return 生成的缩略图
	 */
	public static Bitmap getImageThumbnail(int width, int height,
			String imagePath) {
		Bitmap bitmap = null;
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			// 获取这个图片的宽和高，注意此处的bitmap为null
			bitmap = BitmapFactory.decodeFile(imagePath, options);
			options.inJustDecodeBounds = false; // 设为 false
			// 计算缩放比
			int h = options.outHeight;
			int w = options.outWidth;
			int beWidth = w / width;
			int beHeight = h / height;
			int be = 1;
			if (beWidth < beHeight) {
				be = beWidth;
			} else {
				be = beHeight;
			}
			if (be <= 0) {
				be = 1;
			}
			options.inSampleSize = be;
			// 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
			bitmap = BitmapFactory.decodeFile(imagePath, options);
			// 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
			bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
					ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	// 图片圆角处理
	public static Bitmap getRoundedBitmap(Bitmap mBitmap, float roundPx) {
		try {
			// 创建新的位图
			Bitmap bgBitmap = Bitmap.createBitmap(mBitmap.getWidth(),
					mBitmap.getHeight(), Config.ARGB_8888);
			// 把创建的位图作为画板
			Canvas mCanvas = new Canvas(bgBitmap);

			Paint mPaint = new Paint();
			Rect mRect = new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
			RectF mRectF = new RectF(mRect);
			// 设置圆角半径为20
			// float roundPx = 15;
			mPaint.setAntiAlias(true);
			// 先绘制圆角矩形
			mCanvas.drawRoundRect(mRectF, roundPx, roundPx, mPaint);

			// 设置图像的叠加模式
			mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
			// 绘制图像
			mCanvas.drawBitmap(mBitmap, mRect, mRect, mPaint);
			return bgBitmap;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取视频的缩略图 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
	 * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
	 * 
	 * @param videoPath
	 *            视频的路径
	 * @param width
	 *            指定输出视频缩略图的宽度
	 * @param height
	 *            指定输出视频缩略图的高度度
	 * @param kind
	 *            参照MediaStore.Images.Thumbnails类中的常量MINI_KIND和MICRO_KIND。
	 *            其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
	 * @return 指定大小的视频缩略图
	 */
	public static Bitmap getVideoThumbnail(int width, int height,
			String videoPath, int kind) {
		Bitmap bitmap = null;
		// 获取视频的缩略图
		try {
			bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
			bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
					ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	private String getRegexp(String path) {
		return "^" + path + "[^//]{1,}";
	}

	public List<MyMedia> loadMedia(String path, int type, boolean isRoot) {
		switch (type) {
		case MyMedia.TYPE_VIDEO:
			return loadVideo(path, isRoot);
		case MyMedia.TYPE_MUSIC:
			return loadMusic(path, isRoot);
		case MyMedia.TYPE_GALLERY:
			return loadGallery(path, isRoot);
		case MyMedia.TYPE_OTHER:
			return loadOther(path, isRoot);
		}
		return null;
	}

	public List<MyMedia> loadAllMedia(String path, int type) {
		switch (type) {
		case MyMedia.TYPE_VIDEO:
			return loadAllVideo(path);
		case MyMedia.TYPE_MUSIC:
			return loadAllMusic(path);
		case MyMedia.TYPE_GALLERY:
			return loadAllGallery(path);
		case MyMedia.TYPE_OTHER:
			return loadAllOther(path);
		}
		return null;
	}

	private String getDirNameInPath(String abPath, String path) {
		String str = abPath.substring(path.length());
		return str.substring(0, str.indexOf("/"));
	}

	private String getFileNameInPath(String abPath) {
		return abPath.substring(abPath.lastIndexOf("/") + 1);
	}

	public List<MyMedia> loadVideo(String path, boolean isRoot) {
		List<MyMedia> listDir = new ArrayList<MyMedia>();
		Map<String, Object> dirMap = new HashMap<String, Object>();
		List<MyMedia> listFile = new ArrayList<MyMedia>();
		Cursor cursor = null;

		try {
			// ContentProvider只能由ContentResolver发送请求
			Uri VIDEO_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
			// 获取音频文件的URI,MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
			// 视频 MediaStore.Video.Media.EXTERNAL_CONTENT_URI
			// 图片MediaStore.Images.Media.EXTERNAL_CONTENT_URI
			String[] columns = new String[] { MediaStore.Video.Media._ID,
					MediaStore.Video.Media.DISPLAY_NAME,
					MediaStore.Video.Media.DATA,
					MediaStore.Video.Media.MIME_TYPE };
			// 要读的列名,这些常量可以查GOOGLE官方开发文档,TITLE是标题 DATA是路径//REGEXP
			cursor = mContentResolver.query(VIDEO_URI, columns,
					MediaStore.Video.Media.DATA + " LIKE ?",
					new String[] { path + "%" }, null);

			while (cursor.moveToNext()) {
				// 循环读取第一列,即文件路径,0列是标题
				String id = cursor.getString(0);
				String name = cursor.getString(1);
				String abPath = cursor.getString(2);
				String mimeType = cursor.getString(3);
				// 判断该文件是不是当前文件夹下的文件，如果是当前文件夹下的文件则只显示出文件夹
				if (abPath.matches(getRegexp(path))) {
					MyMedia myMedia = new MyMedia();
					// Bitmap bitmap = getVideoThumbnail(imageWidth,
					// imageHeight,
					// abPath, Thumbnails.MICRO_KIND);
					// if (bitmap != null) {
					// BitmapDrawable bd = new BitmapDrawable(bitmap);
					// myMedia.setImage(bd);
					// myMedia.setMediaType(MyMedia.TYPE_VIDEO);
					// } else {
					// myMedia.setImage(R.drawable.otherfiles_file);
					// myMedia.setMediaType(MyMedia.TYPE_VIDEO_N);
					// }
					myMedia.setImage(R.drawable.otherfiles_file);
					myMedia.setMediaType(MyMedia.TYPE_VIDEO_N);
					myMedia.setId(id);
					myMedia.setName(name);
					myMedia.setPath(abPath);
					myMedia.setMimeType(mimeType);
					listFile.add(myMedia);
				} else {
					String str = abPath.substring(path.length());
					String dirName = str.substring(0, str.indexOf("/"));
					if (dirMap.get(dirName) == null) {
						dirMap.put(dirName, 1);
					} else {
						int count = ((Integer) dirMap.get(dirName)).intValue();
						dirMap.put(dirName, count + 1);
					}
				}
			}

			Iterator iter = dirMap.entrySet().iterator();
			MyMedia myMedia;
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				Object key = entry.getKey();
				Object val = entry.getValue();
				myMedia = new MyMedia();
				myMedia.setImage(R.drawable.video_folder);
				myMedia.setName((String) entry.getKey());
				myMedia.setMediaType(MyMedia.TYPE_DIR);
				myMedia.setTotal((Integer) entry.getValue());
				listDir.add(myMedia);
			}
			listDir.addAll(listFile);

			if (isRoot && cursor.getCount() > 0) {
				// 根目录才有all的文件夹
				myMedia = new MyMedia();
				myMedia.setName("All Videos");
				myMedia.setImage(R.drawable.video_folder1);
				myMedia.setTotal(cursor.getCount());
				myMedia.setMediaType(MyMedia.TYPE_ALL);
				listDir.add(0, myMedia);
			}

		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return listDir;
	}

	public List<MyMedia> loadOther(String path, boolean isRoot) {
		List<MyMedia> listDir = new ArrayList<MyMedia>();
		Map<String, Object> dirMap = new HashMap<String, Object>();
		List<MyMedia> listFile = new ArrayList<MyMedia>();
		Cursor cursor = null;

		try {
			Uri FILES_URI = MediaStore.Files.getContentUri("external");
			String[] columns = new String[] { MediaStore.Files.FileColumns._ID,
					MediaStore.Files.FileColumns.DATA,
					MediaStore.Files.FileColumns.MIME_TYPE };
			// 要读的列名,这些常量可以查GOOGLE官方开发文档,TITLE是标题 DATA是路径//REGEXP
			cursor = mContentResolver.query(FILES_URI, columns,
					MediaStore.Files.FileColumns.MEDIA_TYPE + "="
							+ MediaStore.Files.FileColumns.MEDIA_TYPE_NONE
							+ " AND " + MediaStore.Files.FileColumns.DATA
							+ " LIKE ?", new String[] { path + "%" }, null);

			int total = 0;
			while (cursor.moveToNext()) {
				// 循环读取第一列,即文件路径,0列是标题
				String id = cursor.getString(0);
				String abPath = cursor.getString(1);
				String mimeType = cursor.getString(2);
				File file = new File(abPath);
				if (file != null && !file.isDirectory()) {
					total++;
					if (abPath.matches(getRegexp(path))) {
						MyMedia myMedia = new MyMedia();
						myMedia.setMediaType(MyMedia.TYPE_OTHER);
						myMedia.setImage(R.drawable.otherfiles_file);
						myMedia.setName(getFileNameInPath(abPath));
						myMedia.setPath(abPath);
						myMedia.setMimeType(mimeType);
						myMedia.setId(id);
						listFile.add(myMedia);
					} else {
						String dirName = getDirNameInPath(abPath, path);
						if (dirMap.get(dirName) == null) {
							dirMap.put(dirName, 1);
						} else {
							int count = ((Integer) dirMap.get(dirName))
									.intValue();
							dirMap.put(dirName, count + 1);
						}
					}
				}
			}

			Iterator iter = dirMap.entrySet().iterator();
			MyMedia myMedia;
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				Object key = entry.getKey();
				Object val = entry.getValue();
				myMedia = new MyMedia();
				myMedia.setImage(R.drawable.otherfiles_foder);
				myMedia.setName((String) entry.getKey());
				myMedia.setMediaType(MyMedia.TYPE_DIR);
				myMedia.setTotal((Integer) entry.getValue());
				listDir.add(myMedia);
			}
			listDir.addAll(listFile);

			if (isRoot && total > 0) {
				// 根目录才有all的文件夹
				myMedia = new MyMedia();
				myMedia.setName("All Files");
				myMedia.setImage(R.drawable.otherfiles_foder1);
				myMedia.setTotal(total);
				myMedia.setMediaType(MyMedia.TYPE_ALL);
				listDir.add(0, myMedia);
			}
		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return listDir;
	}

	public List<MyMedia> loadGallery(String path, boolean isRoot) {
		List<MyMedia> listDir = new ArrayList<MyMedia>();
		Map<String, Object> dirMap = new HashMap<String, Object>();
		List<MyMedia> listFile = new ArrayList<MyMedia>();
		Cursor cursor = null;

		try {
			// ContentProvider只能由ContentResolver发送请求
			Uri IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
			// 获取音频文件的URI,MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
			// 视频 MediaStore.Video.Media.EXTERNAL_CONTENT_URI
			// 图片MediaStore.Images.Media.EXTERNAL_CONTENT_URI
			String[] columns = new String[] { MediaStore.Images.Media._ID,
					MediaStore.Images.Media.DISPLAY_NAME,
					MediaStore.Images.Media.DATA,
					MediaStore.Images.Media.MIME_TYPE };
			// 要读的列名,这些常量可以查GOOGLE官方开发文档,TITLE是标题 DATA是路径//REGEXP
			cursor = mContentResolver.query(IMAGE_URI, columns,
					MediaStore.Images.Media.DATA + " LIKE ?",
					new String[] { path + "%" }, null);

			while (cursor.moveToNext()) {
				// 循环读取第一列,即文件路径,0列是标题
				String id = cursor.getString(0);
				String name = cursor.getString(1);
				String abPath = cursor.getString(2);
				String mimeType = cursor.getString(3);
				if (abPath.matches(getRegexp(path))) {
					MyMedia myMedia = new MyMedia();
//					Bitmap bitmap = getImageThumbnail(imageWidth, imageHeight,
//							abPath);
//					if (bitmap != null) {
//						BitmapDrawable bd = new BitmapDrawable(bitmap);
//						myMedia.setImage(bd);
//						myMedia.setMediaType(MyMedia.TYPE_GALLERY);
//					} else {
//						myMedia.setMediaType(MyMedia.TYPE_GALLERY_N);
//						myMedia.setImage(R.drawable.otherfiles_file);
//					}
					
					myMedia.setMediaType(MyMedia.TYPE_GALLERY_N);
					myMedia.setImage(R.drawable.otherfiles_file);

					myMedia.setId(id);
					myMedia.setName(name);
					myMedia.setPath(abPath);
					myMedia.setMimeType(mimeType);
					listFile.add(myMedia);
				} else {
					String dirName = getDirNameInPath(abPath, path);
					if (dirMap.get(dirName) == null) {
						dirMap.put(dirName, 1);
					} else {
						int count = ((Integer) dirMap.get(dirName)).intValue();
						dirMap.put(dirName, count + 1);
					}
				}
			}

			Iterator iter = dirMap.entrySet().iterator();
			MyMedia myMedia;
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				Object key = entry.getKey();
				Object val = entry.getValue();
				myMedia = new MyMedia();
				myMedia.setImage(R.drawable.gallery_folder);
				myMedia.setName((String) entry.getKey());
				myMedia.setMediaType(MyMedia.TYPE_DIR);
				myMedia.setTotal((Integer) entry.getValue());
				listDir.add(myMedia);
			}
			listDir.addAll(listFile);

			if (isRoot && cursor.getCount() > 0) {
				// 根目录才有all的文件夹
				myMedia = new MyMedia();
				myMedia.setName("All Gallery");
				myMedia.setImage(R.drawable.gallery_folder1);
				myMedia.setTotal(cursor.getCount());
				myMedia.setMediaType(MyMedia.TYPE_ALL);
				listDir.add(0, myMedia);
			}

		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return listDir;
	}

	public List<MyMedia> loadMusic(String path, boolean isRoot) {
		List<MyMedia> listDir = new ArrayList<MyMedia>();
		Map<String, Object> dirMap = new HashMap<String, Object>();
		List<MyMedia> listFile = new ArrayList<MyMedia>();
		Cursor cursor = null;

		try {
			Uri AUDIO_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
			String[] columns = new String[] { MediaStore.Audio.Media._ID,
					MediaStore.Audio.Media.DISPLAY_NAME,
					MediaStore.Audio.Media.DATA,
					MediaStore.Audio.Media.MIME_TYPE,
					MediaStore.Audio.Media.ALBUM_ID,
					MediaStore.Audio.Media.DURATION };
			// 要读的列名,这些常量可以查GOOGLE官方开发文档,TITLE是标题 DATA是路径//REGEXP
			cursor = mContentResolver.query(AUDIO_URI, columns,
					MediaStore.Audio.Media.DATA + " LIKE ?",
					new String[] { path + "%" }, null);

			while (cursor.moveToNext()) {
				// 循环读取第一列,即文件路径,0列是标题
				String id = cursor.getString(0);
				String name = cursor.getString(1);
				String abPath = cursor.getString(2);
				String mimeType = cursor.getString(3);
				String album_id = cursor.getString(4);
				int duration = cursor.getInt(5);
				if (abPath.matches(getRegexp(path))) {
					MyMedia myMedia = new MyMedia();
					String albumArtPath = getAlbumArtPath(album_id);
					// Bitmap bitmap = getImageThumbnail(imageWidth,
					// imageHeight, albumArtPath);
					// if (bitmap != null) {
					// BitmapDrawable bd = new BitmapDrawable(bitmap);
					// myMedia.setImage(bd);
					// myMedia.setMediaType(MyMedia.TYPE_MUSIC);
					// } else {
					// myMedia.setMediaType(MyMedia.TYPE_MUSIC_N);
					// myMedia.setImage(R.drawable.music_tile_picture_default);
					// }
					
					myMedia.setMediaType(MyMedia.TYPE_MUSIC_N);
					myMedia.setImage(R.drawable.music_tile_picture_default);

					myMedia.setId(id);
					myMedia.setName(name);
					myMedia.setPath(abPath);
					myMedia.setMimeType(mimeType);
					myMedia.setDuration(duration);
					myMedia.setAlbumArtPath(albumArtPath);
					listFile.add(myMedia);
				} else {
					String dirName = getDirNameInPath(abPath, path);
					if (dirMap.get(dirName) == null) {
						dirMap.put(dirName, 1);
					} else {
						int count = ((Integer) dirMap.get(dirName)).intValue();
						dirMap.put(dirName, count + 1);
					}
				}
			}

			Iterator iter = dirMap.entrySet().iterator();
			MyMedia myMedia;
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				Object key = entry.getKey();
				Object val = entry.getValue();
				myMedia = new MyMedia();
				myMedia.setImage(R.drawable.music_folder);
				myMedia.setName((String) entry.getKey());
				myMedia.setMediaType(MyMedia.TYPE_DIR);
				myMedia.setTotal((Integer) entry.getValue());
				listDir.add(myMedia);
			}
			listDir.addAll(listFile);

			if (isRoot && cursor.getCount() > 0) {
				// 根目录才有all的文件夹
				myMedia = new MyMedia();
				myMedia.setName("All Songs");
				myMedia.setImage(R.drawable.music_folder1);
				myMedia.setTotal(cursor.getCount());
				myMedia.setMediaType(MyMedia.TYPE_ALL);
				listDir.add(0, myMedia);
			}

		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return listDir;
	}

	private Uri getUri(int type) {
		switch (type) {
		case MyMedia.TYPE_VIDEO:
			return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		case MyMedia.TYPE_MUSIC:
			return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		case MyMedia.TYPE_GALLERY:
			return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		case MyMedia.TYPE_OTHER:
			return MediaStore.Files.getContentUri("external");
		}
		return null;
	}

	private int getMediaType(int type) {
		switch (type) {
		case MyMedia.TYPE_VIDEO:
			return MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
		case MyMedia.TYPE_MUSIC:
			return MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO;
		case MyMedia.TYPE_GALLERY:
			return MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
		case MyMedia.TYPE_OTHER:
			return MediaStore.Files.FileColumns.MEDIA_TYPE_NONE;
		}

		return -1;
	}

	//
	// private String getDataString(int type) {
	// switch (type) {
	// case MyMedia.TYPE_VIDEO:
	// return MediaStore.Video.Media.DATA;
	// case MyMedia.TYPE_MUSIC:
	// return MediaStore.Audio.Media.DATA;
	// case MyMedia.TYPE_GALLERY:
	// return MediaStore.Images.Media.DATA;
	// case MyMedia.TYPE_OTHER:
	// return MediaStore.Files.FileColumns.DATA;
	// }
	// return null;
	// }
	//
	// private String getNameString(int type) {
	// switch (type) {
	// case MyMedia.TYPE_VIDEO:
	// return MediaStore.Video.Media.DISPLAY_NAME;
	// case MyMedia.TYPE_MUSIC:
	// return MediaStore.Audio.Media.DISPLAY_NAME;
	// case MyMedia.TYPE_GALLERY:
	// return MediaStore.Images.Media.DISPLAY_NAME;
	// case MyMedia.TYPE_OTHER:
	// return MediaStore.Files.FileColumns.DISPLAY_NAME;
	// }
	// return null;
	// }

	public List<MyMedia> loadAllMusic(String path) {
		List<MyMedia> listFile = new ArrayList<MyMedia>();
		Cursor cursor = null;

		try {
			Uri AUDIO_URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
			String[] columns = new String[] { MediaStore.Audio.Media._ID,
					MediaStore.Audio.Media.DISPLAY_NAME,
					MediaStore.Audio.Media.DATA,
					MediaStore.Audio.Media.MIME_TYPE,
					MediaStore.Audio.Media.ALBUM_ID,
					MediaStore.Audio.Media.DURATION };
			// 要读的列名,这些常量可以查GOOGLE官方开发文档,TITLE是标题 DATA是路径//REGEXP
			cursor = mContentResolver.query(AUDIO_URI, columns,
					MediaStore.Audio.Media.DATA + " LIKE ?",
					new String[] { path + "%" }, null);

			while (cursor.moveToNext()) {
				// 循环读取第一列,即文件路径,0列是标题
				String id = cursor.getString(0);
				String name = cursor.getString(1);
				String abPath = cursor.getString(2);
				String mimeType = cursor.getString(3);
				String album_id = cursor.getString(4);
				int duration = cursor.getInt(5);
				MyMedia myMedia = new MyMedia();
				String albumArtPath = getAlbumArtPath(album_id);
				// Bitmap bitmap = getImageThumbnail(imageWidth, imageHeight,
				// albumArtPath);
				// if (bitmap != null) {
				// BitmapDrawable bd = new BitmapDrawable(bitmap);
				// myMedia.setImage(bd);
				// myMedia.setMediaType(MyMedia.TYPE_MUSIC);
				// } else {
				// myMedia.setMediaType(MyMedia.TYPE_MUSIC_N);
				// myMedia.setImage(R.drawable.music_tile_picture_default);
				// }

				myMedia.setMediaType(MyMedia.TYPE_MUSIC_N);
				myMedia.setImage(R.drawable.music_tile_picture_default);
				
				myMedia.setId(id);
				myMedia.setName(name);
				myMedia.setPath(abPath);
				myMedia.setMimeType(mimeType);
				myMedia.setDuration(duration);
				myMedia.setAlbumArtPath(albumArtPath);
				listFile.add(myMedia);
			}
		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return listFile;
	}

	public List<MyMedia> loadAllVideo(String path) {
		List<MyMedia> listFile = new ArrayList<MyMedia>();
		Cursor cursor = null;

		try {
			Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
			String[] columns = new String[] { MediaStore.Video.Media._ID,
					MediaStore.Video.Media.DISPLAY_NAME,
					MediaStore.Video.Media.DATA,
					MediaStore.Video.Media.MIME_TYPE };
			// 要读的列名,这些常量可以查GOOGLE官方开发文档,TITLE是标题 DATA是路径//REGEXP
			cursor = mContentResolver.query(uri, columns,
					MediaStore.Video.Media.DATA + " LIKE ?",
					new String[] { path + "%" }, null);

			while (cursor.moveToNext()) {
				// 循环读取第一列,即文件路径,0列是标题
				String id = cursor.getString(0);
				String name = cursor.getString(1);
				String abPath = cursor.getString(2);
				String mimeType = cursor.getString(3);
				MyMedia myMedia = new MyMedia();
//				Bitmap bitmap = getVideoThumbnail(imageWidth, imageHeight,
//						abPath, Thumbnails.MICRO_KIND);
//				if (bitmap != null) {
//					BitmapDrawable bd = new BitmapDrawable(bitmap);
//					myMedia.setImage(bd);
//					myMedia.setMediaType(MyMedia.TYPE_VIDEO);
//				} else {
//					myMedia.setMediaType(MyMedia.TYPE_VIDEO_N);
//					myMedia.setImage(R.drawable.otherfiles_file);
//				}
				
				myMedia.setMediaType(MyMedia.TYPE_VIDEO_N);
				myMedia.setImage(R.drawable.otherfiles_file);

				myMedia.setId(id);
				myMedia.setName(name);
				myMedia.setPath(abPath);
				myMedia.setMimeType(mimeType);
				listFile.add(myMedia);
			}
		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return listFile;
	}

	public List<MyMedia> loadAllGallery(String path) {
		List<MyMedia> listFile = new ArrayList<MyMedia>();
		Cursor cursor = null;

		try {
			Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
			String[] columns = new String[] { MediaStore.Images.Media._ID,
					MediaStore.Images.Media.DISPLAY_NAME,
					MediaStore.Images.Media.DATA,
					MediaStore.Images.Media.MIME_TYPE };
			// 要读的列名,这些常量可以查GOOGLE官方开发文档,TITLE是标题 DATA是路径//REGEXP
			cursor = mContentResolver.query(uri, columns,
					MediaStore.Images.Media.DATA + " LIKE ?",
					new String[] { path + "%" }, null);

			while (cursor.moveToNext()) {
				// 循环读取第一列,即文件路径,0列是标题
				String id = cursor.getString(0);
				String name = cursor.getString(1);
				String abPath = cursor.getString(2);
				String mimeType = cursor.getString(3);
				MyMedia myMedia = new MyMedia();
//				Bitmap bitmap = getImageThumbnail(imageWidth, imageHeight,
//						abPath);
//				if (bitmap != null) {
//					BitmapDrawable bd = new BitmapDrawable(bitmap);
//					myMedia.setImage(bd);
//					myMedia.setMediaType(MyMedia.TYPE_GALLERY);
//				} else {
//					myMedia.setMediaType(MyMedia.TYPE_GALLERY_N);
//					myMedia.setImage(R.drawable.otherfiles_file);
//				}

				myMedia.setMediaType(MyMedia.TYPE_GALLERY_N);
				myMedia.setImage(R.drawable.otherfiles_file);
				
				myMedia.setId(id);
				myMedia.setName(name);
				myMedia.setPath(abPath);
				myMedia.setMimeType(mimeType);
				listFile.add(myMedia);
			}
		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return listFile;
	}

	public List<MyMedia> loadAllOther(String path) {
		List<MyMedia> listFile = new ArrayList<MyMedia>();
		Cursor cursor = null;

		try {
			Uri FILES_URI = MediaStore.Files.getContentUri("external");
			String[] columns = new String[] { MediaStore.Files.FileColumns._ID,
					MediaStore.Files.FileColumns.DATA,
					MediaStore.Files.FileColumns.MIME_TYPE };
			// 要读的列名,这些常量可以查GOOGLE官方开发文档,TITLE是标题 DATA是路径//REGEXP
			cursor = mContentResolver.query(FILES_URI, columns,
					MediaStore.Files.FileColumns.MEDIA_TYPE + "="
							+ MediaStore.Files.FileColumns.MEDIA_TYPE_NONE
							+ " AND " + MediaStore.Files.FileColumns.DATA
							+ " LIKE ?", new String[] { path + "%" }, null);

			while (cursor.moveToNext()) {
				// 循环读取第一列,即文件路径,0列是标题
				String id = cursor.getString(0);
				String abPath = cursor.getString(1);
				String mimeType = cursor.getString(2);
				File file = new File(abPath);
				if (file != null && !file.isDirectory()) {
					MyMedia myMedia = new MyMedia();
					myMedia.setMediaType(MyMedia.TYPE_OTHER);
					myMedia.setImage(R.drawable.otherfiles_file);
					myMedia.setName(getFileNameInPath(abPath));
					myMedia.setPath(abPath);
					myMedia.setId(id);
					myMedia.setMimeType(mimeType);
					listFile.add(myMedia);
				}
			}

		} catch (SQLiteException e) {
			e.printStackTrace();
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return listFile;
	}

	// mediaType 为全局的,不带N
	public void play(String path, String mimeType, int mediaType, String id) {
		Uri uri = Uri.parse("file://" + path);
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(uri, mimeType);
		if (MyMedia.TYPE_MUSIC == mediaType) {
			// intent.setComponent(new ComponentName("com.android.music",
			// "com.android.music.MediaPlaybackActivity"));
			intent.putExtra("musicId", id);
			intent.putExtra("path", path);
			intent.setClass(mContext, PlayMusicActivity.class);
		} else if (MyMedia.TYPE_GALLERY == mediaType) {
//			intent.setComponent(new ComponentName("com.android.gallery",
//					"com.android.camera.ViewImage"));
			StoreUtil.savePicPath(mContext, path);
		} else if (MyMedia.TYPE_VIDEO == mediaType) {
			StoreUtil.saveVideoPath(mContext, path);
			StoreUtil.saveVideoMimeType(mContext, mimeType);
		}

		mContext.startActivity(intent);
	}

	public boolean deleteMedia(int mediaType, String id, String path) {
		Uri uri = getUri(mediaType);
		try {
			if (deleteFile(path)) {
				int s = mContentResolver.delete(uri,
						BaseColumns._ID + "=" + id, null);
				return s > 0 ? true : false;
			} else
				return false;
		} catch (Exception e) {
			Log.e("Catch", "delete:" + e.getMessage());
			Log.e("Catch", "id:" + id);
			Log.e("Catch", "path:" + path);
			Log.e("Catch", "mediaType:" + mediaType);
			Log.e("Catch", "uri:" + (uri == null ? null : uri.toString()));
			return false;
		}
	}

	// public boolean deleteMedia(int mediaType, String id, String path) {
	// Uri uri = getUri(mediaType);
	// try {
	// if(mediaType == MyMedia.TYPE_OTHER || mediaType == MyMedia.TYPE_MUSIC){
	// if(deleteFile(path)){
	// int s = mContentResolver.delete(uri, BaseColumns._ID + "=" + id,
	// null);
	// return s > 0 ? true : false;
	// }
	// return false;
	// } else {
	// int s = mContentResolver.delete(uri, BaseColumns._ID + "=" + id,
	// null);
	// return s > 0 ? true : false;
	// }
	// } catch (Exception e) {
	// Log.e("Catch", "delete:" + e.getMessage());
	// Log.e("Catch", "id:" + id);
	// Log.e("Catch", "path:" + path);
	// Log.e("Catch", "mediaType:" + mediaType);
	// Log.e("Catch", "uri:" + (uri == null ? null : uri.toString()));
	// return false;
	// }
	// }

	// public boolean deleteDirFile(int mediaType, String path) {
	// // 保证本文件夹目录不被删除
	// path = path + "/";
	// Uri uri = MediaStore.Files.getContentUri("external");
	// try {
	// int s = mContentResolver.delete(uri,
	// MediaStore.Files.FileColumns.DATA + " LIKE ? AND "
	// + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
	// + getMediaType(mediaType), new String[] { path
	// + "%" });
	// return s > 0 ? true : false;
	// } catch (Exception e) {
	// Log.e("Catch", "delete:" + e.getMessage());
	// Log.e("Catch", "path:" + path);
	// Log.e("Catch", "uri:" + (uri == null ? null : uri.toString()));
	// return false;
	// }
	// }
	//
	// public boolean deleteDir(String path) {
	// Uri uri = MediaStore.Files.getContentUri("external");
	// try {
	// Cursor c = mContentResolver.query(uri, null,
	// MediaStore.Files.FileColumns.DATA + " LIKE ? ",
	// new String[] { path + "%" }, null);
	// if (c.getCount() == 1) {
	// int s = mContentResolver.delete(uri,
	// MediaStore.Files.FileColumns.DATA + " LIKE ? ",
	// new String[] { path + "%" });
	// return s > 0 ? true : false;
	// } else {
	// return false;
	// }
	// } catch (Exception e) {
	// Log.e("Catch", "delete:" + e.getMessage());
	// Log.e("Catch", "path:" + path);
	// Log.e("Catch", "uri:" + (uri == null ? null : uri.toString()));
	// return false;
	// }
	// }
	//
	// public boolean deleteAll(int mediaType) {
	// Uri uri = getUri(mediaType);
	// try {
	// int s = mContentResolver.delete(uri, null, null);
	// return s > 0 ? true : false;
	// } catch (Exception e) {
	// Log.e("Catch", "delete:" + e.getMessage());
	// Log.e("Catch", "mediaType:" + mediaType);
	// Log.e("Catch", "uri:" + (uri == null ? null : uri.toString()));
	// return false;
	// }
	// }

	private boolean deleteFile(String path) {
		try {
			File file = new File(path);
			if (file.isFile())
				return file.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public int getPositionByPath(int type, String rootPath, String path) {
		switch (type) {
		case MyMedia.TYPE_VIDEO:
			return getVideoPositionByPath(rootPath, path);
		case MyMedia.TYPE_MUSIC:
			return getMusicPositionByPath(rootPath, path);
		case MyMedia.TYPE_GALLERY:
			return getGalleryPositionByPath(rootPath, path);
		}
		return -1;
	}

	public int getGalleryPositionByPath(String rootPath, String path) {
		Cursor cursor = null;
		try {
			Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
			String[] columns = new String[] { MediaStore.Images.Media._ID,
					MediaStore.Images.Media.DISPLAY_NAME,
					MediaStore.Images.Media.DATA,
					MediaStore.Images.Media.MIME_TYPE };
			// 要读的列名,这些常量可以查GOOGLE官方开发文档,TITLE是标题 DATA是路径//REGEXP
			cursor = mContentResolver.query(uri, columns,
					MediaStore.Images.Media.DATA + " LIKE ?",
					new String[] { rootPath + "%" }, null);
			while (cursor.moveToNext()) {
				if (path.equals(cursor.getString(2))) {
					return cursor.getPosition();
				}
			}
			return 0;
		} catch (SQLiteException e) {
			e.printStackTrace();
			return 0;
		} finally {
			if (cursor != null)
				cursor.close();
		}
	}

	public int getVideoPositionByPath(String rootPath, String path) {
		Cursor cursor = null;
		try {
			Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
			String[] columns = new String[] { MediaStore.Video.Media._ID,
					MediaStore.Video.Media.DISPLAY_NAME,
					MediaStore.Video.Media.DATA,
					MediaStore.Video.Media.MIME_TYPE };
			// 要读的列名,这些常量可以查GOOGLE官方开发文档,TITLE是标题 DATA是路径//REGEXP
			cursor = mContentResolver.query(uri, columns,
					MediaStore.Video.Media.DATA + " LIKE ?",
					new String[] { rootPath + "%" }, null);
			while (cursor.moveToNext()) {
				if (path.equals(cursor.getString(2))) {
					return cursor.getPosition();
				}
			}
			return 0;
		} catch (SQLiteException e) {
			e.printStackTrace();
			return 0;
		} finally {
			if (cursor != null)
				cursor.close();
		}
	}

	public int getMusicPositionByPath(String rootPath, String path) {
		Cursor cursor = null;
		try {
			Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
			String[] columns = new String[] { MediaStore.Video.Media._ID,
					MediaStore.Audio.Media.DISPLAY_NAME,
					MediaStore.Audio.Media.DATA,
					MediaStore.Audio.Media.MIME_TYPE };
			// 要读的列名,这些常量可以查GOOGLE官方开发文档,TITLE是标题 DATA是路径//REGEXP
			cursor = mContentResolver.query(uri, columns,
					MediaStore.Audio.Media.DATA + " LIKE ?",
					new String[] { rootPath + "%" }, null);
			while (cursor.moveToNext()) {
				if (path.equals(cursor.getString(2))) {
					return cursor.getPosition();
				}
			}
			return 0;
		} catch (SQLiteException e) {
			e.printStackTrace();
			return 0;
		} finally {
			if (cursor != null)
				cursor.close();
		}
	}

	/**
	 * mConnection = new MediaScannerConnection(mContext, this);
	 * mConnection.scanFile(mInfo.mFilename, mInfo.mMimetype);
	 * mConnection.disconnect();
	 */
}
