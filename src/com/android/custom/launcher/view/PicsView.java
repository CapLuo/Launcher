package com.android.custom.launcher.view;

import java.io.File;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.custom.launcher.R;
import com.example.setting.ItemListActivity;
import com.example.setting.util.MediaHelper;
import com.example.setting.util.StoreUtil;
import com.example.setting.util.Util;

public class PicsView extends LinearLayout {
	private ViewPager mPager;// 页卡内容
	private LayoutInflater lf;
	private Context mContext;
	private ImageButton iBtnLeft;
	private ImageButton iBtnRight;
	private ImageButton iBtnMenu;
	private Cursor cursor;
	private View layout;
	private View layoutDefault;
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
		layoutDefault = findViewById(R.id.layout_pic_default);
		iBtnDefault = (ImageButton) findViewById(R.id.btn_pic_default);
		iBtnDefault.setOnClickListener(clickListener);
		mPager = (ViewPager) findViewById(R.id.vPager);
		rootPath = Util.getRootFilePath();
		cursor = loadAllGallery(mContext, rootPath);
		mPager.setAdapter(new MyPagerAdapter(cursor));
		// mPager.setOnKeyListener(new OnKeyListener() {
		//
		// public boolean onKey(View v, int keyCode, KeyEvent event) {
		// return true;
		// }
		// });

		mPager.setOnTouchListener(new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					openPic();
				}
				return false;
			}
		});

		iBtnLeft = (ImageButton) findViewById(R.id.btn_pic_left);
		iBtnLeft.setOnClickListener(clickListener);
		iBtnRight = (ImageButton) findViewById(R.id.btn_pic_right);
		iBtnRight.setOnClickListener(clickListener);
		iBtnMenu = (ImageButton) findViewById(R.id.btn_pic_menu);
		iBtnMenu.setOnClickListener(clickListener);
		// updatePath(true);
	}

	public void updatePath() {
		updatePath(false);
	}

	private void updatePath(boolean isFirst) {
		String path = StoreUtil.loadPicPath(mContext);
		if (path != null) {
			File file = new File(path);
			if (file.isFile()) {
//				Toast.makeText(mContext, "file.isFile()", 500).show();
				int position = getPositionByPath(mContext, rootPath, path);
				if (position >= 0) {
					layout.setVisibility(View.VISIBLE);
					layoutDefault.setVisibility(View.GONE);
					if (!isFirst) {
						cursor = loadAllGallery(mContext, rootPath);
						if(cursor != null){
							mPager.setAdapter(new MyPagerAdapter(cursor));
							mPager.setCurrentItem(position);
						}
					}
					return;
				}
			}
		}
//		Toast.makeText(mContext, "file.isFile() error ", 500).show();
		layout.setVisibility(View.GONE);
		layoutDefault.setVisibility(View.VISIBLE);

		setOnFocusChangeListener(new OnFocusChangeListener() {

			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					if (layoutDefault.getVisibility() == View.VISIBLE) {
						iBtnDefault.setFocusable(true);
						iBtnDefault.setFocusableInTouchMode(true);
						iBtnDefault.requestFocus();
					} else {
						iBtnLeft.setFocusable(true);
						iBtnLeft.setFocusableInTouchMode(true);
						iBtnLeft.requestFocus();
					}
				}

			}
		});
	}

	public void savePath() {
		try {
			if (layoutDefault.getVisibility() == View.GONE)
				StoreUtil.savePicPath(mContext, ((MyPagerAdapter) mPager
						.getAdapter()).getCurrPath(mPager.getCurrentItem()));
			if (cursor != null) {
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void goOnClickDefault() {
		Intent intent = new Intent();
		intent.setClass(mContext, ItemListActivity.class);
		intent.putExtra("position", 2);
		intent.putExtra("path", "All");
		mContext.startActivity(intent);
	}

	public void openAll() {
		Intent intent = new Intent();
		intent.setClass(mContext, ItemListActivity.class);
		intent.putExtra("position", 2);
		intent.putExtra("path", "All");
		mContext.startActivity(intent);
	}

	public void openAllInOne(String path) {
		Intent intent = new Intent();
		intent.setClass(mContext, ItemListActivity.class);
		intent.putExtra("position", 2);
		intent.putExtra("path", path);
		mContext.startActivity(intent);
	}

	public void openPic() {
		try {
			if (layoutDefault.getVisibility() == View.VISIBLE) {
				openAll();
			} else {
				MyPagerAdapter adapter = (MyPagerAdapter) mPager.getAdapter();
				Uri uri = Uri.parse("file://"
						+ adapter.getCurrPath(mPager.getCurrentItem()));
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(uri,
						adapter.getCurrMimeType(mPager.getCurrentItem()));
				intent.setComponent(new ComponentName("com.android.gallery",
						"com.android.camera.ViewImage"));
				mContext.startActivity(intent);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("Catch", "openPic is error");
		}
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
			} else if (v.getId() == R.id.btn_pic_menu) {
				openAllInOne(((MyPagerAdapter) mPager.getAdapter())
						.getCurrPath(mPager.getCurrentItem()));
			} else if (v.getId() == R.id.btn_pic_default) {
				openAllInOne("All");
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

		public String getCurrPath(int position) {
			if (cursor != null && cursor.moveToPosition(position)) {
				return cursor.getString(2);
			}
			return "";
		}

		public String getCurrMimeType(int position) {
			if (cursor != null && cursor.moveToPosition(position)) {
				return cursor.getString(3);
			}
			return "";
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

			if (cursor != null && cursor.moveToPosition(position)) {
				// TODO 490,300
				Bitmap bitmap = MediaHelper.getImageThumbnail(490, 300,
						cursor.getString(2));
				Bitmap bitmap2 = MediaHelper.getRoundedBitmap(bitmap, 12);
				if (bitmap2 == null)
					bitmap2 = bitmap;
				BitmapDrawable bd = new BitmapDrawable(bitmap2);
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
		} catch (SQLiteException e) {
			e.printStackTrace();
		}
		return cursor;
	}

	public int getPositionByPath(Context context, String rootPath, String path) {
		Cursor c = null;
		try {
			Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
			String[] columns = new String[] { MediaStore.Images.Media._ID,
					MediaStore.Images.Media.DISPLAY_NAME,
					MediaStore.Images.Media.DATA,
					MediaStore.Images.Media.MIME_TYPE };
			// 要读的列名,这些常量可以查GOOGLE官方开发文档,TITLE是标题 DATA是路径//REGEXP
			c = context.getContentResolver().query(uri, columns,
					MediaStore.Images.Media.DATA + " LIKE ?",
					new String[] { rootPath + "%" }, null);
			while (c.moveToNext()) {
				if (path.equals(c.getString(2))) {
					return c.getPosition();
				}
			}
			return -1;
		} catch (SQLiteException e) {
			e.printStackTrace();
			return -1;
		} finally {
			if (c != null)
				c.close();
		}
	}
}
