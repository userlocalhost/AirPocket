package com.android.tmp07;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.EditText;
import android.util.Log;

public class InputTwoColumns extends Activity {
	private static final String TAG = "InputTwoColumns";
  public static final String KEY_TITLE = "com.android.tmp07.inputtwocolumns.title";
  public static final String KEY_FIRST_LABEL = "com.android.tmp07.inputtwocolumns.first_label";
  public static final String KEY_SECOND_LABEL = "com.android.tmp07.inputtwocolumns.second_label";
  public static final String KEY_FIRST_COLUMN = "com.android.tmp07.inputtwocolumns.first_column";
  public static final String KEY_SECOND_COLUMN = "com.android.tmp07.inputtwocolumns.second_column";

	OnClickListener submitListener = new View.OnClickListener() {
		public void onClick(View v) {
			Intent i = new Intent();

			i.putExtra(KEY_FIRST_COLUMN, ((EditText) findViewById(R.id.first_input)).getText().toString());
			i.putExtra(KEY_SECOND_COLUMN, ((EditText) findViewById(R.id.second_input)).getText().toString());

			setResult(Activity.RESULT_OK, i);

			finish();
		}
	};

	OnClickListener cancelListener = new View.OnClickListener() {
		public void onClick(View v) {
			setResult(Activity.RESULT_CANCELED);
			finish();
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.input_two_dialog);

		setLabels();
	}

	private void setLabels() {
		String firstLabelStr = getIntent().getStringExtra(KEY_FIRST_LABEL);
		String secondLabelStr = getIntent().getStringExtra(KEY_SECOND_LABEL);
		String titleStr = getIntent().getStringExtra(KEY_TITLE);

		if(firstLabelStr != null) {
			TextView firstLabel = (TextView) findViewById(R.id.first_input_label);
			firstLabel.setText(firstLabelStr);
		}

		if(secondLabelStr != null) {
			TextView secondLabel = (TextView) findViewById(R.id.second_input_label);
			secondLabel.setText(secondLabelStr);
		}

		findViewById(R.id.submit).setOnClickListener(submitListener);
		findViewById(R.id.cancel).setOnClickListener(cancelListener);

		if(titleStr != null) {
			setTitle(titleStr);
		}
	}
}
