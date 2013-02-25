package com.ericcurtin.fallMonitor;

import org.holoeverywhere.app.Activity;

import android.os.Bundle;

public abstract class FallMonitorAbstActivity extends Activity {

	protected abstract void initializeUI();
	
	protected void onCreate(Bundle savedInstanceState) {
		initializeUI();
		super.onCreate(savedInstanceState);
	}

}