package com.android.custom.launcher.adapter;

import java.util.ArrayList;

import com.android.custom.launcher.R;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class ApplicationsAdapter extends BaseAdapter
{
    public static enum WorkState {
        UNINSTALL, NORMAL;
    }

    public static class AppItem {
        private PackageManager mPackageManager = null;
        private ResolveInfo mResolveInfo = null;
        private String mTitle = null;
        private Drawable mIcon = null;

        public AppItem(PackageManager packageManager, ResolveInfo info) {
            this.mPackageManager = packageManager;
            this.mResolveInfo = info;
            mTitle = info.activityInfo.applicationInfo.loadLabel(packageManager).toString();
        }

        public ResolveInfo getResolveInfo() {
            return mResolveInfo;
        }

        public PackageManager getPackageManager() {
            return mPackageManager;
        }

        public String getTitle()
        {
            return mTitle;
        }

        public Drawable getIcon()
        {
            if (mIcon == null) {
                mIcon = mResolveInfo.loadIcon(mPackageManager);
            }

            return mIcon;
        }

        public Intent getIntent()
        {
            ComponentName component_name = new ComponentName(mResolveInfo.activityInfo.applicationInfo.packageName,
                    mResolveInfo.activityInfo.name);
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setComponent(component_name);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

            return intent;
        }

        public Intent unInstall() {
            Uri uri = Uri.parse("package:" + mResolveInfo.activityInfo.applicationInfo.packageName);
            Intent intent = new Intent(Intent.ACTION_DELETE, uri);
            return intent;
        }

    }

    private LayoutInflater mInflater = null;
    private ArrayList<AppItem> mApps = new ArrayList<AppItem>();
    private WorkState mWorkState = WorkState.NORMAL;

    public ApplicationsAdapter(Context context, GridView gridView)
    {
        gridView.setAdapter(this);
        mInflater = LayoutInflater.from(context);
    }

    public void setAppItems(ArrayList<AppItem> apps)
    {
        mApps = apps;
    }

    public void addAppItems(AppItem item) {
        mApps.add(item);
        this.notifyDataSetChanged();
    }

    public void deleteItems(String packageName) {
        for (AppItem item : mApps) {
            if (item.getResolveInfo().activityInfo.packageName.equals(packageName)) {
                mApps.remove(item);
                this.notifyDataSetChanged();
                break;
            }
        }
    }

    public Object getItem(int position)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public long getItemId(int position)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        
        AppItem app = mApps.get(position);
        
        View ret_view = convertView;
        if (ret_view == null) {
            ret_view = mInflater.inflate(R.layout.apps_item, null);
        } 
        TextView textview = (TextView)ret_view.findViewById(R.id.app_name);
        ImageView imageview = (ImageView)ret_view.findViewById(R.id.app_icon);
        ImageView unload = (ImageView)ret_view.findViewById(R.id.app_unload);
        if (mWorkState == WorkState.NORMAL) {
            unload.setVisibility(View.GONE);
        } else {
            unload.setVisibility(View.VISIBLE);
        }

        textview.setText(app.getTitle());
        imageview.setImageDrawable(app.getIcon());

        ret_view.setTag(app);
        return ret_view;
    }

    public int getCount() {
        return mApps.size();
    }

    public void changeWorkState(WorkState state) {
        mWorkState = state;
    }

    public boolean getWorkState() {
        return mWorkState == WorkState.NORMAL;
    }

}
