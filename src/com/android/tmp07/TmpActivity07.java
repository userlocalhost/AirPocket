package com.android.tmp07;

import android.app.Activity;
import android.os.Bundle;

import android.widget.DatePicker;
import android.widget.Button;

import android.content.Intent;
import android.app.DatePickerDialog;
import android.view.View;
import android.util.Log;

import java.util.Calendar;
import java.text.DateFormat;

public class TmpActivity07 extends Activity
{
	Calendar targetDate = Calendar.getInstance();
	DateFormat fmtDate = DateFormat.getDateInstance(DateFormat.MEDIUM);
	Button dateDisplay;

	DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener(){
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth){
			targetDate.set(Calendar.YEAR, year);
			targetDate.set(Calendar.MONTH, monthOfYear);
			targetDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
			updateDateDisplay();
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		dateDisplay = (Button)findViewById(R.id.input_date);
		dateDisplay.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				new DatePickerDialog(TmpActivity07.this, d,
					targetDate.get(Calendar.YEAR),
					targetDate.get(Calendar.MONTH),
					targetDate.get(Calendar.DAY_OF_MONTH)).show();
			}
		});

		Button displayButton = (Button)findViewById(R.id.display_eventindex_day);
		displayButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				Intent intent = new Intent(TmpActivity07.this, EventIndexDay.class);
				intent.putExtra(EventIndexDay.KEY_DATE, targetDate);

				startActivity(intent);
			}
		});

		updateDateDisplay();
	}

	private void updateDateDisplay(){
		dateDisplay.setText(fmtDate.format(targetDate.getTime()));
	}
}
