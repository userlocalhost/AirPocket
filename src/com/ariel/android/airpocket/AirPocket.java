package com.ariel.android.airpocket;

import android.app.Activity;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;

import android.widget.EditText;

import android.content.Intent;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.IOException;

public class AirPocket extends Activity
{
	private static final String TAG = "AirPocket";

	OnClickListener clickEvent = new View.OnClickListener() {
		public void onClick(View v) {

			Log.d(TAG, "[clickEvent] + " + getPackageName());

			/*
			File file = new File(AppConfig.APP_CONFIG_PATH);
			if(! file.exists()) {
				Log.d(TAG, "hogehoge.txt is NOT existed");
	
				file.getParentFile().mkdir();
	
				try {
					FileOutputStream fos = new FileOutputStream(file, true);
					OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
					BufferedWriter bw = new BufferedWriter(osw);

					String inputEmail = ((EditText) findViewById(R.id.editEmail)).getText().toString();
					String inputPasswd = ((EditText) findViewById(R.id.editPasswd)).getText().toString();

					Log.d(TAG, String.format("[onClick] email:%s, passwd:%s", inputEmail, inputPasswd));
				
					bw.write(String.format("email=%s\npasswd=%s", inputEmail, inputPasswd));
					bw.flush();
					bw.close();
				} catch(IOException e) {
					Log.e(TAG, e.getMessage());
				}
			}
			*/

			openEventIndex();
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_page);

		File file = new File(AppConfig.APP_CONFIG_PATH);
		if(file.exists()) {
			openEventIndex();
		} else {
			findViewById(R.id.loginButton).setOnClickListener(clickEvent);
		}

		ScheduleContent.resumeFromStorage();
	}

	private void openEventIndex() {
		Intent intent = new Intent(AirPocket.this, EventIndexMonth.class);

		AirPocket.this.finish();

		startActivity(intent);
	}
}
