package com.android.custom.launcher;

import com.android.custom.launcher.services.LauncherService;

import android.app.Application;
import android.content.Intent;

public class MainApplication extends Application {

	@Override
	public void onCreate() {
		Intent intent = new Intent(this, LauncherService.class);
		startService(intent);
		super.onCreate();
	}

}
