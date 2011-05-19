package com.android.tmp07;

import android.app.Activity;
import android.os.Bundle;

import android.widget.TextView;
import android.widget.LinearLayout;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.content.Intent;
import android.net.Uri;
import android.database.Cursor;
import android.provider.Contacts;

import java.util.ArrayList;
import android.util.Log;

public class ItemSelect extends Activity
{
	public static final String KEY_ITEMS = "com.android.tmp07.itemselect.inputs";
	public static final String KEY_TITLE = "com.android.tmp07.itemselect.title";
	public static final String KEY_SELECTED_ITEM = "com.android.tmp07.itemselect.selected_item";
	
	private static final String TAG = "ItemSelect";
	
	private static final int FP = ViewGroup.LayoutParams.FILL_PARENT;
	private static final int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
	private static final float textSize = 18f;

	OnClickListener selectItem = new View.OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent();

			intent.putExtra(KEY_SELECTED_ITEM, ((TextView) v).getText());
			setResult(Activity.RESULT_OK, intent);

			finish();
		}
	};
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_candidate);

		LinearLayout keyInputs = (LinearLayout) findViewById(R.id.key_inputs);
		ArrayList<String> inputs = getIntent().getStringArrayListExtra(KEY_ITEMS);
		int titleId = getIntent().getIntExtra(KEY_TITLE, 0);

		if(titleId > 0) {
			this.setTitle(titleId);
		}

		for(int i=0; i<inputs.size(); i++) {
			String input = inputs.get(i);
			TextView candidate = new TextView(this);

			candidate.setText(input);
			candidate.setTextSize(textSize);
			candidate.setOnClickListener(selectItem);
			candidate.setBackgroundResource(R.drawable.itemselect_row);

			keyInputs.addView(candidate, new ViewGroup.LayoutParams(FP, WC));
		}
	}
}
