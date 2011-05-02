package com.android.tmp07;

import android.app.Activity;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.MotionEvent;
import android.view.Gravity;

import android.widget.RelativeLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.FrameLayout;

import android.widget.TableRow;
import android.widget.TextView;

import android.graphics.Color;

import android.content.Intent;
import android.util.Log;

import java.util.Date;
import java.util.LinkedList;
import java.util.Calendar;
import java.lang.Integer;
import java.lang.Exception;

public class ArielEvent extends Activity
{
	private static final String[] weekLabelJp = {"月", "火", "水", "木", "金", "土", "日"};
	private static final String TAG = "Tmp12";

	/* current activity status */
	private static int statusSuspend = 0;
	private static int statusActive = 1;
	private static final float moveThreashold = 100f;

	private float motionX;
	private int motionStatus = 0;

	private LinkedList<TableRow> contents = new LinkedList<TableRow>();

	protected final Calendar currentDate = Calendar.getInstance();

	/* View size parameter */
	private static final int labelHeight = 25;
	private static final int columnHeight = 60;
	private static final float labelSize = 18f;
	private static final float columnSize = 20f;
	private static final float outsideColumnSize = 15f;

	private int activityStatus;

	OnTouchListener moveMonthMotion = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent e) {
			if(e.getAction() == MotionEvent.ACTION_DOWN) {
				motionX = e.getX();
			} else if(e.getAction() == MotionEvent.ACTION_MOVE) {
				if((e.getX() - motionX) > moveThreashold) {
					motionStatus = -1;
				} else if((e.getX() - motionX) < (moveThreashold * -1)) {
					motionStatus = 1;
				}
			} else if(e.getAction() == MotionEvent.ACTION_UP) {
				if(motionStatus == 0) {
					try {
						int numOfDay = (Integer) v.getTag();
						Calendar sendCal = (Calendar) currentDate.clone();
						Intent intent = new Intent(ArielEvent.this, EventIndexDay.class);
			
						sendCal.set(Calendar.DAY_OF_MONTH, numOfDay);
			
						intent.putExtra(EventIndexDay.KEY_DATE, sendCal);
			
						startActivity(intent);
					} catch(Exception exception) {
						Log.d(TAG, "[moveMonth] ERROR:"+exception.getMessage());
					}
				} else {
					doMoveMonth(motionStatus);
				
					motionX = e.getX();
					motionStatus = 0;
				}
			}
	
			return true;
		}
	};

	@Override
	protected void onResume() {
		super.onResume();

		if(activityStatus == statusSuspend){
			TableLayout mainBoard = (TableLayout) findViewById(R.id.ev_month_index_main_board);
			int length = contents.size();
	
			for(int i=0; i<length; i++){
				mainBoard.removeView(contents.get(i));
			}
	
			contents.clear();
			constructScreen();

			activityStatus = statusActive;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		activityStatus = statusSuspend;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.eventindex_month);

		try {
			findViewById(R.id.ev_month_main_board).setOnTouchListener(moveMonthMotion);
		} catch (Exception e) {
			Log.d(TAG, "[onCreate] ERROR:"+e.getMessage());
		}

		/*to set name of each weekday */
		generateWeekLabel();

		constructScreen();

		activityStatus = statusActive;
	}

	private void constructScreen() {
		/*to set date label "${year} / ${month}" */
		setDateLabel();

		/* 
		 * Main processing to show each day column.
		 * NOTE: Now, the View is replaced into alternative which is TextView. 
		 * 
		 */
		generateMonthView();
	}

	private void setDateLabel() {
		try {
			TextView currentDateView = (TextView) findViewById(R.id.ev_month_index_current_month);
	
			currentDateView.setTextColor(getResources().getColor(R.color.normal_text));
			currentDateView.setText(String.format("%d/%02d",
						currentDate.get(Calendar.YEAR),
						currentDate.get(Calendar.MONTH) + 1));
		} catch (Exception e) {
			Log.e(TAG, "[setDateLabel] ERROR:"+e.getMessage());
		}
	}

	private void generateMonthView() {
		try {
			TableLayout mainBoard = (TableLayout) findViewById(R.id.ev_month_index_main_board);
			Calendar tmpCal = (Calendar) currentDate.clone();
			Calendar now = Calendar.getInstance();
			boolean lastMonthFlag = true;
			boolean endOfMonth = false;
			int columnNum = 6;
			int weekCount;
			int dayCount = 1;
			int daysOfMonth = getDaysOfMonth(tmpCal.get(Calendar.MONTH));
			int daysOfLastMonth = getDaysOfMonth(tmpCal.get(Calendar.MONTH) - 1);

			mainBoard.setStretchAllColumns(true);
			tmpCal.set(Calendar.DAY_OF_MONTH, 1);
			weekCount = tmpCal.get(Calendar.DAY_OF_WEEK);
	
			Log.d(TAG, "[generateYearView] currentYear:"+tmpCal.getTime().getYear());
			Log.d(TAG, "[generateMonthView] currentMonth:"+tmpCal.getTime().getMonth());
	
			for(int i=0; i<columnNum; i++){
				TableRow weekRow = new TableRow(this);
	
				contents.add(weekRow);
		
				/*implementation for drawing last month*/
				if(lastMonthFlag){
					lastMonthFlag = false;
					for(int j=(weekCount-1); j>0; j--){
						TextView day = new TextView(this);
						day.setText(String.format("%s", daysOfLastMonth - j + 1));
						day.setBackgroundColor(getResources().getColor(R.color.outside_background));
						day.setGravity(Gravity.CENTER_HORIZONTAL);
						day.setTextSize(outsideColumnSize);
						day.setHeight(columnHeight);
						day.setPadding(0, (int)(columnSize-outsideColumnSize), 0, 0);
	
						weekRow.addView(day);
					}
				}
	
				for(; weekCount<8; weekCount++){
					if(endOfMonth == false){
						FrameLayout frameLayout = new FrameLayout(this);
						TextView day = new TextView(this);

						day.setTag(dayCount);
						day.setText(String.format("%s", dayCount++));
						day.setBackgroundColor(getResources().getColor(R.color.weekday_background));
						day.setTextColor(getResources().getColor(R.color.normal_text));
						day.setTextSize(columnSize);
						day.setHeight(columnHeight);
						day.setGravity(Gravity.CENTER_HORIZONTAL);
						day.setOnTouchListener(moveMonthMotion);

						frameLayout.addView(day);
					
						if((now.get(Calendar.MONTH) == tmpCal.get(Calendar.MONTH)) && 
							(now.get(Calendar.YEAR) == tmpCal.get(Calendar.YEAR)) && 
							(tmpCal.get(Calendar.DAY_OF_MONTH) == (now.get(Calendar.DAY_OF_MONTH)))) {

							TextView event = new TextView(this);

							event.setBackgroundResource(R.drawable.event_today);
							event.setHeight(columnHeight);

							frameLayout.addView(event);
						}else if(ScheduleContent.isConformScheduleFromDate(tmpCal.getTime())) {
							TextView event = new TextView(this);

							event.setBackgroundResource(R.drawable.event_existence);
							event.setHeight(columnHeight);

							frameLayout.addView(event);
						}
		
						weekRow.addView(frameLayout);
		
						if(dayCount > daysOfMonth){
							endOfMonth = true;
							dayCount = 1;
						}
					}else{
						TextView day = new TextView(this);
						day.setText(String.format("%s", dayCount++));
						day.setBackgroundColor(getResources().getColor(R.color.outside_background));
						day.setTextSize(outsideColumnSize);
						day.setHeight(columnHeight);
						day.setGravity(Gravity.CENTER_HORIZONTAL);
						day.setPadding(0, (int)(columnSize-outsideColumnSize), 0, 0);
		
						weekRow.addView(day);
					}

					tmpCal.roll(Calendar.DAY_OF_MONTH, true);
				}
				weekCount = 1;
	
				mainBoard.addView(weekRow);
			}
		} catch (Exception e) {
			Log.e(TAG, "[generateMonthView] ERROR:"+e.getMessage());
		}
	}

	private int getDaysOfMonth(int currentMonth) {
		Calendar cal = Calendar.getInstance();
		int nextMonth = currentMonth + 1;
		int retDays = -1;

		if(currentMonth > 11){
			currentMonth = 0;
		}else if(currentMonth < 0){
			currentMonth = 11;
		}

		if(currentMonth == 11){
			/*set days for December*/
			retDays = 31;
		}else{
			cal.set(Calendar.MONTH, nextMonth);
			retDays = cal.get(Calendar.DAY_OF_YEAR);
	
			cal.set(Calendar.MONTH, currentMonth);
			retDays -= cal.get(Calendar.DAY_OF_YEAR);
		}

		return retDays;
	}

	private void generateWeekLabel() {
		try {
			TableLayout mainBoard = (TableLayout) findViewById(R.id.ev_month_index_main_board);
			mainBoard.setStretchAllColumns(true);
	
			TableRow labelRow = new TableRow(this);
	
			for(int i=0; i<weekLabelJp.length; i++){
				TextView tv = new TextView(this);
				tv.setText(weekLabelJp[i]);
				tv.setTextSize(labelSize);
				tv.setHeight(labelHeight);
				tv.setBackgroundColor(Color.BLUE);
				tv.setGravity(Gravity.CENTER_HORIZONTAL);
		
				labelRow.addView(tv);
			}
			mainBoard.addView(labelRow);
		} catch (Exception e) {
			Log.d(TAG, "[generateWeekLabel] ERROR:"+e.getMessage());
		}
	}

	private void doMoveMonth(int direction) {
		TableLayout mainBoard = (TableLayout) findViewById(R.id.ev_month_index_main_board);
		int currentMonth = currentDate.get(Calendar.MONTH);
		int currentYear = currentDate.get(Calendar.YEAR);

		int length = contents.size();
		for(int i=0; i<length; i++){
			mainBoard.removeView(contents.get(i));
		}
		contents.clear();

		if(direction > 0) {
			if(++currentMonth > 11){
				currentYear++;
				currentMonth = 0;
			}
			Log.d(TAG, "[onClick] clicked prev button");
		} else {
			if(--currentMonth < 0){
				currentYear--;
				currentMonth = 11;
			}
			Log.d(TAG, "[onClick] clicked next button");
		}

		currentDate.set(Calendar.MONTH, currentMonth);
		currentDate.set(Calendar.YEAR, currentYear);

		constructScreen();
	}
}
