package com.android.tmp07;

import android.app.Activity;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;

import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ListView;

import android.content.Intent;
import android.util.Log;

import java.util.Date;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;

import java.lang.Exception;

public class EventListView extends Activity
{
	public static final String KEY_DATE = "com.android.tmp07.makelistview.date";
	public static final String KEY_STATUS= "com.android.tmp07.makelistview.status";

	public static final int statusAllEvents = (1<<0);

	private static final String TAG = "EventListView";
	private Calendar currentDate;

	OnClickListener makeEvent = new View.OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(EventListView.this, EditEvent.class);
	
			intent.putExtra(EditEvent.KEY_DATE, currentDate);

			finish();

			startActivity(intent);
		}
	};

	OnClickListener selectEvent = new View.OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(EventListView.this, EventViewer.class);
			ScheduleContent doc = (ScheduleContent) v.getTag();

			intent.putExtra(EventViewer.KEY_OBJ_ID, doc.getId());
			intent.putExtra(EventViewer.KEY_DATE, currentDate);

			if(doc.isStatus(ScheduleContent.Allday)) {
				intent.putExtra(EventViewer.KEY_STATUS, EditEvent.StatusAllday);
			}

			finish();

			Log.d(TAG, "[selectEvent:onClick]");

			startActivity(intent);
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		ArrayList docs;

		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_listview);
		
		int listStatus = getIntent().getIntExtra(KEY_STATUS, 0);
		currentDate = (Calendar) getIntent().getSerializableExtra(KEY_DATE);

		List<ScheduleContent> datalist = new ArrayList<ScheduleContent>();

		if((listStatus & statusAllEvents) > 0) {
			docs = ScheduleContent.grepScheduleFromDate(currentDate.getTime());
		} else {
			docs = ScheduleContent.grepScheduleFromTime(currentDate.getTime());
		}

		for(int i=0; i<docs.size(); i++){
			datalist.add((ScheduleContent) docs.get(i));
		}

		ScheduleContentAdapter adapter = new ScheduleContentAdapter(
				this, R.layout.event_listview_rowdata, datalist, selectEvent);

		try {
			TextView current_date = (TextView) findViewById(R.id.ev_list_current_date);

			current_date.setTextColor(getResources().getColor(R.color.date_text));
			current_date.setText(String.format("%d/%02d/%02d", 
						currentDate.get(Calendar.YEAR),
						currentDate.get(Calendar.MONTH) + 1,
						currentDate.get(Calendar.DAY_OF_MONTH)));

			findViewById(R.id.ev_list_make_event).setOnClickListener(makeEvent);

			ListView eventList = (ListView) findViewById(R.id.ev_list_event_listview);
			eventList.setAdapter(adapter);
		} catch(Exception e) {
			Log.e(TAG, "[onCrate] ERROR:"+e.getMessage());
		}

		/*
		TextView current_date = (TextView) findViewById(R.id.current_date);
		currentDate = (Calendar) getIntent().getSerializableExtra(KEY_DATE);

		currentDateString = String.format("%d/%02d/%02d", 
					currentDate.get(Calendar.YEAR),
					currentDate.get(Calendar.MONTH) + 1,
					currentDate.get(Calendar.DAY_OF_MONTH));

		current_date.setTextColor(getResources().getColor(R.color.date_text));
		current_date.setText(currentDateString);

		findViewById(R.id.current_date).setOnClickListener(selectTitle);
		findViewById(R.id.move_prev).setOnClickListener(moveDay);;
		findViewById(R.id.move_next).setOnClickListener(moveDay);;
		*/
	}
}
