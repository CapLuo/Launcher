package com.android.custom.launcher;

import android.app.Application;
import android.content.Intent;

import com.android.custom.launcher.services.LauncherService;

public class MainApplication extends Application {

	@Override
	public void onCreate() {
		Intent intent = new Intent(this, LauncherService.class);
		startService(intent);
		super.onCreate();
	}

}
