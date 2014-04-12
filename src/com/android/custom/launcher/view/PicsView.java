package com.android.custom.launcher.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.custom.launcher.R;

public class PicsView extends LinearLayout{
	private ViewPager mPager;// 页卡内容
	private LayoutInflater lf;
	
	public PicsView(Context context) {
		super(context);
		lf = LayoutInflater.from(context);
        lf.inflate(R.layout.pics_view, this, true);
		initView();
	}
	public PicsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        lf = LayoutInflater.from(context);
        lf.inflate(R.layout.pics_view, this, true);
        initView();
    }
	private void initView(){
		mPager = (ViewPager) findViewById(R.id.vPager);
		mPager.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {

				}
				return false;
			}
		});
		
		View view1 = lf.inflate(R.layout.pic, null);
		View view2 = lf.inflate(R.layout.pic, null);
		View view3 = lf.inflate(R.layout.pic, null);

		List<View> viewList = new ArrayList<View>();// 将要分页显示的View装入数组中
		viewList.add(view1);
		viewList.add(view2);
		viewList.add(view3);
		
		List<Integer> bitmapList = new ArrayList<Integer>();
		bitmapList.add(R.drawable.home_background);
		bitmapList.add(R.drawable.ic_action_search);
		bitmapList.add(R.drawable.ic_launcher);

		mPager.setAdapter(new MyPagerAdapter(viewList,bitmapList));
		mPager.setCurrentItem(0);
	}
	private class MyPagerAdapter extends PagerAdapter {

		private List<View> viewList;
		private List<Integer> bitmapList;

		public MyPagerAdapter(List<View> viewList,List<Integer> bitmapList) {
			this.viewList = viewList;
			this.bitmapList = bitmapList;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {

			return arg0 == arg1;
		}

		@Override
		public int getCount() {

			return viewList.size();
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(viewList.get(position));

		}

		@Override
		public int getItemPosition(Object object) {

			return super.getItemPosition(object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View view = viewList.get(position);
			container.addView(view);
			view.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					// TODO Auto-generated method stub
//					changeActivity();
				}
			});
			ImageView imageView = (ImageView) view.findViewById(R.id.img);
			imageView.setBackgroundResource(bitmapList.get(position));
			return view;
		}

	};
	
//	private void changeActivity() {
//		Intent intent = new Intent(Intent.ACTION_MAIN);
//		intent.setFlags(1);
//		Log.e("@@@@##", "" + ((1 & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT));
//		intent.setClassName("com.android.launcher", "com.android.launcher2.Launcher");
//		this.startActivity(intent);
//
//	}
}
