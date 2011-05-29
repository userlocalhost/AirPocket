package com.android.tmp07;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.LinearLayout;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.FileWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.lang.StringBuffer;
import java.lang.IndexOutOfBoundsException;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShowConfig extends Activity
{
	private static final String TAG = "ShowConfig";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_config);
	}

	@Override
	public void onStart() {
		super.onStart();

		File file = new File(AppConfig.APP_CONFIG_PATH);
		LinearLayout display = (LinearLayout) findViewById(R.id.display);

		if(file.exists()) {
			try {
				InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
				BufferedReader br = new BufferedReader(isr);

				String configLine;
				while((configLine = br.readLine()) != null) {
					TextView textView = new TextView(this);

					textView.setText(configLine);
					display.addView(textView);
				}
				
				br.close();
			} catch(IOException e) {
				Log.e(TAG, e.getMessage());
			}
		}
	}
}
