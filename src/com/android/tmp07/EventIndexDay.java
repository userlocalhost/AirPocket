package com.android.tmp07;

import android.app.Activity;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout;

import android.content.Intent;
import android.util.Log;

import java.util.Date;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Calendar;

import java.lang.Exception;

public class EventIndexDay extends Activity
{
	public static final String KEY_DATE = "com.android.tmp07.eventindexday.date";
	public static Calendar currentDate;

	private static final int FP = ViewGroup.LayoutParams.FILL_PARENT;
	private static final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	private static final String TAG = "EventIndexDay";
	private static String currentDateString;

	private LinkedList<TextView> docsBuffer = new LinkedList<TextView>();

	OnClickListener selectTitle = new View.OnClickListener() {
		public void onClick(View v) {
			finish();
		}
	};
	
	OnClickListener selectAlldayBoard = new View.OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(EventIndexDay.this, EventListView.class);
			Calendar sendCal = (Calendar) currentDate.clone();
		
			sendCal.set(Calendar.MINUTE, 0);

			intent.putExtra(EventListView.KEY_DATE, currentDate);
			intent.putExtra(EventListView.KEY_STATUS, EventListView.statusAllEvents);

			startActivityForResult(intent, 1);
		}
	};

	public void moveDate(int direction) {
		int prevDay = currentDate.get(Calendar.DAY_OF_MONTH);
		int prevMonth = currentDate.get(Calendar.MONTH);

		if(direction > 0) {
			currentDate.roll(Calendar.DAY_OF_MONTH, true);

			if(prevDay > currentDate.get(Calendar.DAY_OF_MONTH)){
				currentDate.roll(Calendar.MONTH, true);
			}
			
			if(prevMonth > currentDate.get(Calendar.MONTH)){
				currentDate.roll(Calendar.YEAR, true);
			}
		} else {
			currentDate.roll(Calendar.DAY_OF_MONTH, false);

			if(prevDay < currentDate.get(Calendar.DAY_OF_MONTH)){
				currentDate.roll(Calendar.MONTH, false);
			}
			
			if(prevMonth < currentDate.get(Calendar.MONTH)){
				currentDate.roll(Calendar.YEAR, false);
			}
		}

		finish();

		Intent intent = new Intent(EventIndexDay.this, EventIndexDay.class);

		intent.putExtra(KEY_DATE, currentDate);
		startActivity(intent);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.eventindex_day);

		initLabel();

		redrawAlldayEvents();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if(requestCode == EditEvent.StatusEdit && resultCode == RESULT_OK){
			//makeScheduleFromResult(data);
		}

		redrawAlldayEvents();
	}

	private void makeScheduleFromResult(Intent data) {
		Date startTime = (Date) data.getSerializableExtra(EditEvent.KEY_STARTTIME);
		Date endTime = (Date) data.getSerializableExtra(EditEvent.KEY_ENDTIME);

		LinkedList<ScheduleContent> duplicateDocs = new LinkedList<ScheduleContent>();
		ArrayList indexList = new ArrayList();

		int i, depth, position;
		int regStartMinutes = (startTime.getHours() * 60) + startTime.getMinutes();
		int regEndMinutes = (endTime.getHours() * 60) + endTime.getMinutes();

		ScheduleContent newDoc = new ScheduleContent(
					data.getStringExtra(EditEvent.KEY_SUBJECT),
					data.getStringExtra(EditEvent.KEY_CONTEXT),
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
					((regEndMinutes > startMinutes) && (regEndMinutes < endMinutes))) {

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

	protected void showEditActivity(int hour, int minute) {
		Intent intent;
		Calendar sendCal = (Calendar) currentDate.clone();

		sendCal.set(Calendar.HOUR_OF_DAY, hour);
		sendCal.set(Calendar.MINUTE, minute);

		if(ScheduleContent.isConformScheduleFromTime(sendCal.getTime())){
			intent = new Intent(this, EventListView.class);
			intent.putExtra(EventListView.KEY_DATE, sendCal);
		}else{
			intent = new Intent(this, EditEvent.class);
			intent.putExtra(EditEvent.KEY_DATE, sendCal);
		}

		startActivityForResult(intent, EditEvent.StatusEdit);
	}

	private void initLabel() {
		try {
			TextView current_date = (TextView) findViewById(R.id.ev_day_index_current_date);
			currentDate = (Calendar) getIntent().getSerializableExtra(KEY_DATE);
	
			currentDateString = String.format("%04d/%02d/%02d", 
						currentDate.get(Calendar.YEAR),
						currentDate.get(Calendar.MONTH) + 1,
						currentDate.get(Calendar.DAY_OF_MONTH));
	
			current_date.setTextColor(getResources().getColor(R.color.date_text));
			current_date.setText(currentDateString);

			findViewById(R.id.ev_day_index_current_date).setOnClickListener(selectTitle);
		} catch(Exception e) {
			Log.e(TAG, "[onCreate] ERROR:"+e.getMessage());
		}
	}

	private void redrawAlldayEvents() {
		LinearLayout alldayBoard = (LinearLayout) findViewById(R.id.ev_day_index_wholeday_list);
		ArrayList events = ScheduleContent.grepAllday(currentDate.getTime());
	
		/* clear all-day events on current board */
		clearDocsBuffer();

		alldayBoard.setOnClickListener(selectAlldayBoard);

		for(int i=0; i<events.size(); i++) {
			ScheduleContent doc = (ScheduleContent) events.get(i);
			TextView item = new TextView(this);
			item.setTextColor(getResources().getColor(R.color.normal_text));
			item.setBackgroundResource(R.drawable.event_wholeday);
			item.setGravity(Gravity.CENTER);
			item.setText(doc.getSubject());
	
			docsBuffer.add(item);
			//alldayBoard.addView(item, new ViewGroup.LayoutParams(FP, WC));
			alldayBoard.addView(item);
		}
	}

	private void clearDocsBuffer() {
		LinearLayout alldayBoard = (LinearLayout) findViewById(R.id.ev_day_index_wholeday_list);

		for(int i=0; i<docsBuffer.size(); i++) {
			alldayBoard.removeView((TextView)docsBuffer.get(i));
		}

		docsBuffer.clear();
	}
}
