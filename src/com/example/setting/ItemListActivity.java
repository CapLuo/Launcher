package com.example.setting;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import cn.ireliance.android.common.ui.SwitchCallbackFragmentActivity;

import com.android.custom.launcher.R;
import com.example.fragment.GridItemCallBack;
import com.example.fragment.MyGridFragment;
import com.example.setting.adapter.LeftMainMenuAdapter;
import com.example.setting.adapter.MyMedia;
import com.example.setting.listener.KeyBackListener;
import com.example.setting.view.UsbTitleView;

/**
 * An activity representing a list of Items. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link ItemDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ItemListFragment} and the item details (if present) is a
 * {@link ItemDetailFragment}.
 * <p>
 * This activity also implements the required {@link ItemListFragment.Callbacks}
 * interface to listen for item selections.
 */
public class ItemListActivity extends SwitchCallbackFragmentActivity implements
		ItemListFragment.Callbacks {
	private MyGridFragment currFragment;
	private UsbTitleView usbTitleView;

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_item_twopane);
		usbTitleView = (UsbTitleView)findViewById(R.id.usb_title);
		ItemListFragment itemListFragment = (ItemListFragment) getSupportFragmentManager().findFragmentById(
				R.id.item_list);
		itemListFragment.setActivateOnItemClick(true);
		// TODO: If exposing deep links into your app, handle intents here.
		Intent intent = getIntent();
		LeftMainMenuAdapter adapter = (LeftMainMenuAdapter)itemListFragment.getListView().getAdapter();
		if(intent != null){
			int position = intent.getIntExtra("position", 0);
			adapter.setSelector(position);
			adapter.notifyDataSetChanged();
			String filePath = intent.getStringExtra("path");
			switch (position) {
			case 0:
				currFragment = selectFragment(MyMedia.TYPE_VIDEO,filePath);
				break;
			case 1:
				currFragment = selectFragment(MyMedia.TYPE_MUSIC,filePath);
				break;
			case 2:
				currFragment = selectFragment(MyMedia.TYPE_GALLERY,filePath);
				break;
			case 3:
				currFragment = selectFragment(MyMedia.TYPE_OTHER,filePath);
				break;
			}
			itemListFragment.getListView().setItemChecked(position, true);
		} else {
			adapter.setSelector(0);
			adapter.notifyDataSetChanged();
			onItemSelected(null, 0);
			itemListFragment.getListView().setItemChecked(0, true);
		}
	}

	/**
	 * Callback method from {@link ItemListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	public void onItemSelected(View v, int position) {
		
		switch (position) {
		case 0:
			currFragment = selectFragment(MyMedia.TYPE_VIDEO,null);
			break;
		case 1:
			currFragment = selectFragment(MyMedia.TYPE_MUSIC,null);
			break;
		case 2:
			currFragment = selectFragment(MyMedia.TYPE_GALLERY,null);
			break;
		case 3:
			currFragment = selectFragment(MyMedia.TYPE_OTHER,null);
			break;
		}
		
	}

	private MyGridFragment selectFragment(int type, String filePath) {
		FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();
		Bundle arguments = new Bundle();
		arguments.putInt("mediaType", type);
		if(filePath != null){
			arguments.putString("path", filePath);
		}
		MyGridFragment fragment = new MyGridFragment(usbTitleView);
		fragment.setArguments(arguments);
		transaction.replace(R.id.item_detail_container, fragment);
		transaction.commit();
		GridItemCallBack gridItemCallBack = new GridItemCallBack(this, fragment);
		putItemSelectedCallback(MyGridFragment.class, gridItemCallBack);
		return fragment;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if (currFragment instanceof KeyBackListener) {
				if (((KeyBackListener) currFragment).onKeyBackDown())
					return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

}
