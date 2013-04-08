package com.ericcurtin.fallMonitor;

import org.holoeverywhere.app.Activity;

import android.os.Bundle;

/**
 * Abstract class that ensures the UI is processed before the rest of the
 * onCreate method which causes less issues with the HoloEverywhere library.
 * 
 * @author Eric Curtin
 */
public abstract class FallMonitorAbstActivity extends Activity {

	protected abstract void initializeUI();

	protected void onCreate(Bundle savedInstanceState) {
		initializeUI();
		super.onCreate(savedInstanceState);
	}

}