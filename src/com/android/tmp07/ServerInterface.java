package com.android.tmp07;

import android.util.Log;
import android.content.res.Resources.NotFoundException;
import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.lang.StringBuffer;
import java.lang.IndexOutOfBoundsException;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParserException;

/*
 * This class survie server infomation to local application.
 * */
public class ServerInterface {
	private static final String TAG = "ServerInterface";
	public static final String InfoFilepath = "/sdcard/ArielMultiScheduler/logininfo.txt";

	private static final String readURL = "http://192.168.0.3/documentSample.xml";
	private static final String postURL = "http://192.168.0.3/cgi-bin/tmp10.cgi";

	ServerInterface() {
		File file = new File(InfoFilepath);
		if(file.exists()) {
			try {
				InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
				BufferedReader br = new BufferedReader(isr);

				String configLine;
				while((configLine = br.readLine()) != null) {
					Log.d(TAG, "[Constructor] > "+configLine);
				}
				
				br.close();
			} catch(IOException e) {
				Log.e(TAG, e.getMessage());
			}
		}
	}

	public boolean doSync(Context index) {
		boolean ret = true;
		String xmlContext = getDocuments(index);

		ret = parseFromXML(xmlContext);

		return ret;
	}

	private String getDocuments(Context index) {
		StringBuffer payload = new StringBuffer();

		try {
			URL url = new URL(index.getResources().getString(R.string.readDocumentURL));
	
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setReadTimeout(100000);
			con.setRequestMethod("GET");
			con.addRequestProperty("Accept-Charset", "UTF-8");
			con.setDoInput(true);
			con.connect();
	
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));

			String line;
			while((line = reader.readLine()) != null) {
				payload.append(line);
			}
			
			reader.close();
			con.disconnect();

		} catch (IOException e) {
			Log.e(TAG, "IOException", e);
		}

		return payload.toString();
	}

	private boolean parseFromXML(String xmlContent) {
		boolean ret = true;

		try {
			XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();

			try {
				parser.setInput(new StringReader(xmlContent));
			} catch (NotFoundException e) {
				Log.e(TAG, "[parseFromXML] XML_ERROR "+e.getMessage());
			} catch (XmlPullParserException e) {
				Log.e(TAG, "[parseFromXML] XML_ERROR "+e.getMessage());
			}
			
			
			for(int e = parser.getEventType(); e != XmlPullParser.END_DOCUMENT; e = parser.next()) {
				if(e == XmlPullParser.START_TAG) {
					String elemName = parser.getName();

					if(elemName.equals("entry")) {
						ScheduleContent doc = new ScheduleContent();

						/* parse inputXML and initialize ScheduleContent object */
						ret = parseEntry(parser, doc);
						if(! ret) {
							break;
						}

						doc.regist();
					}
				}
			}

		} catch (NotFoundException e) {
			ret = false;

			Log.e(TAG, "[parseEntry] XML " + e.getClass().getName() + ": " + e.getMessage());
		} catch ( XmlPullParserException e) {
			ret = false;

			Log.d(TAG, "[parseEntry] XML " + e.getClass().getName() + ": " + e.getMessage());
			for ( StackTraceElement s : e.getStackTrace()) {
				Log.d(TAG, "[parseEntry] TRACE " + s.toString());
			}
		} catch ( IOException e) {
			ret = false;

			Log.d(TAG, "[parseEntry] XML " + e.getClass().getName() + ": " + e.getMessage());
		}

		return ret;
	}

	private static boolean parseEntry(XmlPullParser parser, ScheduleContent doc) {
		boolean ret = true;

		try{
			int depth = 1;
	
			for(int e = parser.next(); (e != XmlPullParser.END_DOCUMENT) && (depth > 0); e = parser.next()) {
				if( e == XmlPullParser.TEXT ) {
					depth++;
				} else if(e == XmlPullParser.START_TAG) {
					String elemName = parser.getName();
	
					e = parser.next();
	
					if(e == XmlPullParser.TEXT) {
						if(elemName.equals("title")) {
							doc.setSubject(parser.getText());
						} else if(elemName.equals("id")) {
							doc.setId(parser.getText());
						} else if(elemName.equals("starttime")) {
							doc.setStartTime(convStr2Date(parser.getText()));
						} else if(elemName.equals("endtime")) {
							doc.setEndTime(convStr2Date(parser.getText()));
						} else if(elemName.equals("wholeday") && parser.getText().equals("1")) {
							doc.setStatus(ScheduleContent.Allday);
						}

						Log.d(TAG, "[parseEntry] "+parser.getText());
					}
				} else if(e == XmlPullParser.END_TAG) {
					depth--;
				}
			}

		} catch (NotFoundException e) {
			ret = false;

			Log.e(TAG, "[parseEntry] XML " + e.getClass().getName() + ": " + e.getMessage());
		} catch ( XmlPullParserException e) {
			ret = false;

			Log.d(TAG, "[parseEntry] XML " + e.getClass().getName() + ": " + e.getMessage());
			for ( StackTraceElement s : e.getStackTrace()) {
				Log.d(TAG, "[parseEntry] TRACE " + s.toString());
			}
		} catch(ParseException e) {
			Log.d(TAG, "[parseEntry] " + e.getMessage());
		} catch ( IOException e) {
			ret = false;

			Log.d(TAG, "[parseEntry] XML " + e.getClass().getName() + ": " + e.getMessage());
		}

		return ret;
	}

	/* 
	 * This routine convert Date-string which is implemented in relation to 
	 *  RFC3339 string to java.util.Date object.
	 * */
	private static Date convStr2Date(String datestring) throws ParseException, IndexOutOfBoundsException {
		Date d = new Date();

		//if there is no time zone, we don't need to do any special parsing.
		if(datestring.endsWith("Z")){
			try{
				//spec for RFC3339
				SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
				d = s.parse(datestring);
			} catch(ParseException pe) {
				//spec for RFC3339 (with fractional seconds)
				SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
				s.setLenient(true);
				d = s.parse(datestring);
			}
			return d;
		}

		//step one, split off the timezone.
		String firstpart = datestring.substring(0,datestring.lastIndexOf('-'));
		String secondpart = datestring.substring(datestring.lastIndexOf('-'));

		//step two, remove the colon from the timezone offset
		secondpart = secondpart.substring(0,secondpart.indexOf(':')) + secondpart.substring(secondpart.indexOf(':')+1);
		datestring	= firstpart + secondpart;

		//spec for RFC3339
		SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		try{
			d = s.parse(datestring);
		} catch(ParseException pe) {
			//spec for RFC3339 (with fractional seconds)
			s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ");
			s.setLenient(true);
			d = s.parse(datestring);
		}

		return d;
	}
}
