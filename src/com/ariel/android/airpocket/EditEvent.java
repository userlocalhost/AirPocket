package com.ariel.android.airpocket;

import android.app.Activity;
import android.os.Bundle;

import android.widget.Toast;
import android.widget.TimePicker;
import android.widget.DatePicker;
import android.widget.Button;
import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ArrayAdapter;

import android.view.View;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;

import android.app.TimePickerDialog;
import android.app.DatePickerDialog;

import android.net.Uri;
import android.database.Cursor;
import android.provider.Contacts;
import android.content.Intent;
import android.util.Log;

import java.util.Date;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.ArrayList;
import java.text.DateFormat;
import java.lang.Exception;

public class EditEvent extends Activity
{
	public static final String KEY_DATE =
		"com.ariel.android.airpocket.editevent.date";
	public static final String KEY_STATUS =
		"com.ariel.android.airpocket.editevent.status";
	public static final String KEY_OBJID =
		"com.ariel.android.airpocket.editevent.objid";
	public static final String KEY_SUBJECT =
		"com.ariel.android.airpocket.editevent.subject";
	public static final String KEY_CONTEXT =
		"com.ariel.android.airpocket.editevent.context";
	public static final String KEY_STARTTIME =
		"com.ariel.android.airpocket.editevent.starttime";
	public static final String KEY_ENDTIME =
		"com.ariel.android.airpocket.editevent.endtime";
	public static final String KEY_LABEL_RESOURCEID =
		"com.ariel.android.airpocket.editevent.label_resourceid";

	public static final int StatusEdit = 1<<0;
	public static final int StatusAllday = 1<<1;
	public static final int StatusEditLabel = 1<<2;
	public static final int StatusEditAttendee = 1<<3;

	private static final String TAG = "EditEvent";
	private static final String TIME_CONFIG_ALERT = "終了時刻が開始時刻の前に設定されています。";
	private static final String BLANK_ADDRESS_ALERT = "アドレス帳に Email 登録がありません。";
	private static final String NONCOMPATIBLE_ALERT = "アドレス帳に対応していません。";
	
	private static final int FP = ViewGroup.LayoutParams.FILL_PARENT;
	private static final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;

	private Calendar currentDate;
	private Calendar startTime;
	private Calendar endTime;

	private int requestStatus;
	private ScheduleContent document;
	private String resourceLabel = null;
	private LinkedList<String> attendeesList;

	/* The following member is used at attendeelist-View */
	private int idCounter = 1;

	OnCheckedChangeListener checkAllDay = new CompoundButton.OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton button, boolean isChecked) {
			if(isChecked) {
				Log.d(TAG, "[checkAllDay] checked");

				((Button) findViewById(R.id.start_date)).setEnabled(false);
				((Button) findViewById(R.id.start_time)).setEnabled(false);
				((Button) findViewById(R.id.end_date)).setEnabled(false);
				((Button) findViewById(R.id.end_time)).setEnabled(false);
			} else {
				Log.d(TAG, "[checkAllDay] UN-checked");

				((Button) findViewById(R.id.start_date)).setEnabled(true);
				((Button) findViewById(R.id.start_time)).setEnabled(true);
				((Button) findViewById(R.id.end_date)).setEnabled(true);
				((Button) findViewById(R.id.end_time)).setEnabled(true);
			}
		}
	};

	OnClickListener submitEvent = new View.OnClickListener(){
		public void onClick(View v){
			CheckBox wholeDayCheck = (CheckBox) findViewById(R.id.check_allday);

			if(startTime.getTime().compareTo(endTime.getTime()) < 0 || wholeDayCheck.isChecked()) {
				makeDocument(startTime.getTime(), endTime.getTime());
	
				Intent i = new Intent();
	
				i.putExtra(KEY_SUBJECT, ((TextView) findViewById(R.id.subject_input)).getText().toString());
				i.putExtra(KEY_CONTEXT, ((TextView) findViewById(R.id.context_input)).getText().toString());
				i.putExtra(KEY_STARTTIME, startTime.getTime());
				i.putExtra(KEY_ENDTIME, endTime.getTime());
	
				setResult(Activity.RESULT_OK, i);
	
				finish();
			} else {
				Toast message = Toast.makeText(EditEvent.this, TIME_CONFIG_ALERT, Toast.LENGTH_SHORT);
				message.show();
			}
		}
	};
	
	OnClickListener cancelEvent = new View.OnClickListener(){
		public void onClick(View v){

			if(startTime.getTime().compareTo(endTime.getTime()) < 0) {
				Log.d(TAG, "[debug] startTime < endTime");
			}

			if(startTime.getTime().compareTo(endTime.getTime()) == 0) {
				Log.d(TAG, "[debug] startTime == endTime");
			}

			if(startTime.getTime().compareTo(endTime.getTime()) > 0) {
				Log.d(TAG, "[debug] startTime > endTime");
			}

			setResult(Activity.RESULT_CANCELED);
			finish();
		}
	};

	OnClickListener selectAttendee = new View.OnClickListener(){
		public void onClick(View v){
			Cursor cursor = managedQuery(Contacts.ContactMethods.CONTENT_EMAIL_URI, null, null, null, null);
			int columnIndex = cursor.getColumnIndex(Contacts.ContactMethods.DATA);

			if(columnIndex != -1  && cursor != null) {
				Intent intent = new Intent(EditEvent.this, ItemSelect.class);
				ArrayList<String> items = new ArrayList<String> ();

				if(cursor.moveToFirst()) {
					do {
						Log.d(TAG, "[selectAttendee] columnIndex : " + columnIndex);
	
						items.add(cursor.getString(columnIndex));
					} while(cursor.moveToNext());
	
					intent.putExtra(ItemSelect.KEY_ITEMS, items);
	
					startActivityForResult(intent, StatusEditAttendee);
				} else {
					Toast.makeText(EditEvent.this, BLANK_ADDRESS_ALERT, Toast.LENGTH_LONG).show();
				}

			} else {
				Toast.makeText(EditEvent.this, NONCOMPATIBLE_ALERT, Toast.LENGTH_LONG).show();
			}
		}
	};

	OnClickListener selectLabel = new View.OnClickListener(){
		public void onClick(View v){
			Intent intent = new Intent(EditEvent.this, EditLabel.class);

			startActivityForResult(intent, StatusEditLabel);
		}
	};
	
	OnClickListener deleteAttendee = new View.OnClickListener(){
		public void onClick(View v) {
			String deleteAttendeeStr = (String) v.getTag();
			LinearLayout attendeeViewContainer = (LinearLayout) findViewById(R.id.attendee_list);

			for(int i=0; i<attendeeViewContainer.getChildCount(); i++) {
				View child = attendeeViewContainer.getChildAt(i);
				String childStr = (String) child.getTag();

				if(childStr.equals(deleteAttendeeStr)) {
					attendeeViewContainer.removeViewAt(i);
					break;
				}
			}

			for(int i=0; i<attendeesList.size(); i++) {
				if(attendeesList.get(i).equals(deleteAttendeeStr)) {
					attendeesList.remove(i);
					break;
				}
			}
		}
	};
	
	DatePickerDialog.OnDateSetListener startDateEvent = new DatePickerDialog.OnDateSetListener(){
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth){
			startTime.set(Calendar.YEAR, year);
			startTime.set(Calendar.MONTH, monthOfYear);
			startTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			
			updateDateDisplay(startTime, R.id.start_date);
		}
	};
	
	DatePickerDialog.OnDateSetListener endDateEvent = new DatePickerDialog.OnDateSetListener(){
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth){
			endTime.set(Calendar.YEAR, year);
			endTime.set(Calendar.MONTH, monthOfYear);
			endTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			
			updateDateDisplay(endTime, R.id.end_date);
		}
	};

	TimePickerDialog.OnTimeSetListener startTimeEvent = new TimePickerDialog.OnTimeSetListener(){
		public void onTimeSet(TimePicker view, int hour, int minute){
			startTime.set(Calendar.HOUR_OF_DAY, hour);
			startTime.set(Calendar.MINUTE, minute);
		
			updateTimeDisplay(startTime, R.id.start_time);
		}
	};
	
	TimePickerDialog.OnTimeSetListener endTimeEvent = new TimePickerDialog.OnTimeSetListener(){
		public void onTimeSet(TimePicker view, int hour, int minute){
			endTime.set(Calendar.HOUR_OF_DAY, hour);
			endTime.set(Calendar.MINUTE, minute);
		
			updateTimeDisplay(endTime, R.id.end_time);
		}
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		Log.d(TAG, String.format("[onActivityResult]"));

		if((requestCode == StatusEditLabel) && (resultCode == RESULT_OK)){
			ImageView labelImage = (ImageView) findViewById(R.id.show_label);
			int resourceId;

			resourceLabel = data.getStringExtra(KEY_LABEL_RESOURCEID);

			Log.d(TAG, String.format("[onActivityResult] label : %s", resourceLabel));

			resourceId = getResources().getIdentifier(resourceLabel, "drawable", "com.ariel.android.airpocket");
			labelImage.setImageResource(resourceId);
			labelImage.setAdjustViewBounds(true);
			labelImage.invalidate();
		} else if((requestCode == StatusEditAttendee) && (resultCode == RESULT_OK)) {
			String attendeeStr = data.getStringExtra(ItemSelect.KEY_SELECTED_ITEM);
			boolean attendFlag = true;

			for(int i=0; i<attendeesList.size(); i++) {
				if(attendeesList.get(i).equals(attendeeStr)) {
					attendFlag = false;
					break;
				}
			}

			if(attendFlag) {
				addAttendeeView(attendeeStr);
				attendeesList.add(attendeeStr);
			}
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_event);

		requestStatus = getIntent().getIntExtra(KEY_STATUS, 0);
		
		findViewById(R.id.submit).setOnClickListener(submitEvent);
		findViewById(R.id.cancel).setOnClickListener(cancelEvent);

		currentDate = (Calendar) getIntent().getSerializableExtra(KEY_DATE);
		((TextView) findViewById(R.id.title)).setText(String.format("%04d/%02d/%02d の予定作成",
					currentDate.get(Calendar.YEAR),
					currentDate.get(Calendar.MONTH) + 1,
					currentDate.get(Calendar.DAY_OF_MONTH)));
		
		startTime = (Calendar) currentDate.clone();
		endTime = (Calendar) currentDate.clone();

		/* Set default endTime */
		endTime.set(Calendar.HOUR_OF_DAY, startTime.get(Calendar.HOUR_OF_DAY) + 1);

		/* Initialize processing of time-and-date configuration */
		initButtonEvent();

		((CheckBox) findViewById(R.id.check_allday)).setOnCheckedChangeListener(checkAllDay);

		if((requestStatus & StatusEdit) > 0) {
			document = ScheduleContent.getFromId(getIntent().getStringExtra(KEY_OBJID));

			((TextView) findViewById(R.id.subject_input)).setText(document.getSubject());
			((TextView) findViewById(R.id.context_input)).setText(document.getContext());

			startTime.setTime(document.getStartTime());
			endTime.setTime(document.getEndTime());

			resourceLabel = document.getResourceLabel();
			if(resourceLabel != null) {
				int resourceId = getResources().getIdentifier(resourceLabel, "drawable", "com.ariel.android.airpocket");
				ImageView labelImage = (ImageView) findViewById(R.id.show_label);

				labelImage.setImageResource(resourceId);
				labelImage.setAdjustViewBounds(true);
				labelImage.invalidate();
			}

			/* set display of attendees */
			attendeesList = document.getAttendee();
			if(attendeesList != null) {
				for(int i=0; i<attendeesList.size(); i++) {
					addAttendeeView(attendeesList.get(i));
				}
			}
		} else {
			attendeesList = new LinkedList<String> ();
		}

		if((requestStatus & StatusAllday) > 0) {
			((CheckBox) findViewById(R.id.check_allday)).setChecked(true);
		}

		updateTimeDisplay(startTime, R.id.start_time);
		updateTimeDisplay(endTime, R.id.end_time);
		
		updateDateDisplay(startTime, R.id.start_date);
		updateDateDisplay(endTime, R.id.end_date);

		/* initialize of each buttom click-event */
		findViewById(R.id.edit_label_button).setOnClickListener(selectLabel);
		findViewById(R.id.attendee_add).setOnClickListener(selectAttendee);
	}

	private void initButtonEvent() {
		((Button)findViewById(R.id.start_time)).setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				new TimePickerDialog(EditEvent.this, startTimeEvent,
					startTime.get(Calendar.HOUR_OF_DAY),
					startTime.get(Calendar.MINUTE),
					true).show();
			}
		});
		
		((Button)findViewById(R.id.end_time)).setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				new TimePickerDialog(EditEvent.this, endTimeEvent,
					endTime.get(Calendar.HOUR_OF_DAY),
					endTime.get(Calendar.MINUTE),
					true).show();
			}
		});

		((Button)findViewById(R.id.start_date)).setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				new DatePickerDialog(EditEvent.this, startDateEvent,
					startTime.get(Calendar.YEAR),
					startTime.get(Calendar.MONTH),
					startTime.get(Calendar.DAY_OF_MONTH)).show();
			}
		});

		((Button)findViewById(R.id.end_date)).setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				new DatePickerDialog(EditEvent.this, endDateEvent,
					endTime.get(Calendar.YEAR),
					endTime.get(Calendar.MONTH),
					endTime.get(Calendar.DAY_OF_MONTH)).show();
			}
		});
	}

	private void updateDateDisplay(Calendar cal, int viewId) {
		Date timeObj = cal.getTime();
		int year = timeObj.getYear() + 1900;
		int month = timeObj.getMonth() + 1;
		int day = timeObj.getDate();

		((Button)findViewById(viewId)).
			setText(String.format("%04d/%02d/%02d", year, month, day));
	}
	
	private void updateTimeDisplay(Calendar cal, int viewId) {
		Date timeObj = cal.getTime();

		((Button)findViewById(viewId))
			.setText(String.format("%02d:%02d", timeObj.getHours(), timeObj.getMinutes()));
	}

	private void makeDocument(Date startTime, Date endTime) {
		CheckBox checkAllDay = (CheckBox) findViewById(R.id.check_allday);
		String subject = ((TextView) findViewById(R.id.subject_input)).getText().toString();
		String context = ((TextView) findViewById(R.id.context_input)).getText().toString();
		ScheduleContent newDoc;
			
		if((requestStatus & StatusEdit) > 0) {
			newDoc = document;

			newDoc.setSubject(subject);
			newDoc.setContext(context);
			newDoc.setStartTime(startTime);
			newDoc.setEndTime(endTime);

			/* newDoc remove temporaly to recompute location index */
			newDoc.removeObj();
		} else {
			/* create new Document */
			newDoc= new ScheduleContent(subject, context, startTime, endTime);
		}

		/* Is newDoc across multiple days */
		if(! checkSameDate(startTime, endTime)) {
			newDoc.setStatus(ScheduleContent.Multiday);
		}

		if(checkAllDay.isChecked()) {
			newDoc.setStatus(ScheduleContent.Allday);
		}

		if(resourceLabel != null) {
			newDoc.setResourceLabel(resourceLabel);
		}

		newDoc.setAttendee(attendeesList);

		newDoc.regist();
	}

	private void updateDocument() {
		document.setStartTime(startTime.getTime());
		document.setEndTime(endTime.getTime());
		document.setSubject(((TextView) findViewById(R.id.subject_input)).getText().toString());
		document.setContext(((TextView) findViewById(R.id.context_input)).getText().toString());
	}

	private static boolean checkSameDate(Date a, Date b) {
		int aDate = (a.getYear() * 365) + (a.getMonth() * 31) + a.getDate();
		int bDate = (b.getYear() * 365) + (b.getMonth() * 31) + b.getDate();

		Log.d(TAG, String.format("[checkSameDate] aDate:%d, bDate:%d", aDate, bDate));

		return (aDate == bDate);
	}

	private void addAttendeeView(String attendeeStr) {
		try {
			LinearLayout attendeeListView = (LinearLayout) findViewById(R.id.attendee_list);
			RelativeLayout container = new RelativeLayout(this);

			TextView emailAddr = new TextView(this);
			TextView deleteBtn = new TextView(this);

			emailAddr.setId(idCounter++);
			deleteBtn.setId(idCounter++);

			/* initialize display of each attendee */
			emailAddr.setText(attendeeStr);
			emailAddr.setGravity(Gravity.LEFT);

			/* initialize delete button of each attendee */
			deleteBtn.setBackgroundResource(R.drawable.delete_mid);
			deleteBtn.setGravity(Gravity.RIGHT);
			deleteBtn.setOnClickListener(deleteAttendee);
			deleteBtn.setTag(attendeeStr);

			/* set RelativeLayout.LayoutParams */
			RelativeLayout.LayoutParams paramsBtn = new RelativeLayout.LayoutParams(WC, WC);
			RelativeLayout.LayoutParams paramsEmail = new RelativeLayout.LayoutParams(FP, WC);

			paramsBtn.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			paramsEmail.addRule(RelativeLayout.LEFT_OF, deleteBtn.getId());
			paramsEmail.addRule(RelativeLayout.ALIGN_BASELINE, deleteBtn.getId());

			/* set sequence is important ! */
			container.addView(deleteBtn, paramsBtn);
			container.addView(emailAddr, paramsEmail);
			container.setTag(attendeeStr);

			attendeeListView.addView(container, new ViewGroup.LayoutParams(FP, WC));
		} catch(Exception e) {
			Log.e(TAG, "[onActivityResult] (StatusEditAttendee) " + e.getMessage());
		}
	}
}
