package com.ariel.android.airpocket;

import android.app.Activity;
import android.os.Bundle;

import android.widget.TextView;
import android.content.Intent;
import android.content.Context;
import android.util.Log;
import android.os.Handler;
import android.os.Message;

import java.lang.*;
import java.lang.reflect.*;
import java.lang.Thread;
import java.util.Date;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Calendar;
import java.io.Serializable;

import java.lang.Exception;

public class ProgressiveProcessing extends Activity
{
	private static final String TAG = "ProgressiveProcessing";

	public static final String KEY_COMMENT = "com.ariel.android.airpocket.progressive_processing.comment";
	public static final String KEY_STATUS = "com.ariel.android.airpocket.progressive_processing.status";
	//public static final String KEY_METHOD = "com.ariel.android.airpocket.progressive_processing.method";
	//public static final String KEY_CLASS = "com.ariel.android.airpocket.progressive_processing.class";

	public static final int THREAD_DONE = (1 << 0);
	
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			Log.d(TAG, "[handleMessage]");

			switch(msg.what) {
			case THREAD_DONE:
				Intent i = new Intent();
				String ret = (String) msg.obj;

				i.putExtra(KEY_STATUS, ret);
				setResult(Activity.RESULT_OK, i);

				finish();
				break;
			}
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.progressive_processing);

		BackProc thread = new BackProc(this);
		thread.start();

		/* set comment string */
		((TextView) findViewById(R.id.comment)).setText(getIntent().getStringExtra(KEY_COMMENT));
	}

	class BackProc extends Thread {
		private static final String TAG = "BackProc";
		private Context context;

		public BackProc(Context context) {
			this.context = context;
		}

		@Override
		public void run() {
			String sendResult = "false";

			ServerInterface serverInterface = new ServerInterface();
			boolean ret = serverInterface.doSyncGoogleCalendar();
			if(ret) {
				sendResult = "true";
			}

			mHandler.obtainMessage(ProgressiveProcessing.THREAD_DONE, sendResult).sendToTarget();
		}
	}
}
