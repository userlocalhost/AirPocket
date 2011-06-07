package com.android.tmp07;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ImageView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.LayoutInflater;
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
	private static final int imageSize = 36;
	private static final float sizeTimeText = 11f;

	private LayoutInflater inflater;
	private int resourceId;
	private Context context;
	OnClickListener selectEvent;

	ScheduleContentAdapter(Context context, int resourceId, List<ScheduleContent> items, OnClickListener selectEvent) {
		super(context, resourceId, items);

		this.items = items;
		this.resourceId = resourceId;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.selectEvent = selectEvent;
		this.context = context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;

		if(row == null) {
			row = this.inflater.inflate(resourceId, null);
		}

		try {
			ScheduleContent doc = items.get(position);
			Date startTime = doc.getStartTime();
			Date endTime = doc.getEndTime();
			String timeline;

			if(doc.isStatus(ScheduleContent.Allday) && doc.isStatus(ScheduleContent.Multiday)) {
				timeline = String.format("[終日]%04d/%02d/%02d-%04d/%02d/%02d",
						startTime.getYear() + 1900, startTime.getMonth() + 1, startTime.getDate(),
						endTime.getYear() + 1900, endTime.getMonth() + 1, endTime.getDate());
			}else if(doc.isStatus(ScheduleContent.Multiday)) {
				timeline = String.format("%04d/%02d/%02d %02d:%02d - %04d/%02d/%02d %02d:%02d",
						startTime.getYear() + 1900, startTime.getMonth() + 1, startTime.getDate(), startTime.getHours(), startTime.getMinutes(),
						endTime.getYear() + 1900, endTime.getMonth() + 1, endTime.getDate(), endTime.getHours(), endTime.getMinutes());
			} else if(doc.isStatus(ScheduleContent.Allday)) {
				timeline = String.format("[終日]%04d/%02d/%02d",
						startTime.getYear() + 1900, startTime.getMonth() + 1, startTime.getDate());
			} else {
				timeline = String.format("%02d:%02d - %02d:%02d",
					startTime.getHours(), startTime.getMinutes(),
					endTime.getHours(), endTime.getMinutes());
			}

			/* set title and timeline context */
			TextView text = (TextView) row.findViewById(R.id.ev_list_row_label);
			text.setText(String.format("%s\n%s", doc.getSubject(), timeline));
			text.setTextSize(sizeTimeText);

			/* set image-label */
			String labelName = doc.getResourceLabel();
			if(labelName != null) {
				ImageView image = (ImageView) row.findViewById(R.id.ev_list_row_image);
				int resourceId = this.context.getResources().getIdentifier(labelName, "drawable", "com.android.tmp07");

				image.setImageResource(resourceId);
				image.setAdjustViewBounds(true);
				image.setMaxHeight(imageSize);
				image.setMaxWidth(imageSize);
			}

			row.setTag(doc);
			row.setOnClickListener(this.selectEvent);
		} catch(Exception e) {
			Log.e(TAG, "[getView] ERROR:"+e.getMessage());
		}

		return row;
	}
}
