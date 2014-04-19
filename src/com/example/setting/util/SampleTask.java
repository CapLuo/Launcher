package com.example.setting.util;

import android.content.Context;
import android.os.AsyncTask;

public class SampleTask extends AsyncTask<Void, Void, Boolean> {
	public interface TaskListener {
		public void updatePageData();

		public void updatePageUI();
	}
	
	private TaskListener taskListener;
	private Context mContext;

	public SampleTask(Context context, TaskListener taskListener) {
		this.mContext = context;
		this.taskListener = taskListener;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
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
		if (result) {
			try {
				taskListener.updatePageUI();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		super.onPostExecute(result);
	}

}