package com.android.tmp07;

import android.util.Log;

import java.util.Date;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class ScheduleContent implements Serializable
{
	public static final String SCHEDULE_CONTENT_DIR = "/sdcard/AirPocket/ScheduleContent/";
	/* Class member */
	public static LinkedList<ScheduleContent> documents = new LinkedList<ScheduleContent>();
	public static int Allday = (1<<0);
	public static int Multiday = (1<<1);

	private static final String TAG = "ScheduleContent";
	private ArrayList overlappedDocumentId = new ArrayList(); 

	private String id;
	private String subject;
	private String context;
	private String resourceLabel;
	private Date startTime;
	private Date endTime;
	private int status;

	private int index;
	private int depth;

	private LinkedList<String> attendee;
	
	/* This member used for Google Calendar Sync */
	private String editURL;
	
	ScheduleContent() {
		this.status = 0;
		this.id = UUID.randomUUID().toString();
		this.resourceLabel = null;
		this.attendee = null;
		this.editURL = null;
	}

	ScheduleContent(String subject, String summary, Date start, Date end) {
		this.subject = subject;
		this.context = summary;
		this.startTime = start;
		this.endTime = end;
		this.status = 0;
		this.id = UUID.randomUUID().toString();
		this.resourceLabel = null;
		this.attendee = null;
		this.editURL = null;
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

	public boolean isSameYear(Date cmpDay) {
		boolean ret = false;
		int startDays = (this.startTime.getYear() * 400);
		int endDays = (this.endTime.getYear() * 400);
		int cmpDays = (cmpDay.getYear() * 400);

		if((startDays == cmpDays) || (endDays == cmpDays) || ((startDays <= cmpDays) && (cmpDays <= endDays))) {
			ret = true;
		}

		return ret;
	}

	public boolean isSameMonth(Date cmpDay) {
		boolean ret = false;
		int startDays = (this.startTime.getYear() * 400) + (this.startTime.getMonth() * 31);
		int endDays = (this.endTime.getYear() * 400) + (this.endTime.getMonth() * 31);
		int cmpDays = (cmpDay.getYear() * 400) + (cmpDay.getMonth() * 31);

		if((startDays == cmpDays) || (endDays == cmpDays) || ((startDays <= cmpDays) && (cmpDays <= endDays))) {
			ret = true;
		}

		return ret;
	}

	public boolean isSameDay(Date cmpDay) {
		boolean ret = false;
		int startDays = (this.startTime.getYear() * 400) + (this.startTime.getMonth() * 31) + this.startTime.getDate();
		int endDays = (this.endTime.getYear() * 400) + (this.endTime.getMonth() * 31) + this.endTime.getDate();
		int cmpDays = (cmpDay.getYear() * 400) + (cmpDay.getMonth() * 31) + cmpDay.getDate();

		if((startDays == cmpDays) || (endDays == cmpDays) || ((startDays <= cmpDays) && (cmpDays <= endDays))) {
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

			if(doc.id.equals(this.id)) {
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

	public static ArrayList grepScheduleFromMonth(Date date) {
		ArrayList ret = new ArrayList();
		
		for(int i=0; i<documents.size(); i++) {
			ScheduleContent doc = documents.get(i);

			if(doc.isSameMonth(date)) {
				ret.add(doc);
			}
		}

		return ret;
	}
	
	public static List getAllDocuments() {
		return documents;
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

			if(doc.id.equals(uuidStr)) {
				ret = doc;
				break;
			}
		}

		return ret;
	}

	public static boolean resumeFromStorage() {
		boolean ret = false;
		File dir = new File(SCHEDULE_CONTENT_DIR);

		if(dir.exists()) {
			String[] files = dir.list();

			for(int i=0; i<files.length; i++) {
				String idPath = files[i];
				File file = new File(SCHEDULE_CONTENT_DIR + idPath);
					
				Log.d(TAG, "[resumeFromStorage] idPath : " + idPath);

				if(file.exists()) {
					Log.d(TAG, "[resumeFromStorage] idPath (exist): " + idPath);

					try {
						ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
						
						ScheduleContent doc = (ScheduleContent) ois.readObject();
						documents.add(doc);
	
						Log.d(TAG, "[resumeFromStorage] subject : " + doc.getSubject());
					} catch(java.io.IOException e) {
						Log.e(TAG, "[resumeFromStorage] " + e);
					} catch(java.lang.ClassNotFoundException e) {
						Log.e(TAG, "[resumeFromStorage] " + e);
					}
				} else {
					Log.e(TAG, "[resumeFromStorage] idPath doesn't exist : " + idPath);
				}
			}

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

	public void setEditURL(String url) {
		this.editURL = url;
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

	public void setId(String id) {
		this.id = id;
	}

	public void setAttendee(LinkedList<String> list) {
		this.attendee = list;
	}

	public void setStatus(int status) {
		this.status |= status;
	}

	public void setResourceLabel(String label) {
		this.resourceLabel = label;
	}

	public String getEditURL() {
		return this.editURL;
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

	public String getResourceLabel() {
		return this.resourceLabel;
	}

	public String getId() {
		return this.id;
	}

	public LinkedList<String> getAttendee() {
		return this.attendee;
	}

	public boolean isStatus(int status) {
		return ((this.status & status) > 0);
	}

	public void regist() {

		if(startTime == null || endTime == null || hasSameSchedule()) {
			return;
		}

		ArrayList indexList = new ArrayList();
		int regStartMinutes = (this.startTime.getHours() * 60) + this.startTime.getMinutes();
		int regEndMinutes = (this.endTime.getHours() * 60) + this.endTime.getMinutes();
		
		this.setPosition(0, 0);

		/* check duplicate docs */
		for(int i=0; i<ScheduleContent.documents.size(); i++){
			ScheduleContent doc = ScheduleContent.documents.get(i);
			int startMinutes = (doc.getStartTime().getHours() * 60) + doc.getStartTime().getMinutes();
			int endMinutes = (doc.getEndTime().getHours() * 60) + doc.getEndTime().getMinutes();

			if((! doc.isStatus(ScheduleContent.Allday) && 
				(doc.isJustSameDay(startTime) || doc.isJustSameDay(endTime)))
					&&
				(((regStartMinutes >= startMinutes) && (regStartMinutes < endMinutes)) ||
				((regEndMinutes > startMinutes) && (regEndMinutes < endMinutes)) ||
				((regStartMinutes < startMinutes) && (regEndMinutes > endMinutes)) ||
				(! checkSameDate(endTime, doc.getEndTime()) && checkSameDate(endTime, doc.getStartTime()) && (regEndMinutes > startMinutes)) ||
				(! checkSameDate(startTime, doc.getStartTime()) && checkSameDate(startTime, doc.getEndTime()) && (regStartMinutes > endMinutes)))
			){
				this.addOverlappedId(doc.getId());
				doc.addOverlappedId(this.getId());

				doc.setDepth(doc.getDepth() + 1);
				indexList.add(doc.getIndex());
			}
		}

		int targetIndex = -1;
		int findFlag;
		do{
			findFlag = 1;

			targetIndex++;

			Log.d(TAG, "[regist] targetIndex : "+targetIndex);
			for(int i=0; i<indexList.size(); i++){
				Log.d(TAG, String.format("[regist] %d:%d", i, indexList.get(i)));
				if(targetIndex == indexList.get(i)){
					findFlag = 0;
					break;
				}
			}
		} while(findFlag == 0);

		this.setPosition(indexList.size(), targetIndex);

		documents.add(this);

		boolean saveRet = saveToStorage();
		Log.d(TAG, "[regist] saveRet : " + saveRet);
	}

	/* private processing */
	private static boolean checkSameDate(Date a, Date b) {
		int aDate = (a.getYear() * 365) + (a.getMonth() * 31) + a.getDate();
		int bDate = (b.getYear() * 365) + (b.getMonth() * 31) + b.getDate();

		Log.d(TAG, String.format("[checkSameDate] aDate:%d, bDate:%d", aDate, bDate));

		return (aDate == bDate);
	}

	/*
	 * @return :
	 *	'false' means there is no document which is same of ditected ScheduleContent.
	 *	'true' is otherwise.
	 * */
	private boolean hasSameSchedule() {
		boolean ret = false;

		for(int i=0; i<documents.size(); i++) {
			ScheduleContent doc = documents.get(i);

			Log.d(TAG, String.format("[hasSameSchedule] check %s == %s", id, doc.getId()));

			if(doc.getId().equals(id)) {
				Log.d(TAG, String.format("[hasSameSchedule] check %s == %s", id, doc.getId()));
				ret = true;
				break;
			}
		}

		return ret;
	}

	/* save current object to file */
	private boolean saveToStorage() {
		File file = new File(SCHEDULE_CONTENT_DIR + this.id);
		boolean ret = true;

		if(! file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}

		try {
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
	
			oos.writeObject(this);
			oos.flush();
			oos.close();
		} catch(java.io.IOException e) {
			Log.e(TAG, "[saveToStorage] " + e);
			ret = false;
		}
		
		return ret;
	}
}
