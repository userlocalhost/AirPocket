package com.ariel.android.airpocket;

import android.app.Activity;
import android.app.ListActivity;
import android.os.Bundle;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ArrayAdapter;

import android.view.LayoutInflater;
import android.content.Intent;

import android.util.Log;

import java.lang.Exception;

public class EditLabel extends Activity
{
	private static final String TAG = "EditLabel";
	private static final String items[] = {
		"barger", "bell01", "binocle", "cart", "clip", "coffie", "edit", "film", "fire", "flower", "fun1", "hart01", "hart02", "hart03", "shoe", "star", "star02"
	};

	private ScheduleContent doc;

	OnClickListener itemSelected = new View.OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent();
			String resourceId = (String) v.getTag();

			intent.putExtra(EditEvent.KEY_LABEL_RESOURCEID, resourceId);
			setResult(Activity.RESULT_OK, intent);

			finish();
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.event_edit_label);

		GridView screen = (GridView) findViewById(R.id.screen);
		screen.setAdapter(new LabelAdapter());
	}

	class LabelAdapter extends ArrayAdapter {
		LabelAdapter() {
			super(EditLabel.this, R.layout.event_label_row, items);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;

			if(row == null) {
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.event_label_row, null);
			}

			try {
				ImageView image = (ImageView) row.findViewById(R.id.image);
				int resourceId = EditLabel.this.getResources().getIdentifier(
						items[position], "drawable", "com.ariel.android.airpocket");
	
				image.setImageResource(resourceId);
				image.setAdjustViewBounds(true);
				image.setMaxWidth(80);
				image.setMaxHeight(80);

				row.setTag(items[position]);
				row.setOnClickListener(itemSelected);
			} catch(Exception e) {
				Log.e(TAG, "[getView] ERROR:"+e.getMessage());
			}

			return row;
		}
	}
}
