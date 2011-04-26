package com.android.tmp07;

import android.util.Log;

import java.util.Date;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.UUID;

public class ScheduleContent
{
	private static final String TAG = "ScheduleContent";
	public static LinkedList<ScheduleContent> documents = new LinkedList<ScheduleContent>();
	private ArrayList overlappedDocumentId = new ArrayList(); 

	private UUID id;
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

		this.id = UUID.randomUUID();
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

	public void removeObj() {
		ArrayList overlappedDocs = this.getOverlappedDocsFromId();
		int i;

		for(i=0; i<overlappedDocs.size(); i++) {
			ScheduleContent doc = (ScheduleContent) overlappedDocs.get(i);

			if(doc != null) {
				Log.d(TAG, String.format("[removeObj] overlappedDocument.subject:%s[%s] (index:%d/%d) => %s[%s]",doc.getSubject(), doc.getId(), doc.getIndex(), this.index, this.subject, this.id.toString()));

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

		Log.d(TAG, String.format("[addOverlap] %s(%s) <= %s", this.subject, this.id.toString(), id));

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

			if(doc.isSameDay(date) && (startTime <= currentTime) && (currentTime < endTime)) {
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
}
