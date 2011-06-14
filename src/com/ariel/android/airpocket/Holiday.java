package com.ariel.android.airpocket;

import android.util.Log;

import java.util.Date;
import java.util.Calendar;
import java.util.ArrayList;

public class Holiday
{
	class Condition {
		public int month;
		public int day;
		public int weekOfMonth;
		public int dayOfWeek;
	}

	/* for debug */
	private static final String TAG = "Holiday";

	/* holiday status */
	private static final int statusDay = (1<<0);
	private static final int statusCondition = (1<<1);
	private static final int statusDayOfSpring = (1<<2);
	private static final int statusDayOfFall = (1<<3);

	/* special case */
	public static final int DayOfSpring = (1<<0);
	public static final int DayOfFall = (1<<1);

	private static ArrayList<Holiday> holidayList = new ArrayList<Holiday>();

	/* holiday object's member */
	private Condition cond;
	private int status;
	private String label;

	Holiday(int status, String label) {
		Condition cond = new Condition();
		int inputStatus = 0;

		if((status & DayOfSpring) > 0) {
			cond.month = 2;
			inputStatus = statusDayOfSpring;
		} else if((status & DayOfFall) > 0) {
			cond.month = 8;
			inputStatus = statusDayOfFall;
		}

		if(inputStatus > 0) {
			this.status = inputStatus;
			this.cond = cond;
			this.label = label;

			Log.d(TAG, String.format("[Holiday] (%d) %s", inputStatus, label));
	
			holidayList.add(this);
		}
	}

	Holiday(int month, int day, String label) {
		Condition cond = new Condition();
		
		cond.month = month - 1;
		cond.day = day;

		this.cond = cond;
		this.status = statusDay;
		this.label = label;

		holidayList.add(this);
	}

	Holiday(int month, int weekOfMonth, int dayOfWeek, String label) {
		Condition cond = new Condition();

		cond.month = month - 1;
		cond.weekOfMonth = weekOfMonth;
		cond.dayOfWeek = dayOfWeek;

		this.cond = cond;
		this.status = statusCondition;
		this.label = label;

		holidayList.add(this);
	}
	
	public static boolean isHoliday(Date date) {
		boolean ret = false;

		if(getHoliday(date) != null) {
			ret = true;
		}

		return ret;
	}

	public static Holiday getHoliday(Date date) {
		Holiday ret = null;
		Calendar cal = Calendar.getInstance();
		
		for(int i=0; i<holidayList.size(); i++) {
			Holiday holiday = holidayList.get(i);

			if((holiday.cond.month == date.getMonth()) && 
				(
				(((holiday.status & statusDay) > 0) && 
				 (holiday.cond.day== date.getDate()))
					|| 
				(((holiday.status & statusCondition) > 0) &&
				 ((holiday.cond.weekOfMonth == (date.getDate()) / 7)) &&
				 (holiday.cond.dayOfWeek == (date.getDay())))
					||
				(((holiday.status & statusDayOfSpring) > 0) && isDayOfSpring(date))
					||
				(((holiday.status & statusDayOfFall) > 0) && isDayOfFall(date))
				))
			{
					ret = holiday;
					break;
			}
		}

		return ret;
	}

	public String getLabel() {
		return this.label;
	}

	private static boolean isDayOfSpring(Date date) {
		boolean ret = false;
		int year = date.getYear() + 1900;
		int day = date.getDate();

		switch(year % 4) {
			case 0 :
				if(((year >= 1900) && (year <= 1956) && (day == 21)) ||
					((year >= 1960) && (year <= 2088) && (day == 20)) ||
					((year >= 2092) && (year <= 2096) && (day == 19))) {
					ret = true;
				}
				break;
			case 1 :
				if(((year >= 1901) && (year <= 1989) && (day == 21)) ||
					((year >= 1993) && (year <= 2097) && (day == 20))) {
					ret = true;
				}
				break;
			case 2 :
				if(((year >= 1902) && (year <= 2022) && (day == 21)) ||
					((year >= 2026) && (year <= 2098) && (day == 20))) {
					ret = true;
				}
				break;
			case 3 :
				if(((year >= 1903) && (year <= 1923) && (day == 22)) ||
					((year >= 1927) && (year <= 2055) && (day == 21)) ||
					((year >= 2059) && (year <= 2099) && (day == 20))) {
					ret = true;
				}
				break;
		}

		return ret;
	}

	private static boolean isDayOfFall(Date date) {
		boolean ret = false;
		int year = date.getYear() + 1900;
		int day = date.getDate();

		switch(year % 4) {
			case 0 :
				if(((year >= 1900) && (year <= 2008) && (day == 23)) ||
					((year >= 2012) && (year <= 2096) && (day == 22))) {
					ret = true;
				}
				break;
			case 1 :
				if(((year >= 1901) && (year <= 1917) && (day == 24)) ||
					((year >= 1921) && (year <= 2041) && (day == 23)) ||
					((year >= 2045) && (year <= 2097) && (day == 22))) {
					ret = true;
				}
				break;
			case 2 :
				if(((year >= 1902) && (year <= 1946) && (day == 24)) ||
					((year >= 1950) && (year <= 2074) && (day == 23)) ||
					((year >= 2078) && (year <= 2098) && (day == 22))) {
					ret = true;
				}
				break;
			case 3 :
				if(((year >= 1903) && (year <= 1979) && (day == 24)) ||
					((year >= 1983) && (year <= 2099) && (day == 23))) {
					ret = true;
				}
				break;
		}

		return ret;
	}
}
