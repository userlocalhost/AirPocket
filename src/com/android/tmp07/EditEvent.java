package com.android.tmp07;

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

import android.view.View;
import android.view.View.OnClickListener;


import android.app.TimePickerDialog;
import android.app.DatePickerDialog;

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
		"com.android.tmp07.editevent.date";
	public static final String KEY_STATUS =
		"com.android.tmp07.editevent.status";
	public static final String KEY_OBJID =
		"com.android.tmp07.editevent.objid";
	public static final String KEY_SUBJECT =
		"com.android.tmp07.editevent.subject";
	public static final String KEY_CONTEXT =
		"com.android.tmp07.editevent.context";
	public static final String KEY_STARTTIME =
		"com.android.tmp07.editevent.starttime";
	public static final String KEY_ENDTIME =
		"com.android.tmp07.editevent.endtime";

	public static final int StatusEdit = 1<<0;
	public static final int StatusAllday = 1<<1;

	private static final String TAG = "EditEvent";
	private static final String TIME_CONFIG_ALERT = "終了時刻が開始時刻の前に設定されています。";

	private Calendar currentDate;
	private Calendar startTime;
	private Calendar endTime;

	private int requestStatus;
	private ScheduleContent document;

	OnCheckedChangeListener checkAllDay = new CompoundButton.OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton button, boolean isChecked) {
			if(isChecked) {
				Log.d(TAG, "[checkAllDay] checked");
			} else {
				Log.d(TAG, "[checkAllDay] UN-checked");
			}
		}
	};

	OnClickListener submitEvent = new View.OnClickListener(){
		public void onClick(View v){
			if(startTime.getTime().compareTo(endTime.getTime()) < 0) {
				if((requestStatus & StatusEdit) > 0) {
					document.removeObj();
				}
				makeDocument(startTime.getTime(), endTime.getTime());
	
				Intent i = new Intent();
	
				i.putExtra(KEY_SUBJECT, ((TextView) findViewById(R.id.subject_input)).getText().toString());
				i.putExtra(KEY_CONTEXT, ((TextView) findViewById(R.id.context_input)).getText().toString());
				i.putExtra(KEY_STARTTIME, startTime.getTime());
				i.putExtra(KEY_ENDTIME, endTime.getTime());
	
				setResult(RESULT_OK, i);
	
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

			setResult(RESULT_CANCELED);
			finish();
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
			document = ScheduleContent.getFromId(
					getIntent().getStringExtra(KEY_OBJID));

			((TextView) findViewById(R.id.subject_input)).setText(document.getSubject());
			((TextView) findViewById(R.id.context_input)).setText(document.getContext());

			startTime.setTime(document.getStartTime());
			endTime.setTime(document.getEndTime());
		}

		if((requestStatus & StatusAllday) > 0) {
			((CheckBox) findViewById(R.id.check_allday)).setChecked(true);
		}

		updateTimeDisplay(startTime, R.id.start_time);
		updateTimeDisplay(endTime, R.id.end_time);
		
		updateDateDisplay(startTime, R.id.start_date);
		updateDateDisplay(endTime, R.id.end_date);
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
		ArrayList indexList = new ArrayList();

		CheckBox checkAllDay = (CheckBox) findViewById(R.id.check_allday);
		int i, depth, position;
		int regStartMinutes = (startTime.getHours() * 60) + startTime.getMinutes();
		int regEndMinutes = (endTime.getHours() * 60) + endTime.getMinutes();

		ScheduleContent newDoc = new ScheduleContent(
					((TextView) findViewById(R.id.subject_input)).getText().toString(),
					((TextView) findViewById(R.id.context_input)).getText().toString(),
					startTime, endTime);

		newDoc.setPosition(0, 0);

		/* Is newDoc across multiple days */
		if(! checkSameDate(startTime, endTime)) {
			newDoc.setStatus(ScheduleContent.Multiday);
		}

		if(! checkAllDay.isChecked()) {

			/* check duplicate docs */
			for(i=0; i<ScheduleContent.documents.size(); i++){
				ScheduleContent doc = ScheduleContent.documents.get(i);
				int startMinutes = (doc.getStartTime().getHours() * 60) + doc.getStartTime().getMinutes();
				int endMinutes = (doc.getEndTime().getHours() * 60) + doc.getEndTime().getMinutes();
	
				if((doc.isSameDay(currentDate.getTime()) && 
					! doc.isStatus(ScheduleContent.Allday) && 
					(doc.isJustSameDay(startTime) || doc.isJustSameDay(endTime)))
						&&
					(((regStartMinutes >= startMinutes) && (regStartMinutes < endMinutes)) ||
					((regEndMinutes > startMinutes) && (regEndMinutes < endMinutes)) ||
					((regStartMinutes < startMinutes) && (regEndMinutes > endMinutes)) ||
					(! checkSameDate(endTime, doc.getEndTime()) && checkSameDate(endTime, doc.getStartTime()) && (regEndMinutes > startMinutes)) ||
					(! checkSameDate(startTime, doc.getStartTime()) && checkSameDate(startTime, doc.getEndTime()) && (regStartMinutes > endMinutes)))
				){
					newDoc.addOverlappedId(doc.getId());
					doc.addOverlappedId(newDoc.getId());
	
					doc.setDepth(doc.getDepth() + 1);
					indexList.add(doc.getIndex());
				}
			}

		} else { /* check of allday event */
			newDoc.setStatus(ScheduleContent.Allday);
		}

		int targetIndex = -1;
		int findFlag;
		do{
			findFlag = 1;

			targetIndex++;

			Log.d(TAG, "[makeScheduleFromResult] targetIndex : "+targetIndex);
			for(i=0; i<indexList.size(); i++){
				Log.d(TAG, String.format("[makeScheduleFromResult] %d:%d", i, indexList.get(i)));
				if(targetIndex == indexList.get(i)){
					findFlag = 0;
					break;
				}
			}
		}while(findFlag == 0);

		newDoc.setPosition(indexList.size(), targetIndex);

		ScheduleContent.documents.add(newDoc);
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
}
