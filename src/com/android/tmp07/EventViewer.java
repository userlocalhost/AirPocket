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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import android.app.TimePickerDialog;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;

import android.content.Intent;
import android.content.DialogInterface;

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
	public static final String KEY_STATUS =
		"com.android.tmp07.eventviewer.status";

	private static final String TAG = "EventViewer";
	
	private static int REQUEST_SET_GOOGLE_ACCOUNT_INFO = (1 << 0);

	private Calendar currentDate;
	private ScheduleContent document;
	private int eventStatus;

	OnClickListener selectEdit = new View.OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(EventViewer.this, EditEvent.class);

			intent.putExtra(EditEvent.KEY_DATE, currentDate);
			intent.putExtra(EditEvent.KEY_STATUS, EditEvent.StatusEdit | eventStatus);
			intent.putExtra(EditEvent.KEY_OBJID, document.getId());

			finish();
			startActivity(intent);
		}
	};

	OnClickListener selectDelete = new View.OnClickListener() {
		public void onClick(View v) {

			new AlertDialog.Builder(EventViewer.this)
				.setTitle("予定の削除")
				.setMessage(String.format("%s を削除してもいいですか？", document.getSubject()))
				.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dlg, int sumthin) {

							/* delete google calendars' one, if there is */
							ServerInterface.delDocument(document);

							document.removeObj();

							Intent intent = new Intent();
							setResult(RESULT_OK, intent);
							finish();
						}
					})
				.setNeutralButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dlg, int sumthin) {
							// nope
						}
					})
				.show();
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Date startTime;
		Date endTime;
		String timeStr;

		super.onCreate(savedInstanceState);
		setContentView(R.layout.eventviewer);
		
		//findViewById(R.id.submit).setOnClickListener(submitEvent);
		//findViewById(R.id.cancel).setOnClickListener(cancelEvent);

		eventStatus = getIntent().getIntExtra(KEY_STATUS, 0);
		document = ScheduleContent.getFromId(getIntent().getStringExtra(KEY_OBJ_ID));

		startTime = document.getStartTime();
		endTime = document.getEndTime();

		currentDate = (Calendar) getIntent().getSerializableExtra(KEY_DATE);
		((TextView) findViewById(R.id.title)).setText(String.format("%04d/%02d/%02d の予定",
					currentDate.get(Calendar.YEAR),
					currentDate.get(Calendar.MONTH) + 1,
					currentDate.get(Calendar.DAY_OF_MONTH)));

		if(document.isStatus(ScheduleContent.Allday) && document.isStatus(ScheduleContent.Multiday)) {
			timeStr = String.format("[終日] %04d/%02d/%02d - %04d/%02d/%02d",
					startTime.getYear() + 1900, startTime.getMonth() + 1, startTime.getDate(),
					endTime.getYear() + 1900, endTime.getMonth() + 1, endTime.getDate());
		} else if(document.isStatus(ScheduleContent.Allday)) {
			timeStr = String.format("[終日] %04d/%02d/%02d",
					startTime.getYear() + 1900, startTime.getMonth() + 1, startTime.getDate());
		} else {
			timeStr = String.format("%02d:%02d - %02d:%02d",
				startTime.getHours(), startTime.getMinutes(),
				endTime.getHours(), endTime.getMinutes());
		}

		/* display setting */
		((TextView) findViewById(R.id.time)).setText(timeStr);

		((TextView) findViewById(R.id.subject_context)).setText(document.getSubject());
		((TextView) findViewById(R.id.context)).setText(document.getContext());
		
		((TableLayout) findViewById(R.id.ev_viewer_operations)).setStretchAllColumns(true);

		findViewById(R.id.evv_operation_edit).setOnClickListener(selectEdit);
		findViewById(R.id.evv_operation_delete).setOnClickListener(selectDelete);

		/* set label-image if there is */
		//String labelName = document.getResourceLabel();
		//if(labelName != null) {
			//TextView contextView = (TextView) findViewById(R.id.context);
			//int resourceId = getResources().getIdentifier(labelName, "drawable", "com.android.tmp07");

			//contextView.setBackgroundResource(resourceId);
		//}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_event_viewer, menu);

		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menuSendGoogleCalendar:
			if(ServerInterface.isLogined()) {
				boolean ret = ServerInterface.putDocument(document, this);
				if(ret) {
					Toast.makeText(this, "予定を同期させました", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(this, "予定の同期に失敗しました", Toast.LENGTH_LONG).show();
				}
			} else {
				Intent intent = new Intent(this, InputTwoColumns.class);

				intent.putExtra(InputTwoColumns.KEY_TITLE, "google account を入力してください");

				startActivityForResult(intent, REQUEST_SET_GOOGLE_ACCOUNT_INFO);
			}
			break;
		}
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK && requestCode == REQUEST_SET_GOOGLE_ACCOUNT_INFO) {
			String id = data.getStringExtra(InputTwoColumns.KEY_FIRST_COLUMN);
			String passwd = data.getStringExtra(InputTwoColumns.KEY_SECOND_COLUMN);
			
			AppConfig.setConfig("googleLoginId", id);
			AppConfig.setConfig("googleLoginPasswd", passwd);
			
			boolean ret = ServerInterface.putDocument(document, this);
			if(ret) {
				Toast.makeText(this, "予定を同期させました", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(this, "予定の同期に失敗しました", Toast.LENGTH_LONG).show();
			}
		} else if(requestCode == REQUEST_SET_GOOGLE_ACCOUNT_INFO) {
			Toast.makeText(this, "認証に失敗しました", Toast.LENGTH_LONG).show();
		}
	}
}
