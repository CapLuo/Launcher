package com.android.custom.launcher.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapUtils {
	public static final String IMAGE_ID = "_id";
	public static final String IMAGE_NAME = "_name";
	public static final String IMAGE_PATH = "_path";
	public static final String IMAGE_SIZE = "_size";

	//scale image to display size
	public static Bitmap decodeBitmap(String path, int displayWidth, int displayHeight) {
		BitmapFactory.Options op = new BitmapFactory.Options();
		op.inJustDecodeBounds = true;
		Bitmap bmp = BitmapFactory.decodeFile(path, op);
		int width = (int)Math.ceil(op.outWidth / (float) displayWidth);
		int height = (int)Math.ceil(op.outHeight / (float) displayHeight);  
		if(width > 1 && height > 1) {
			if (width > height) {
				op.inSampleSize = width;
			} else {
				op.inSampleSize = height;
			}
			op.inJustDecodeBounds = false;
			bmp = BitmapFactory.decodeFile(path, op);
		}
		return Bitmap.createScaledBitmap(bmp, displayWidth, displayHeight, true);
	}

	public static Bitmap decodeBitmap(String path, int maxImageSize) {
		BitmapFactory.Options op = new BitmapFactory.Options();
		op.inJustDecodeBounds = true;
		Bitmap bmp = BitmapFactory.decodeFile(path, op);
		int scale = 1;
		if (op.outWidth > maxImageSize || op.outHeight > maxImageSize) {
			scale = (int) Math.pow(2, (int)Math.round(Math.log(maxImageSize / (double)Math.max(op.outWidth, op.outHeight)) / Math.log(0.5)));
		}
		op.inJustDecodeBounds = false;
		op.inSampleSize = scale;
		bmp = BitmapFactory.decodeFile(path, op);
		return bmp;
	}

	public static List<ImageHolder> getImages(Context context) {
		List<ImageHolder> images = new ArrayList<ImageHolder>();
		List<HashMap<String, String>> imagesInfo = FilesUtil.getDataImages(context);
		for (HashMap<String, String> info : imagesInfo) {
			images.add(new ImageHolder(info.get(IMAGE_ID),
					info.get(IMAGE_NAME), info.get(IMAGE_PATH), info.get(IMAGE_SIZE)));
		}
		return images;
	}

	public static class ImageHolder {
		private String mName;
		private String mPath;
		private String mSize;
		private String mId;

		public ImageHolder(String id, String name, String path, String size) {
			mId = id;
			mName = name;
			mPath = path;
			mSize = size;
		}

		public String getID() {
			return mId;
		}

		public String getName() {
			return mName;
		}

		public String getPath() {
			return mPath;
		}

		public String getSize() {
			return mSize;
		}
	}
}
