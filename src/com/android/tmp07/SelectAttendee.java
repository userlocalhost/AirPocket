package com.android.tmp07;

import android.app.Activity;
import android.os.Bundle;

import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.content.Intent;
import android.net.Uri;
import android.database.Cursor;
import android.provider.Contacts;

import android.util.Log;

public class SelectAttendee extends Activity
{
	private static final String TAG = "SelectAttendee";
	
	private static final int FP = ViewGroup.LayoutParams.FILL_PARENT;
	private static final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	private static final float textSize = 18f;

	OnClickListener selectItem = new View.OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent();

			intent.putExtra(EditEvent.KEY_ATTENDEE, ((TextView) v).getText());
			setResult(Activity.RESULT_OK, intent);

			finish();
		}
	};
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_select_attendee);

		LinearLayout listBoard = (LinearLayout) findViewById(R.id.attendee_list);

		Cursor cursor = managedQuery(Contacts.ContactMethods.CONTENT_EMAIL_URI, null, null, null, null);
		int columnIndex = cursor.getColumnIndex(Contacts.ContactMethods.DATA);

		if(columnIndex != -1 ) {
			cursor.moveToFirst();
			do {
				TextView row = new TextView(this);
				String email = cursor.getString(columnIndex);

				row.setText(email);
				row.setOnClickListener(selectItem);
				row.setTextSize(textSize);
				row.setBackgroundResource(R.drawable.attendee_list_row);

				listBoard.addView(row, new ViewGroup.LayoutParams(FP, WC));
			} while(cursor.moveToNext());
		}
	}
}

