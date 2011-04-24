package com.android.tmp07;

import android.util.Log;

import java.util.Date;
import java.util.LinkedList;
import java.util.ArrayList;

public class ScheduleContent
{
	private static final String TAG = "ScheduleContent";
	public static LinkedList<ScheduleContent> documents = new LinkedList<ScheduleContent>();

	private String subject;
	private String context;
	private Date startTime;
	private Date endTime;

	private int index;
	private int depth;

	ScheduleContent(String subject, String summary, Date start, Date end) {
		this.subject = subject;
		this.context = summary;
		this.startTime = start;
		this.endTime = end;
	}

	public boolean isSameDay(Date cmpDay) {
		boolean ret = false;

		if((this.startTime.getYear() == cmpDay.getYear()) &&
			(this.startTime.getMonth() == cmpDay.getMonth()) &&
			(this.startTime.getDate() == cmpDay.getDate())) {

			ret = true;
		}

		return ret;
	}

	public static ArrayList grepSchedule(Date date) {
		ArrayList ret = new ArrayList();
		int currentTime = (date.getHours() * 60) + date.getMinutes();

		for(int i=0; i<documents.size(); i++) {
			ScheduleContent doc = documents.get(i);
			int startTime = (doc.startTime.getHours() * 60) + doc.startTime.getMinutes();
			int endTime = (doc.endTime.getHours() * 60) + doc.endTime.getMinutes();

			if(doc.isSameDay(date) && (startTime <= currentTime) && (currentTime < endTime)) {
				ret.add(doc);
			}
		}

		return ret;
	}

	public static boolean isConformSchedule(Date date) {
		boolean ret = false;

		if(grepSchedule(date).size() > 0){
			ret = true;
		}

		return ret;
	}

	/* get/set methods */
	public void setDepth(int depth) {
		this.depth = depth;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public void setPosition(int depth, int index) {
		this.depth = depth;
		this.index = index;
	}

	public Date getStartTime() {
		return this.startTime;
	}

	public Date getEndTime() {
		return this.endTime;
	}

	public int getIndex() {
		return this.index;
	}

	public int getDepth() {
		return this.depth;
	}

	public String getSubject() {
		return this.subject;
	}

	public String getContext() {
		return this.context;
	}
}
