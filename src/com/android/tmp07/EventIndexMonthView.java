package com.android.tmp07;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;

import android.view.View;
import android.view.View.MeasureSpec;
import android.view.MotionEvent;

import android.widget.TextView;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TableLayout;

import android.os.Bundle;
import android.os.Parcelable;

import android.util.AttributeSet;
import android.util.Log;

import java.util.LinkedList;
import java.util.Date;

public class EventIndexMonthView extends TableLayout {

	private static final String TAG = "EventIndexMonthView";
	private ArielEvent index;

	public EventIndexMonthView(Context context){
		super(context);

		this.index = (ArielEvent) context;
	}

	public EventIndexMonthView(Context context, AttributeSet attrs){
		super(context, attrs);

		this.index = (ArielEvent) context;
	}

	@Override
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		Log.d(TAG, "[onSizeChanged]");

		this.index.setFullscreen(w, h);
	}
}
