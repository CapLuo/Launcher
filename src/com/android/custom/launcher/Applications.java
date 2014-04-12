package com.android.custom.launcher;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;

import com.android.custom.launcher.adapter.ApplicationsAdapter;
import com.android.custom.launcher.adapter.ApplicationsAdapter.AppItem;
import com.android.custom.launcher.adapter.ApplicationsAdapter.WorkState;

public class Applications extends BaseActivity implements OnItemClickListener, OnItemLongClickListener {

    private GridView mGridView;
    private ApplicationsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.applications);

        mGridView = (GridView) findViewById(R.id.myApps);
        mGridView.setOnItemClickListener(this);
        mGridView.setOnItemLongClickListener(this);
        mAdapter = new ApplicationsAdapter(this, mGridView);
        setAppItems();
    }

    private void setAppItems() {
        PackageManager mPackageManager = getPackageManager();
        List<PackageInfo> package_Infos = mPackageManager.getInstalledPackages(0);

        final Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolve_Infos = mPackageManager.queryIntentActivities(intent, 0);

        ArrayList<AppItem> apps = new ArrayList<AppItem> ();
        for (int i = 0; i < package_Infos.size(); i++) {
            for (int j = 0; j < resolve_Infos.size(); j++) {
                if (package_Infos.get(i).packageName.equals(resolve_Infos.get(j).activityInfo.packageName)) {
                    apps.add(new AppItem(mPackageManager, resolve_Infos.get(j)));
                }
            }
        }
        mAdapter.setAppItems(apps);
        mAdapter.notifyDataSetChanged();
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
        mAdapter.changeWorkState(WorkState.NORMAL);
        mAdapter.notifyDataSetChanged();
        super.onBackPressed();
    }

}
