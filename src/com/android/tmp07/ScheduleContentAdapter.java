package com.android.tmp07;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;

import java.util.Date;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.lang.Exception;

public class ScheduleContentAdapter extends ArrayAdapter<ScheduleContent>
{
	private static final String TAG = "ScheduleContentAdapter";
	private List<ScheduleContent> items;

	ScheduleContentAdapter(Context context, int resourceId, List<ScheduleContent> items) {
		super(context, resourceId, items);

		this.items = items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;

		try {
			ScheduleContent doc = items.get(position);
			Date startTime = doc.getStartTime();
			Date endTime = doc.getStartTime();
				
			String timeline = String.format("%02d/%02d - %02d/%02d", 
					startTime.getHours(), startTime.getMinutes(),
					endTime.getHours(), endTime.getMinutes());
	
			TextView text = (TextView) convertView.findViewById(R.id.ev_list_row_label);
			text.setText(String.format("> %s\n%s", doc.getSubject(), timeline));
		} catch(Exception e) {
			Log.e(TAG, "[getView] ERROR:"+e.getMessage());
		}

		return view;
	}
}
