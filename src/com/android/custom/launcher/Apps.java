package com.android.custom.launcher;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.PopupWindow;

import com.android.custom.launcher.adapter.ApplicationsAdapter;
import com.android.custom.launcher.adapter.ApplicationsAdapter.AppItem;
import com.android.custom.launcher.adapter.ApplicationsAdapter.WorkState;

public class Apps extends BaseActivity implements OnItemClickListener, OnItemLongClickListener {

    private GridView mGridView;
    private ApplicationsAdapter mAdapter;
    private ArrayList<AppItem> mApps;
    private PopupWindow mPopupWindow;
    private View delete;

    private BroadcastReceiver mAppControll = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED) ||
                    intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {
                setAppItems();
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.applications);
        initPopuWindow();

        mGridView = (GridView) findViewById(R.id.myApps);
        mGridView.setOnItemClickListener(this);
        mGridView.setOnItemLongClickListener(this);
        mAdapter = new ApplicationsAdapter(this, mGridView);

        IntentFilter appFilter = new IntentFilter();
        appFilter.addDataScheme("package");
        appFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        appFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        registerReceiver(mAppControll, appFilter);
    }

    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add("menu");
		return super.onCreateOptionsMenu(menu);
	}

    public boolean onMenuOpened(int featureId, Menu menu) {  
        return false;
    } 

	@Override
    protected void onResume() {
        super.onResume();
        new LoadAppTask().execute();
    }

	@Override
    protected void onDestroy() {
        unregisterReceiver(mAppControll);
        super.onDestroy();
    }

	private void setAppItems() {
        PackageManager mPackageManager = getPackageManager();
        List<PackageInfo> package_Infos = mPackageManager.getInstalledPackages(0);

        final Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolve_Infos = mPackageManager.queryIntentActivities(intent, 0);

        mApps = new ArrayList<AppItem> ();
        for (int i = 0; i < package_Infos.size(); i++) {
            for (int j = 0; j < resolve_Infos.size(); j++) {
                if ((package_Infos.get(i).applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
                    if (package_Infos.get(i).packageName.equals(resolve_Infos.get(j).activityInfo.packageName)) {
                        if (!resolve_Infos.get(j).activityInfo.packageName.equals(this.getApplication().getPackageName())) {
                            mApps.add(new AppItem(mPackageManager, resolve_Infos.get(j)));
                        }
                    }
                }
            }
        }

    }

    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2,
            long arg3) {
        if (mAdapter.getWorkState()) {
            mAdapter.changeWorkState(WorkState.UNINSTALL);
            mAdapter.notifyDataSetChanged();
        }
        return true;
    }

    public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
        Object tag = view.getTag();
        if (tag instanceof AppItem) {
            AppItem item = (AppItem) tag;
            if (mAdapter.getWorkState()) {
                Intent intent = item.getIntent();
                this.startActivity(intent);
            } else {
                Intent intent = item.unInstall();
                this.startActivity(intent);
            }
        }
    }

    @Override
    public void onBackPressed() {
    	if (mPopupWindow.isShowing()) {
    		mPopupWindow.dismiss();
    		return;
    	}
        if (mAdapter.getWorkState()) {
            super.onBackPressed();
            return;
        }
        mAdapter.changeWorkState(WorkState.NORMAL);
        mAdapter.notifyDataSetChanged();
    }

    private class LoadAppTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			setAppItems();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
	        mAdapter.setAppItems(mApps);
	        mAdapter.notifyDataSetChanged();
			super.onPostExecute(result);
		}

    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			View v = mGridView.getFocusedChild();
			if (v == null) {
				return false;
			} else {
				Object object = v.getTag();
				if (object instanceof AppItem) {
					if (mPopupWindow.isShowing()) {
						mPopupWindow.dismiss();
					} else {
						mPopupWindow.showAtLocation(findViewById(R.layout.applications), Gravity.BOTTOM, 0, 0);
					}
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private void initPopuWindow() {
		LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View sub_view = mLayoutInflater.inflate(R.layout.app_menu, null);
		mPopupWindow = new PopupWindow(sub_view, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
		mPopupWindow.setFocusable(true);
		delete = sub_view.findViewById(R.id.delete);
		delete.setFocusable(true);
		delete.setFocusableInTouchMode(true);
		delete.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				View view = mGridView.getFocusedChild();
				if (view != null) {
					Object object = view.getTag();
					if (object instanceof AppItem) {
						AppItem item = (AppItem) object;
						Apps.this.startActivity(item.getIntent());
					}
				}
			}
		});
	}
}
