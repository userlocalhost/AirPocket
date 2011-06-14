package com.ariel.android.airpocket;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;

import android.view.View;
import android.view.View.MeasureSpec;
import android.view.MotionEvent;

import android.widget.TextView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import android.os.Bundle;
import android.os.Parcelable;

import android.util.AttributeSet;
import android.util.Log;

import java.util.LinkedList;
import java.util.Date;

public class EventView extends View {
	private static final String[] data = {"0:00", "1:00","2:00","3:00","4:00","5:00","6:00","7:00","8:00","9:00","10:00","11:00","12:00","13:00","14:00","15:00","16:00","17:00","18:00","19:00","20:00","21:00","22:00","23:00"};
	private static final String TAG = "EventView";

	private static final int timelineWidth = 40;
	private static final float timeRecoardSize = 15f;
	private static final float moveDateThreashold = 100f;
	private static int screenHeight = 960;

	private final EventIndexDay index;
	private final Rect selRect = new Rect();
	private static int startPoint;

	/* following member is used at onTouchEvent */
	private float motionX;
	private int motionStatus = 0;

	public EventView(Context context){
		super(context);

		this.index = (EventIndexDay) context;
	}

	public EventView(Context context, AttributeSet attrs){
		super(context, attrs);

		this.index = (EventIndexDay) context;
	}

	public EventView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);

		this.index = (EventIndexDay) context;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		/*
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			startPoint = (int)event.getY();

			select(startPoint, startPoint);
			Log.d(TAG, "[onTouchEvent] ACTION_DOWN");
		}else if(event.getAction() == MotionEvent.ACTION_MOVE){
			int currentPoint = (int)event.getY();

			if(startPoint > currentPoint){
				select(currentPoint, startPoint);
			}else{
				select(startPoint, currentPoint);
			}
			Log.d(TAG, "[onTouchEvent] ACTION_MOVE");
		}else if(event.getAction() == MotionEvent.ACTION_UP){
			int currentPoint = (int)event.getY();

			select(currentPoint, currentPoint);

			this.index.showEditActivity();

			Log.d(TAG, "[onTouchEvent] ACTION_UP");
		}
		*/

		if(event.getAction() == MotionEvent.ACTION_DOWN) {
			if(motionStatus != 0) {
				EventView.this.index.moveDate(motionStatus);
				
				motionStatus = 0;
			}
			motionX = event.getX();
		} else if(event.getAction() == MotionEvent.ACTION_MOVE) {
			if((event.getX() - motionX) > moveDateThreashold) {
				motionStatus = -1;
			} else if((event.getX() - motionX) < (moveDateThreashold * -1)) {
				motionStatus = 1;
			}
		} else if(event.getAction() == MotionEvent.ACTION_UP) {
			if(motionStatus == 0) {
				int currentPoint = (int)event.getY();
				int indexNum = (48 * currentPoint) / screenHeight;
	
				select(currentPoint, currentPoint);
	
				this.index.showEditActivity((indexNum / 2), (indexNum % 2) * 30);
			} else {
				EventView.this.index.moveDate(motionStatus);
				
				motionX = event.getX();
				motionStatus = 0;
			}
		}

		return true;
	}

	@Override
	protected void onMeasure(int widthSpec, int heightSpec){
		int width = MeasureSpec.getSize(widthSpec);

		setMeasuredDimension(width, this.screenHeight);
	}

	@Override
	protected void onDraw(Canvas canvas){
		initScreen(canvas);

		drawSelectedArea(canvas);

		drawDocuments(canvas, ScheduleContent.documents);
	}

	private void drawDocuments(Canvas canvas, LinkedList<ScheduleContent> docs)
	{
		for(int i=0; i<docs.size(); i++){
			ScheduleContent doc = docs.get(i);
			Paint docStyle = new Paint();
			Paint textStyle = new Paint();
			int paddingLeft = 0;

			Log.d(TAG, "[drawDocumens] date : "+EventIndexDay.currentDate.getTime().getDate());

			if(doc.isSameDay(EventIndexDay.currentDate.getTime()) &&
					doc.isJustSameDay(EventIndexDay.currentDate.getTime()) &&
					! doc.isStatus(ScheduleContent.Allday)) {
				int length = (getWidth() - paddingLeft);
				int prefix = timelineWidth;
				int depth = doc.getDepth();
				int indexNum = doc.getIndex();
				String labelName;
				BitmapDrawable icon = null;
				int imageSize = 32;
				int imagePadding = 5;
	
				if(depth > 0) {
					int tmpLength = (3 * length) / (2 * (depth + 1));

					prefix += ((length - tmpLength) / depth) * indexNum;
					length = prefix + tmpLength;
				}

				int startTotalMinutes = (doc.getStartTime().getHours() * 60) + doc.getStartTime().getMinutes();
				int endTotalMinutes = (doc.getEndTime().getHours() * 60) + doc.getEndTime().getMinutes();
	
				float docTop = ((screenHeight * startTotalMinutes) / 1440);
				float docBottom = ((screenHeight * endTotalMinutes) / 1440);

				if(! checkSameDate(EventIndexDay.currentDate.getTime(), doc.getStartTime())) {
					docTop = 0;
				}

				if(! checkSameDate(EventIndexDay.currentDate.getTime(), doc.getEndTime())) {
					docBottom = screenHeight;
				}

				docStyle.setStyle(Paint.Style.FILL);
				docStyle.setColor(getResources().getColor(R.color.document_background));
	
				canvas.drawRoundRect(new RectF(prefix, docTop, length, docBottom), 10, 10, docStyle);

				labelName = doc.getResourceLabel();
				if(labelName != null) {
					int resourceId = this.index.getResources().getIdentifier(labelName, "drawable", "com.ariel.android.airpocket");
					int imageX = prefix + imagePadding;
					int imageY = (int)docTop + 3;

					icon = (BitmapDrawable) this.index.getResources().getDrawable(resourceId);
					icon.setBounds(imageX, imageY, imageX + imageSize, imageY + imageSize);
					icon.draw(canvas);
				}

				float textPadding = 10f;
				float textX = prefix + textPadding + imageSize + imagePadding;
				float textY = docTop + 20f;
				float textSize = 14f;
				textStyle.setColor(getResources().getColor(R.color.normal_text));
				textStyle.setTextSize(textSize);
	
				canvas.drawText(doc.getSubject(), textX, textY, textStyle);
			}
		}
	}

	private void drawSelectedArea(Canvas canvas){
		Paint selectedArea = new Paint();

		selectedArea.setColor(getResources().getColor(R.color.selected_timeline));
		canvas.drawRect(selRect, selectedArea);
	}

	private void initScreen(Canvas canvas){
		Paint board = new Paint();
		Paint timeline = new Paint();
		Paint textstyle = new Paint();
		Paint linestyle = new Paint();
		int screenWidth = getWidth();
		int boardWidth = screenWidth - timelineWidth;
		float timeRightPadding = 3f;

		board.setColor(getResources().getColor(R.color.board_background));

		timeline.setColor(getResources().getColor(R.color.timeline_background));

		linestyle.setColor(getResources().getColor(R.color.timeline_background));

		textstyle.setTextAlign(Paint.Align.RIGHT);
		textstyle.setTextSize(timeRecoardSize);

		/* draw background for canvas */
		canvas.drawRect(0, 0, timelineWidth - 1, screenHeight, timeline);
		canvas.drawRect(timelineWidth, 0, boardWidth, screenHeight, board);

		canvas.drawLine(timelineWidth+1, 0, timelineWidth+1, screenHeight, timeline);

		//canvas.drawText("fuga", 0, textstyle.getTextSize(), textstyle);

		for(int i=0;i<data.length;i++){
			float hourPoint = ((float)i/data.length) * screenHeight;
			float halfPoint = hourPoint + (float)screenHeight/(data.length * 2);

			/* write timerecoard string*/
			canvas.drawText(data[i], timelineWidth - timeRightPadding, hourPoint + timeRecoardSize, textstyle);

			/* draw separate hour-line */
			canvas.drawLine(timelineWidth, hourPoint, screenWidth, hourPoint, linestyle);
			canvas.drawLine(timelineWidth, halfPoint, screenWidth, halfPoint, linestyle);

			//Log.d(TAG, i+", "+data[i]+", "+y);
		}
	}

	private void select(int top, int bottom){
		getRect(top, bottom, selRect);

		invalidate(selRect);
	}

	private void getRect(int top, int bottom, Rect rect){
		int topIndex = (48 * top) / screenHeight;
		int bottomIndex = (48 * bottom) / screenHeight;
		int partHeight = screenHeight / 48;

		Log.d(TAG, "[getRect] top:"+topIndex+", bottom:"+bottomIndex);

		rect.set(timelineWidth, 
				(topIndex * partHeight),
				getWidth(), 
				(bottomIndex * partHeight) + partHeight);
	}

	private static boolean checkSameDate(Date a, Date b) {
		int aDate = (a.getYear() * 365) + (a.getMonth() * 31) + a.getDate();
		int bDate = (b.getYear() * 365) + (b.getMonth() * 31) + b.getDate();

		return (aDate == bDate);
	}
}
