package com.android.custom.launcher.view;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.custom.launcher.R;
import com.example.setting.ItemListActivity;
import com.example.setting.adapter.MyMedia;
import com.example.setting.util.StoreUtil;

public class PicsView extends LinearLayout {
	private ViewPager mPager;// 页卡内容
	private LayoutInflater lf;
	private Context mContext;
	private ImageButton iBtnLeft;
	private ImageButton iBtnRight;
	private ImageButton iBtnMenu;
	private Cursor cursor;
	private View layout;
	private ImageButton iBtnDefault;
	private String rootPath;

	public PicsView(Context context) {
		super(context);
		mContext = context;
		lf = LayoutInflater.from(context);
		lf.inflate(R.layout.pics_view, this, true);
		initView();
	}

	public PicsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		lf = LayoutInflater.from(context);
		lf.inflate(R.layout.pics_view, this, true);
		initView();
	}

	private void initView() {
		layout = findViewById(R.id.layout_pic_main);
		iBtnDefault = (ImageButton) findViewById(R.id.btn_pic_default);
		iBtnDefault.setOnClickListener(clickListener);
		mPager = (ViewPager) findViewById(R.id.vPager);
		rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		cursor = loadAllGallery(mContext, rootPath);
		mPager.setAdapter(new MyPagerAdapter(cursor));
		mPager.setOnKeyListener(new OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				return true;
			}
		});

		iBtnLeft = (ImageButton) findViewById(R.id.btn_pic_left);
		iBtnLeft.setOnClickListener(clickListener);
		iBtnRight = (ImageButton) findViewById(R.id.btn_pic_right);
		iBtnRight.setOnClickListener(clickListener);
		iBtnMenu = (ImageButton) findViewById(R.id.btn_pic_menu);
		iBtnMenu.setOnClickListener(clickListener);

		setOnFocusChangeListener(new OnFocusChangeListener() {

			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					iBtnLeft.setFocusable(true);
					iBtnLeft.setFocusableInTouchMode(true);
					iBtnLeft.requestFocus();
				}

			}
		});
		updatePath(true);
	}

	public void updatePath() {
		updatePath(false);
	}

	private void updatePath(boolean isFirst) {
		String path = StoreUtil.loadPicPath(mContext);
		if (path != null) {
			int position = getPositionByPath(mContext, rootPath, path);
			layout.setVisibility(View.VISIBLE);
			iBtnDefault.setVisibility(View.GONE);
			if (!isFirst) {
				cursor = loadAllGallery(mContext, rootPath);
				mPager.setAdapter(new MyPagerAdapter(cursor));
				mPager.setCurrentItem(position);
			}
		} else {
			layout.setVisibility(View.GONE);
			iBtnDefault.setVisibility(View.VISIBLE);
		}
	}

	public void savePath() {
		if (iBtnDefault.getVisibility() == View.GONE)
			StoreUtil.savePicPath(mContext,
					((MyPagerAdapter) mPager.getAdapter()).getCurrPath());
		cursor.close();
	}

	private OnClickListener clickListener = new OnClickListener() {

		public void onClick(View v) {
			if (v.getId() == R.id.btn_pic_left) {
				int p = mPager.getCurrentItem() - 1;
				mPager.setCurrentItem(p >= 0 ? p : 0);
			} else if (v.getId() == R.id.btn_pic_right) {
				int p2 = mPager.getCurrentItem() + 1;
				mPager.setCurrentItem(p2 <= cursor.getCount() ? p2 : cursor
						.getCount());
			} else if (v.getId() == R.id.btn_pic_menu
					|| v.getId() == R.id.btn_pic_default) {
				Intent intent = new Intent();
				intent.setClass(mContext, ItemListActivity.class);
				intent.putExtra("position", 2);
				mContext.startActivity(intent);
			}

		}
	};

	private class MyPagerAdapter extends PagerAdapter {

		private Cursor cursor;

		public MyPagerAdapter(Cursor cursor) {
			this.cursor = cursor;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getCount() {
			if (cursor != null)
				return cursor.getCount();
			return 0;
		}

		public String getCurrPath() {
			return cursor.getString(2);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((ViewGroup) object);
		}

		@Override
		public int getItemPosition(Object object) {
			return super.getItemPosition(object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View view = lf.inflate(R.layout.pic, null);
			container.addView(view);
			view.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					// TODO Auto-generated method stub
					// changeActivity();
				}
			});
			ImageView imageView = (ImageView) view.findViewById(R.id.img);

			if (cursor.moveToPosition(position)) {
				// TODO 490,300
				Bitmap bitmap = getImageThumbnail(cursor.getString(2), 490, 300);
				BitmapDrawable bd = new BitmapDrawable(bitmap);
				imageView.setBackgroundDrawable(bd);
			}
			// imageView.setBackgroundResource(bitmapList.get(position));
			return view;
		}

	};

	public Cursor loadAllGallery(Context context, String rootPath) {
		Cursor cursor = null;
		try {
			Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
			String[] columns = new String[] { MediaStore.Images.Media._ID,
					MediaStore.Images.Media.DISPLAY_NAME,
					MediaStore.Images.Media.DATA,
					MediaStore.Images.Media.MIME_TYPE };
			// 要读的列名,这些常量可以查GOOGLE官方开发文档,TITLE是标题 DATA是路径//REGEXP
			cursor = context.getContentResolver().query(uri, columns,
					MediaStore.Images.Media.DATA + " LIKE ?",
					new String[] { rootPath + "%" }, null);

			// while (cursor.moveToNext()) {
			// // 循环读取第一列,即文件路径,0列是标题
			// String id = cursor.getString(0);
			// String name = cursor.getString(1);
			// String abPath = cursor.getString(2);
			// String mimeType = cursor.getString(3);
			// MyMedia myMedia = new MyMedia();
			// Bitmap bitmap = getImageThumbnail(abPath);
			// if (bitmap != null) {
			// BitmapDrawable bd = new BitmapDrawable(bitmap);
			// myMedia.setImage(bd);
			// myMedia.setMediaType(MyMedia.TYPE_GALLERY);
			// } else {
			// myMedia.setMediaType(MyMedia.TYPE_GALLERY_N);
			// myMedia.setImage(R.drawable.otherfiles_file);
			// }
			//
			// myMedia.setId(id);
			// myMedia.setName(name);
			// myMedia.setPath(abPath);
			// myMedia.setMimeType(mimeType);
			// listFile.add(myMedia);
			// }
		} catch (SQLiteException e) {
			e.printStackTrace();
		}
		return cursor;
	}

	public int getPositionByPath(Context context, String rootPath, String path) {
		Cursor cursor = null;
		try {
			Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
			String[] columns = new String[] { MediaStore.Images.Media._ID,
					MediaStore.Images.Media.DISPLAY_NAME,
					MediaStore.Images.Media.DATA,
					MediaStore.Images.Media.MIME_TYPE };
			// 要读的列名,这些常量可以查GOOGLE官方开发文档,TITLE是标题 DATA是路径//REGEXP
			cursor = context.getContentResolver().query(uri, columns,
					MediaStore.Images.Media.DATA + " LIKE ?",
					new String[] { rootPath + "%" }, null);
			while (cursor.moveToNext()) {
				if(path.equals(cursor.getString(2))){
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

	public Bitmap getImageThumbnail(String imagePath, int width, int height) {
		Bitmap bitmap = null;
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
		return bitmap;
	}

	// private void changeActivity() {
	// Intent intent = new Intent(Intent.ACTION_MAIN);
	// intent.setFlags(1);
	// Log.e("@@@@##", "" + ((1 & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) !=
	// Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT));
	// intent.setClassName("com.android.launcher",
	// "com.android.launcher2.Launcher");
	// this.startActivity(intent);
	//
	// }
}
