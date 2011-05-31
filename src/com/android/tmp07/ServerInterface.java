package com.android.tmp07;

import android.util.Log;
import android.content.res.Resources.NotFoundException;
import android.content.Context;

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
import java.io.FileNotFoundException;
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
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.Iterator;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParserException;

/*
 * This class survie server infomation to local application.
 * */
public class ServerInterface {
	private static final String TAG = "ServerInterface";
	private static final String CALENDAR_SOURCE = "ArielNetworks-AirPocket-v0.1";
	private static final String ClientLoginURL = "https://www.google.com/accounts/ClientLogin";
	private static final String UserCalendarURL = "https://www.google.com/calendar/feeds/default/private/full";
	//private static final String UserCalendarURL = "http://www.google.com/calendar/feeds/smoke.airone%40gmail.com/private/full";
	//private static final String UserCalendarURL = "https://www.google.com/calendar/feeds/smoke.airone%40gmail.com/private/full";

	/* These URL may describe am or bm */
	private static final String ReadDocumentURL = "";
	private static final String PostDocumentURL = "";

	public static final int GOOGLE_CLIENT = (1 << 0);

	/* Followings are private members */
	private static String accessToken;

	/* followings are public processing */
	public static boolean doSyncAirone() {
		boolean ret = true;
		String xmlContext = getDocuments();

		ret = parseFromXML(xmlContext);

		return ret;
	}

	public static boolean doSyncGoogleCalendar() {
		boolean ret = true;
		String loginId = AppConfig.getConfig("googleLoginId");
		String loginPasswd = AppConfig.getConfig("googleLoginPasswd");

		if(loginId == null || loginPasswd == null) {
			return false;
		}

		if(accessToken == null) {
			accessToken = gcalClientLogin(loginId, loginPasswd);
		}
		
		Log.d(TAG, "[doSyncGoogleCalendar] accessToken : " + accessToken);

		if(accessToken != null) {
			parseFromXML(gcalGetRequest(UserCalendarURL));
		} else {
			ret = false;
		}

		return ret;
	}

	public static boolean isLogined() {
		boolean ret = false;

		if(accessToken != null) {
			ret = true;
		}

		return ret;
	}

	public static boolean putDocument(ScheduleContent doc, Context index) {
		boolean ret = true;
		String loginId = AppConfig.getConfig("googleLoginId");
		String loginPasswd = AppConfig.getConfig("googleLoginPasswd");
		String sendData = makeSendDocTemplate(doc, index);

		if(loginId == null || loginPasswd == null) {
			return false;
		}

		if(accessToken == null) {
			accessToken = gcalClientLogin(loginId, loginPasswd);
		}
		
		Log.d(TAG, "[doSyncGoogleCalendar] accessToken : " + accessToken);

		if(accessToken != null && sendData != null) {
			String requestURL = doc.getEditURL();
			String protocol = "PUT";

			if(requestURL == null) {
				requestURL = UserCalendarURL;
				protocol = "POST";
			}

			Log.d(TAG, "[putDocument] requestURL : " + requestURL);

			String responseXML = gcalPostRequest(requestURL, sendData, protocol);

			if(responseXML != null) {
				updateDocumentStatus(doc, responseXML);
			}

			ret = true;
		} else {
			ret = false;
		}

		return ret;
	}

	public static boolean delDocument(ScheduleContent doc) {
		boolean ret = false;
		String loginId = AppConfig.getConfig("googleLoginId");
		String loginPasswd = AppConfig.getConfig("googleLoginPasswd");
		String requestURL = doc.getEditURL();

		if(loginId == null || loginPasswd == null) {
			return false;
		}

		if(accessToken == null) {
			accessToken = gcalClientLogin(loginId, loginPasswd);
		}

		if(accessToken != null && requestURL != null) {
			int responseCode = gcalRequest(requestURL, "DELETE");

			Log.d(TAG, "[delDocument] responseCode : " + responseCode);
			ret = true;
		}

		return ret;
	}

	/* followings are private processing */

	private static String getDocuments() {
		StringBuffer payload = new StringBuffer();

		try {
			URL url = new URL(ReadDocumentURL);
	
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

	private static boolean parseFromXML(String xmlContent) {
		boolean ret = true;

		Log.d(TAG, String.format("[parseFromXML] xmlContent (%d) : %s", xmlContent.length(), xmlContent));

		try {
			XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();

			try {
				parser.setInput(new StringReader(xmlContent));
			} catch (NotFoundException e) {
				Log.e(TAG, "[parseFromXML] XML_ERROR "+e.getMessage());
			} catch (XmlPullParserException e) {
				Log.e(TAG, "[parseFromXML] XML_ERROR "+e.getMessage());
			}
			
			
			for(int e = parser.getEventType(); e != XmlPullParser.END_DOCUMENT;) {
				boolean rollFlag = true;

				if(e == XmlPullParser.START_TAG) {
					String elemName = parser.getName();
					
					Log.d(TAG, String.format("[parseFromXML] tag : %s", elemName));

					if(elemName.equals("entry")) {
						ScheduleContent doc = new ScheduleContent();
							
						Log.d(TAG, String.format("[parseFromXML] call parseEntry()"));
						
						/* parse inputXML and initialize ScheduleContent object */
						ret = parseEntry(parser, doc);
						if(ret) {
							Log.d(TAG, String.format("[parseFromXML] regist document"));
							doc.regist();
						}

						rollFlag = false;
					}
				} else if(e == XmlPullParser.TEXT) {
					Log.d(TAG, String.format("[parseFromXML] text : %s", parser.getText()));
				}

				if(rollFlag) {
					e = parser.next();
				}
			}

		} catch (NotFoundException e) {
			ret = false;

			Log.e(TAG, "[parseFromXML] XML " + e.getClass().getName() + ": " + e.getMessage());
		} catch ( XmlPullParserException e) {
			ret = false;

			Log.e(TAG, "[parseFromXML] XML " + e.getClass().getName() + ": " + e.getMessage());
			for ( StackTraceElement s : e.getStackTrace()) {
				Log.e(TAG, "[parseFromXML] TRACE " + s.toString());
			}
		} catch ( IOException e) {
			ret = false;

			Log.e(TAG, "[parseFromXML] XML " + e.getClass().getName() + ": " + e.getMessage());
		}

		return ret;
	}

	private static boolean parseEntry(XmlPullParser parser, ScheduleContent doc) {
		boolean ret = true;

		try{
			int depth = 1;
	
			for(int e = parser.next(); (e != XmlPullParser.END_DOCUMENT) && (depth > 0);) {
				if(e == XmlPullParser.START_TAG) {
					depth++;
					String elemName = parser.getName();

					Log.d(TAG, String.format("[parseEntry](%d) %s", depth, elemName));
					
					if(elemName.equals("gd:when")) {
						String strStartTime = parser.getAttributeValue(null, "startTime");
						String strEndTime = parser.getAttributeValue(null, "endTime");

						Date startTime = convStr2Date(strStartTime);
						Date endTime = convStr2Date(strEndTime);
							
						Log.d(TAG, String.format("[parseEntry](%d) startTime:%s", depth, startTime));
						Log.d(TAG, String.format("[parseEntry](%d) endTime:%s", depth, endTime));
						Log.d(TAG, String.format("[parseEntry](%d) strStartTime:%s", depth, strStartTime));
						Log.d(TAG, String.format("[parseEntry](%d) strEndTime:%s", depth, strEndTime));

						if(Pattern.compile("^([0-9]+)-([0-9]+)-([0-9]+)$").matcher(strStartTime).find()) {
							Log.d(TAG, String.format("[parseEntry](%d) set wholeday", depth));
							doc.setStatus(ScheduleContent.Allday);
							endTime.setDate(endTime.getDate() - 1);
						}

						if(startTime != null && endTime != null) {
							doc.setStartTime(startTime);
							doc.setEndTime(endTime);
						}
					} else if(elemName.equals("link")) {
						String relStr = parser.getAttributeValue(null, "rel");

						if(relStr != null && relStr.equals("edit")) {
							String hrefStr = parser.getAttributeValue(null, "href");
	
							doc.setEditURL(hrefStr);
						}
					}
	
					e = parser.next();
	
					if(e == XmlPullParser.TEXT) {
						if(elemName.equals("title")) {
							Log.d(TAG, String.format("[parseEntry](%d) title:%s", depth, parser.getText()));
							doc.setSubject(parser.getText());
						} else if(elemName.equals("content")) {
							Log.d(TAG, String.format("[parseEntry](%d) content:%s", depth, parser.getText()));
							doc.setContext(parser.getText());
						} else if(elemName.equals("id")) {
							Log.d(TAG, String.format("[parseEntry](%d) id:%s", depth, parser.getText()));
							doc.setId(parser.getText());
						}

						e = parser.next();
					}
				} else if(e == XmlPullParser.END_TAG) {
					depth--;

					e = parser.next();
				}
			}

		} catch (NotFoundException e) {
			ret = false;

			Log.e(TAG, "[parseEntry] XML " + e.getClass().getName() + ": " + e.getMessage());
		} catch ( XmlPullParserException e) {
			ret = false;

			Log.e(TAG, "[parseEntry] XML " + e.getClass().getName() + ": " + e.getMessage());
			for ( StackTraceElement s : e.getStackTrace()) {
				Log.e(TAG, "[parseEntry] TRACE " + s.toString());
			}
		} catch(ParseException e) {
			Log.e(TAG, "[parseEntry] " + e.getMessage());
		} catch ( IOException e) {
			ret = false;

			Log.e(TAG, "[parseEntry] XML " + e.getClass().getName() + ": " + e.getMessage());
		}

		return ret;
	}

	/* 
	 * This routine convert Date-string which is implemented in relation to 
	 *  RFC3339 string to java.util.Date object.
	 * */
	private static Date convStr2Date(String datestring) throws ParseException, IndexOutOfBoundsException {
		Date d = new Date();

		Log.d(TAG, "[convStr2Date] datestring : " + datestring);

		if(datestring == null) {
			return null;
		}

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
		} else {
			String wholedayPattern = "^([0-9]+)-([0-9]+)-([0-9]+)$";
			String normaldayPattern = "^([0-9]+)-([0-9]+)-([0-9]+)T([0-9]+):([0-9]+):([0-9]+)";

			Matcher wholeMatcher = Pattern.compile(wholedayPattern).matcher(datestring);
			Matcher normalMatcher = Pattern.compile(normaldayPattern).matcher(datestring);

			if(wholeMatcher.find()) {
				d.setYear(Integer.valueOf(wholeMatcher.group(1)) - 1900);
				d.setMonth(Integer.valueOf(wholeMatcher.group(2)) - 1);
				d.setDate(Integer.valueOf(wholeMatcher.group(3)));
				d.setHours(0);
				d.setMinutes(0);
				d.setSeconds(0);

				return d;
			} else if(normalMatcher.find()) {
				d.setYear(Integer.valueOf(normalMatcher.group(1)) - 1900);
				d.setMonth(Integer.valueOf(normalMatcher.group(2)) - 1);
				d.setDate(Integer.valueOf(normalMatcher.group(3)));
				d.setHours(Integer.valueOf(normalMatcher.group(4)));
				d.setMinutes(Integer.valueOf(normalMatcher.group(5)));
				d.setSeconds(Integer.valueOf(normalMatcher.group(6)));

				return d;
			}
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

	private static String convDate2Str(Date date) {
		String ret = null;

		if(date != null) {
			ret = String.format("%04d-%02d-%02dT%02d:%02d:%02d",
					date.getYear() + 1900, date.getMonth() + 1, date.getDate(),
					date.getHours(), date.getMinutes(), date.getSeconds());
		}

		return ret;
	}
	
	private static String gcalClientLogin(String id, String passwd) {
		String ret = null;
		StringBuffer payload = new StringBuffer();

		try {
			URL url = new URL(ClientLoginURL);
			String postData = String.format("Email=%s&Passwd=%s&service=cl&source=%s", id, passwd, CALENDAR_SOURCE);
	
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setReadTimeout(50000);
			con.setRequestMethod("POST");
			con.addRequestProperty("Accept-Charset", "UTF-8");
			con.addRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			con.setDoOutput(true);
			con.connect();
	
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(con.getOutputStream(), "UTF-8"));

			writer.write(postData);
			writer.flush();
			writer.close();
		
			/* read result */
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));

			Pattern pattern = Pattern.compile("^Auth=(.*)");
			String line;
			while((line = reader.readLine()) != null) {
				Matcher m = pattern.matcher(line);
				if(m.find()) {
					payload.append(m.group(1));
				}
			}
			
			reader.close();
			con.disconnect();

			ret = payload.toString();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "[gcalClientLogin] " + e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, "[gcalClientLogin] " + e.getMessage());
		}

		Log.d(TAG, "[gcalClientLogin] return : " + payload.toString());

		return ret;
	}

	private static String gcalGetRequest(String urlStr) {
		String ret = null;
		StringBuffer payload = new StringBuffer();

		try {
			URL url = new URL(urlStr);
	
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setReadTimeout(100000);
			con.setRequestMethod("GET");
			con.addRequestProperty("Accept-Charset", "UTF-8");
			con.addRequestProperty("Authorization", "GoogleLogin auth=" + accessToken); con.setDoInput(true);
			con.connect();
	
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"), 100000);

			String line;
			while((line = reader.readLine()) != null) {
				payload.append(line);
				Log.d(TAG, String.format("[gcalGetRequest] [%d] > %s", line.length(), line));
			}
			
			reader.close();
			con.disconnect();

			ret = payload.toString();
		} catch (FileNotFoundException e) {
			Log.e(TAG, "[gcalGetRequest] " + e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, "[gcalGetRequest] " + e.getMessage());
		}

		return ret;
	}

	/* 
	 * This routine return request code. 
	 * When something error is occured this routine returns -1.
	 * */
	private static int gcalRequest(String urlStr, String protocol) {
		int ret = -1;

		try {
			URL url = new URL(urlStr);
	
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setReadTimeout(50000);
			con.setRequestMethod(protocol);
			con.addRequestProperty("Accept-Charset", "UTF-8");
			con.addRequestProperty("Content-Type", "application/atom+xml");
			con.addRequestProperty("Authorization", "GoogleLogin auth=" + accessToken); con.setDoInput(true);
			con.setDoOutput(true);
			con.setInstanceFollowRedirects(true);
			con.connect();
		
			ret = con.getResponseCode();
			
			con.disconnect();
		} catch (IOException e) {
			Log.e(TAG, "IOException", e);
		}

		return ret;
	}

	private static String gcalPostRequest(String urlStr, String postData, String protocol) {
		String ret = null;
		StringBuffer payload = new StringBuffer();

		Log.d(TAG, String.format("url:%s, protocol:%s", urlStr, protocol));

		try {
			URL url = new URL(urlStr);
	
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setReadTimeout(100000);
			con.setRequestMethod(protocol);
			con.addRequestProperty("Accept-Charset", "UTF-8");
			con.addRequestProperty("Content-Type", "application/atom+xml");
			con.addRequestProperty("Authorization", "GoogleLogin auth=" + accessToken); con.setDoInput(true);
			con.setDoOutput(true);
			con.setInstanceFollowRedirects(true);
			con.connect();
	
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(con.getOutputStream(), "UTF-8"));
		
			if(postData != null) {
				writer.write(postData);
			}

			writer.flush();
			writer.close();
			
			/* read result */
			BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));

			Log.d(TAG, String.format("[gcalPostRequest] response code : %d", con.getResponseCode()));
			//Map<String, List<String>> responseHeaders = con.getHeaderFields();
			//Set<String> keys = responseHeaders.keySet();

			//for(Iterator<String> i = keys.iterator(); i.hasNext(); ) {
				//String key = i.next();
				//List<String> values = responseHeaders.get(key);

				//for(int j=0; (values != null && j<values.size()); j++) {
					//Log.d(TAG, String.format("[gcalPostRequest] %s[%d] : %s", key, j, values.get(j)));
				//}
			//}

			String line;
			while((line = reader.readLine()) != null) {
				Log.d(TAG, "[gcalPostRequest] " + line);
				payload.append(line);
			}

			if(con.getResponseCode() == 302) {
				Map<String, List<String>> responseHeaders = con.getHeaderFields();
				String redirectURL = responseHeaders.get("location").get(0);
				
				Log.d(TAG, "[gcalPostRequest] redirectURL : " + redirectURL);

				ret = gcalPostRequest(redirectURL, postData, protocol);

				Log.d(TAG, "[gcalPostRequest] responseStr : " + ret);
			} else {
				ret = payload.toString();
			}
			
			reader.close();
			con.disconnect();
		} catch (IOException e) {
			Log.e(TAG, "IOException", e);
		}

		return ret;
	}

	private static String makeSendDocTemplate(ScheduleContent doc, Context index) {
		StringBuffer ret = new StringBuffer();
		BufferedReader readFile = null;
		HashMap<String,String> entryValues = getEntryValues(doc);
		
		try {
			readFile = new BufferedReader(new InputStreamReader(index.getResources().openRawResource(R.raw.add_event)));
		} catch(Exception e) {
			e.printStackTrace();
		}
	
		String line;
		try {
			while((line = readFile.readLine()) != null) {
				String appendLine = line;
				Matcher m = Pattern.compile("^(.*?)\\$\\{(.*?)\\}(.*?)$").matcher(line);
				if(m.find()) {
					String value = entryValues.get(m.group(2));

					Log.d(TAG, "[makeSendDocTemplate] match pattern value : " + value);

					if(value != null) {
						appendLine = m.group(1) + value + m.group(3);
					} else {
						appendLine = m.group(1) + m.group(3);
					}
				}

				ret.append(appendLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				readFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return ret.toString();
	}

	private static HashMap getEntryValues(ScheduleContent doc) {
		HashMap<String, String> ret = new HashMap<String, String> ();

		if(doc != null) {
			Log.d(TAG, String.format("title : %s", doc.getSubject()));
			Log.d(TAG, String.format("content : %s", doc.getContext()));
			Log.d(TAG, String.format("start_time : %s", convDate2Str(doc.getStartTime())));
			Log.d(TAG, String.format("end_time : %s", convDate2Str(doc.getEndTime())));

			ret.put("title", doc.getSubject());
			ret.put("content", doc.getContext());
			ret.put("start_time", convDate2Str(doc.getStartTime()));
			ret.put("end_time", convDate2Str(doc.getEndTime()));
		}

		return ret;
	}
	
	private static void updateDocumentStatus(ScheduleContent doc, String xmlContent) {
		try {
			XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();

			try {
				parser.setInput(new StringReader(xmlContent));
			} catch (NotFoundException e) {
				Log.e(TAG, "[updateDocumentStatus] XML_ERROR "+e.getMessage());
			} catch (XmlPullParserException e) {
				Log.e(TAG, "[updateDocumentStatus] XML_ERROR "+e.getMessage());
			}
			
			
			for(int e = parser.getEventType(); e != XmlPullParser.END_DOCUMENT; e = parser.next()) {
				if(e == XmlPullParser.START_TAG) {
					String elemName = parser.getName();
					String relStr = parser.getAttributeValue(null, "rel");

					if(elemName.equals("entry")) {
						parseEntry(parser, doc);

						break;
					}
				}
			}

		} catch (NotFoundException e) {
			Log.e(TAG, "[updateDocumentStatus] XML " + e.getClass().getName() + ": " + e.getMessage());
		} catch ( XmlPullParserException e) {
			Log.e(TAG, "[updateDocumentStatus] XML " + e.getClass().getName() + ": " + e.getMessage());
			for ( StackTraceElement s : e.getStackTrace()) {
				Log.e(TAG, "[updateDocumentStatus] TRACE " + s.toString());
			}
		} catch ( IOException e) {
			Log.e(TAG, "[updateDocumentStatus] XML " + e.getClass().getName() + ": " + e.getMessage());
		}
	}
}
