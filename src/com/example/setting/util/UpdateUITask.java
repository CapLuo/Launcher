package com.example.setting.util;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

public class UpdateUITask extends AsyncTask<Void, Void, Boolean> {
	public interface TaskListener {
		public void updatePageData();

		public void updatePageUI();
	}
	
	public interface OnTaskCancelListener{
		public void onCancel();
	}
	
	private Dialog dialog;
	private TaskListener taskListener;
	private Context mContext;
	private OnTaskCancelListener onTaskCancelListener;
	private String tag;

	public UpdateUITask(Context context, TaskListener taskListener) {
		this.mContext = context;
		this.taskListener = taskListener;
	}
	
	public UpdateUITask(Context context, TaskListener taskListener, OnTaskCancelListener onTaskCancelListener) {
		this.mContext = context;
		this.taskListener = taskListener;
		this.onTaskCancelListener = onTaskCancelListener;
	}
	
	public void setTag(String tag){
		this.tag = tag;
	}

	@Override
	protected void onPreExecute() {
//		if(tag != null)
//			Log.i("Catch", "onPreExecute:"+tag);
		dialog = Util.progressDialog(mContext, null, null);
		dialog.show();
		dialog.setCancelable(true);
		dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
			
			public void onCancel(DialogInterface dialogInterface) {
				cancel(true);
				if(onTaskCancelListener != null){
					onTaskCancelListener.onCancel();
				}
			}
		});
		super.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
//		if(tag != null)
//			Log.i("Catch", "doInBackground:"+tag);
		try {
			taskListener.updatePageData();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
//		if(tag != null)
//			Log.i("Catch", "onPostExecute:"+tag+"  "+result);
		if (result) {
			try {
				taskListener.updatePageUI();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (dialog != null) {
			dialog.dismiss();
			dialog = null;
		}
		super.onPostExecute(result);
	}

}