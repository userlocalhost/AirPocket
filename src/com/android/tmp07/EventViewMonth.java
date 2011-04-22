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

import android.widget.ListView;
import android.widget.ArrayAdapter;

import android.os.Bundle;
import android.os.Parcelable;

import android.util.AttributeSet;
import android.util.Log;

import java.util.LinkedList;

public class EventViewMonth extends View {
	private static final String TAG = "EventViewMonth";
	private final TmpActivity07 index;

	public EventViewMonth(Context context){
		super(context);

		this.index = (TmpActivity07) context;
	}

	public EventViewMonth(Context context, AttributeSet attrs){
		super(context, attrs);

		this.index = (TmpActivity07) context;
	}

	public EventViewMonth(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);

		this.index = (TmpActivity07) context;
	}
}
