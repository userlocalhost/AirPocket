package com.ariel.android.airpocket;

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

public class AppConfig {
	private static final String TAG = "AppConfig";
	public static final String APP_CONFIG_PATH = "/data/data/com.ariel.android.airpocket/config";

	public static String getConfig(String key) {
		String ret = null;
		File file = new File(APP_CONFIG_PATH);

		if(file.exists()) {
			try {
				InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
				BufferedReader br = new BufferedReader(isr);

				String configLine;
				while((configLine = br.readLine()) != null) {
					Matcher m = Pattern.compile("^" + key + "=(.*)").matcher(configLine);

					if(m.find()) {
						ret = m.group(1);
						Log.d(TAG, "[Constructor] > " + configLine + ", ("+ ret +")");
					}
				}
				
				br.close();
			} catch(IOException e) {
				Log.e(TAG, e.getMessage());
			}
		}

		return ret;
	}

	public static void setConfig(String key, String value) {
		File file = new File(APP_CONFIG_PATH);
		String writeStr = new String();
		boolean isColumn = false;

		if(file.exists()) {
			try {
				InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
				BufferedReader br = new BufferedReader(isr);

				String configLine;
				while((configLine = br.readLine()) != null) {
					Matcher m = Pattern.compile("^" + key + "=(.*)").matcher(configLine);

					if(m.find()) {
						writeStr += String.format("%s=%s\n", key, value);
						isColumn = true;
					} else {
						writeStr += configLine + "\n";
					}
				}

				if(! isColumn) {
					writeStr += String.format("%s=%s\n", key, value);
				}
				
				br.close();
			} catch(IOException e) {
				Log.e(TAG, "[setConfig]" + e.getMessage());
			}
		} else {
			writeStr = String.format("%s=%s\n", key, value);
		}

		try {
			FileWriter writer = new FileWriter(file, false);

			writer.write(writeStr);
			writer.flush();
			writer.close();
		} catch(IOException e) {
			Log.e(TAG, "[setConfig]" + e.getMessage());
		}
	}

	public static void delConfig(String key) {
		setConfig(key, "");
	}
}
