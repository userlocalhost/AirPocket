package com.android.tmp07;

import android.util.Log;

import java.util.Date;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.UUID;

public class ScheduleContent
{
	/* Class member */
	public static LinkedList<ScheduleContent> documents = new LinkedList<ScheduleContent>();
	public static int Allday = 1<<0;
	public static int Multiday = 1<<1;

	private static final String TAG = "ScheduleContent";
	private ArrayList overlappedDocumentId = new ArrayList(); 

	private UUID id;
	private String subject;
	private String context;
	private Date startTime;
	private Date endTime;
	private int status;

	private int index;
	private int depth;

	ScheduleContent(String subject, String summary, Date start, Date end) {
		this.subject = subject;
		this.context = summary;
		this.startTime = start;
		this.endTime = end;

		this.id = UUID.randomUUID();
	}

	/* This is a assistant method */
	public boolean isJustSameDay(Date cmpDay) {
		boolean ret = false;

		if(((this.startTime.getYear() == cmpDay.getYear()) &&
			(this.startTime.getMonth() == cmpDay.getMonth()) &&
			(this.startTime.getDate() == cmpDay.getDate())) ||
			((this.endTime.getYear() == cmpDay.getYear()) &&
			(this.endTime.getMonth() == cmpDay.getMonth()) &&
			(this.endTime.getDate() == cmpDay.getDate()))) {

			ret = true;
		}

		return ret;
	}

	public boolean isSameDay(Date cmpDay) {
		boolean ret = false;
		Date date = (Date) cmpDay.clone();
		Date startDate = (Date) startTime.clone();
		Date endDate = (Date) endTime.clone();

		/* reset date object */
		date.setMinutes(0);
		date.setHours(0);
		startDate.setMinutes(0);
		startDate.setHours(0);
		endDate.setMinutes(0);
		endDate.setHours(0);

		if((startDate.compareTo(date) == 0) ||
			(endDate.compareTo(date) == 0) ||
			((startDate.compareTo(date) < 0) && endDate.compareTo(date) > 0)) {

			ret = true;
		}

		return ret;
	}

	public void removeObj() {
		ArrayList overlappedDocs = this.getOverlappedDocsFromId();
		int i;

		for(i=0; i<overlappedDocs.size(); i++) {
			ScheduleContent doc = (ScheduleContent) overlappedDocs.get(i);

			if(doc != null) {
				doc.setDepth(doc.getDepth() - 1);
				if(doc.getIndex() > this.index) {
					doc.setIndex(doc.getIndex() - 1);
				}
			}
		}

		for(i=0; i<documents.size(); i++) {
			ScheduleContent doc = documents.get(i);

			if(doc.id.compareTo(this.id) == 0) {
				documents.remove(i);
			}
		}
	}

	public void addOverlappedId(String id) {
		ScheduleContent doc = getFromId(id);

		this.overlappedDocumentId.add(id);
	}

	public ArrayList getOverlappedDocsFromId() {
		ArrayList ret = new ArrayList();

		for(int i=0; i<this.overlappedDocumentId.size(); i++) {
			ScheduleContent doc = getFromId((String) this.overlappedDocumentId.get(i));
			if(doc != null) {
				ret.add(doc);
			}
		}

		return ret;
	}

	/*
	 * **************************** *
	 * followings are class method  *
	 * **************************** *
	 */
	public static ArrayList grepScheduleFromTime(Date date) {
		ArrayList ret = new ArrayList();
		int currentTime = (date.getHours() * 60) + date.getMinutes();

		for(int i=0; i<documents.size(); i++) {
			ScheduleContent doc = documents.get(i);
			int startTime = (doc.startTime.getHours() * 60) + doc.startTime.getMinutes();
			int endTime = (doc.endTime.getHours() * 60) + doc.endTime.getMinutes();

			if((doc.isSameDay(date) && (startTime <= currentTime) && (currentTime < endTime) &&
				! doc.isStatus(ScheduleContent.Allday) && doc.isJustSameDay(date)) ||
				(! checkSameDate(date, doc.endTime) && checkSameDate(date, doc.startTime) && (startTime <= currentTime)) ||
				(! checkSameDate(date, doc.startTime) && checkSameDate(date, doc.endTime) && (currentTime < endTime)))
			{
				ret.add(doc);
			}
		}

		return ret;
	}

	public static ArrayList grepScheduleFromDate(Date date) {
		ArrayList ret = new ArrayList();
		
		for(int i=0; i<documents.size(); i++) {
			ScheduleContent doc = documents.get(i);

			if(doc.isSameDay(date)) {
				ret.add(doc);
			}
		}

		return ret;
	}

	public static ArrayList grepAllday(Date date) {
		ArrayList ret = new ArrayList();
		
		for(int i=0; i<documents.size(); i++) {
			ScheduleContent doc = documents.get(i);

			if(doc.isSameDay(date) && 
				(doc.isStatus(ScheduleContent.Allday) || ! doc.isJustSameDay(date))) {

				ret.add(doc);
			}
		}

		return ret;

	}

	public static boolean isConformScheduleFromDate(Date date) {
		boolean ret = false;

		if(grepScheduleFromDate(date).size() > 0){
			ret = true;
		}

		return ret;
	}

	public static boolean isConformScheduleFromTime(Date date) {
		boolean ret = false;

		if(grepScheduleFromTime(date).size() > 0){
			ret = true;
		}

		return ret;
	}

	public static ScheduleContent getFromId(String uuidStr) {
		ScheduleContent ret = null;

		for(int i=0; i<documents.size(); i++) {
			ScheduleContent doc = documents.get(i);

			if(doc.id.toString().equals(uuidStr)) {
				ret = doc;
				break;
			}
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

	public void setStartTime(Date time) {
		this.startTime = time;
	}

	public void setEndTime(Date time) {
		this.endTime = time;
	}

	public void setSubject(String str) {
		this.subject = str;
	}

	public void setContext(String str) {
		this.context = str;
	}

	public void setStatus(int status) {
		this.status |= status;
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

	public String getId() {
		return this.id.toString();
	}

	public boolean isStatus(int status) {
		return ((this.status & status) > 0);
	}

	/* private processing */
	private static boolean checkSameDate(Date a, Date b) {
		int aDate = (a.getYear() * 365) + (a.getMonth() * 31) + a.getDate();
		int bDate = (b.getYear() * 365) + (b.getMonth() * 31) + b.getDate();

		Log.d(TAG, String.format("[checkSameDate] aDate:%d, bDate:%d", aDate, bDate));

		return (aDate == bDate);
	}
}
