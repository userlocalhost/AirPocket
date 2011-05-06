package com.android.tmp07;

import android.widget.TableLayout;
import android.content.Context;
import android.view.View;
import android.util.AttributeSet;
import android.util.Log;

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
