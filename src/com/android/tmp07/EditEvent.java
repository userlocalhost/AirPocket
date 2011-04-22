package com.android.tmp07;

import android.app.Activity;
import android.os.Bundle;

import android.widget.Toast;
import android.widget.TimePicker;
import android.widget.Button;
import android.widget.TextView;

import android.view.View;
import android.view.View.OnClickListener;

import android.app.TimePickerDialog;

import android.content.Intent;
import android.util.Log;

import java.util.Date;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.ArrayList;
import java.text.DateFormat;

public class EditEvent extends Activity
{
	public static final String KEY_DATE =
		"com.android.tmp07.editevent.date";
	public static final String KEY_SUBJECT =
		"com.android.tmp07.editevent.subject";
	public static final String KEY_CONTEXT =
		"com.android.tmp07.editevent.context";
	public static final String KEY_STARTTIME =
		"com.android.tmp07.editevent.starttime";
	public static final String KEY_ENDTIME =
		"com.android.tmp07.editevent.endtime";

	public static final int requestOfEditEvent = 0;

	private static final String TAG = "EditEvent";
	private static final String TIME_CONFIG_ALERT = "終了時刻が開始時刻の前に設定されています。";

	private Calendar currentDate;
	private Calendar startTime;
	private Calendar endTime;

	OnClickListener submitEvent = new View.OnClickListener(){
		public void onClick(View v){
			int startMinutes = (startTime.get(Calendar.HOUR_OF_DAY) * 60) + startTime.get(Calendar.MINUTE);
			int endMinutes = (endTime.get(Calendar.HOUR_OF_DAY) * 60) + endTime.get(Calendar.MINUTE);

			if(endMinutes > startMinutes) {
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
			setResult(RESULT_CANCELED);
			finish();
		}
	};

	TimePickerDialog.OnTimeSetListener startEvent = new TimePickerDialog.OnTimeSetListener(){
		public void onTimeSet(TimePicker view, int hour, int minute){
			startTime.set(Calendar.HOUR_OF_DAY, hour);
			startTime.set(Calendar.MINUTE, minute);
		
			updateTimeDisplay(startTime, R.id.start_time);
		}
	};
	
	TimePickerDialog.OnTimeSetListener endEvent = new TimePickerDialog.OnTimeSetListener(){
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
		
		findViewById(R.id.submit).setOnClickListener(submitEvent);
		findViewById(R.id.cancel).setOnClickListener(cancelEvent);

		currentDate = (Calendar) getIntent().getSerializableExtra(KEY_DATE);
		((TextView) findViewById(R.id.title)).setText(String.format("%d/%02d/%02d の予定作成",
					currentDate.get(Calendar.YEAR),
					currentDate.get(Calendar.MONTH) + 1,
					currentDate.get(Calendar.DAY_OF_MONTH)));
		
		startTime = (Calendar) currentDate.clone();
		endTime = (Calendar) currentDate.clone();

		endTime.set(Calendar.HOUR_OF_DAY, startTime.get(Calendar.HOUR_OF_DAY) + 1);

		((Button)findViewById(R.id.start_time)).setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				new TimePickerDialog(EditEvent.this, startEvent,
					startTime.get(Calendar.HOUR_OF_DAY),
					startTime.get(Calendar.MINUTE),
					true).show();
			}
		});
		
		((Button)findViewById(R.id.end_time)).setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				new TimePickerDialog(EditEvent.this, endEvent,
					endTime.get(Calendar.HOUR_OF_DAY),
					endTime.get(Calendar.MINUTE),
					true).show();
			}
		});

		updateTimeDisplay(startTime, R.id.start_time);
		updateTimeDisplay(endTime, R.id.end_time);
	}
	
	private void updateTimeDisplay(Calendar cal, int viewId)
	{
		Date timeObj = cal.getTime();

		((Button)findViewById(viewId))
			.setText(String.format("%02d:%02d", timeObj.getHours(), timeObj.getMinutes()));
	}

	private void makeDocument(Date startTime, Date endTime) {
		LinkedList<ScheduleContent> duplicateDocs = new LinkedList<ScheduleContent>();
		ArrayList indexList = new ArrayList();

		int i, depth, position;
		int regStartMinutes = (startTime.getHours() * 60) + startTime.getMinutes();
		int regEndMinutes = (endTime.getHours() * 60) + endTime.getMinutes();

		ScheduleContent newDoc = new ScheduleContent(
					((TextView) findViewById(R.id.subject_input)).getText().toString(),
					((TextView) findViewById(R.id.context_input)).getText().toString(),
					startTime, endTime);

		newDoc.setPosition(0, 0);

		Log.d(TAG, String.format("[makeScheduleFromResult] regtime:(%d,%d)", regStartMinutes, regEndMinutes));

		/* check duplicate docs */
		for(i=0; i<ScheduleContent.documents.size(); i++){
			ScheduleContent doc = ScheduleContent.documents.get(i);
			if(doc.isSameDay(currentDate.getTime())) {
				int startMinutes = (doc.getStartTime().getHours() * 60) + doc.getStartTime().getMinutes();
				int endMinutes = (doc.getEndTime().getHours() * 60) + doc.getEndTime().getMinutes();

				Log.d(TAG, String.format("[makeScheduleFromResult] prev[%d]:(%d,%d)", i, startMinutes, endMinutes));
				if(((regStartMinutes >= startMinutes) && (regStartMinutes < endMinutes)) ||
					((regEndMinutes > startMinutes) && (regEndMinutes < endMinutes)) ||
					((regStartMinutes < startMinutes) && (regEndMinutes > endMinutes))) {

					doc.setDepth(doc.getDepth() + 1);
					indexList.add(doc.getIndex());
				}
			}
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
}
