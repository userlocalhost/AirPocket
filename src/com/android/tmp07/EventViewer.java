package com.android.tmp07;

import android.app.Activity;
import android.os.Bundle;

import android.widget.Toast;
import android.widget.TimePicker;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TableLayout;

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

public class EventViewer extends Activity
{
	public static final String KEY_DATE =
		"com.android.tmp07.eventviewer.date";
	public static final String KEY_OBJ_ID =
		"com.android.tmp07.eventviewer.object_id";

	private static final String TAG = "EventViewer";

	private Calendar currentDate;
	private ScheduleContent document;

	OnClickListener selectEdit = new View.OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(EventViewer.this, EditEvent.class);

			intent.putExtra(EditEvent.KEY_DATE, currentDate);
			intent.putExtra(EditEvent.KEY_STATUS, EditEvent.StatusEdit);
			intent.putExtra(EditEvent.KEY_OBJID, document.getId());

			finish();
			startActivity(intent);
		}
	};

	OnClickListener selectDelete = new View.OnClickListener() {
		public void onClick(View v) {
			Log.d(TAG, "[selectDelete]");
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Date startTime;
		Date endTime;

		super.onCreate(savedInstanceState);
		setContentView(R.layout.eventviewer);
		
		//findViewById(R.id.submit).setOnClickListener(submitEvent);
		//findViewById(R.id.cancel).setOnClickListener(cancelEvent);

		document = ScheduleContent.getFromId(getIntent().getStringExtra(KEY_OBJ_ID));

		startTime = document.getStartTime();
		endTime = document.getEndTime();

		currentDate = (Calendar) getIntent().getSerializableExtra(KEY_DATE);
		((TextView) findViewById(R.id.title)).setText(String.format("%d/%02d/%02d の予定",
					currentDate.get(Calendar.YEAR),
					currentDate.get(Calendar.MONTH) + 1,
					currentDate.get(Calendar.DAY_OF_MONTH)));

		/* display setting */
		((TextView) findViewById(R.id.time)).
			setText(String.format("%02d:%02d - %02d:%02d",
				startTime.getHours(), startTime.getMinutes(),
				endTime.getHours(), endTime.getMinutes()));

		((TextView) findViewById(R.id.subject_context)).setText(document.getSubject());
		((TextView) findViewById(R.id.context)).setText(document.getContext());
		
		((TableLayout) findViewById(R.id.ev_viewer_operations)).setStretchAllColumns(true);

		findViewById(R.id.evv_operation_edit).setOnClickListener(selectEdit);
		findViewById(R.id.evv_operation_delete).setOnClickListener(selectDelete);
	}
}
