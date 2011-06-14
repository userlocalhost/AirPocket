package com.ariel.android.airpocket;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.text.InputType;
import android.util.Log;

public class InputTwoColumns extends Activity {
	private static final String TAG = "InputTwoColumns";
  public static final String KEY_TITLE = "com.ariel.android.airpocket.inputtwocolumns.title";
  public static final String KEY_FIRST_LABEL = "com.ariel.android.airpocket.inputtwocolumns.first_label";
  public static final String KEY_SECOND_LABEL = "com.ariel.android.airpocket.inputtwocolumns.second_label";
  public static final String KEY_FIRST_COLUMN = "com.ariel.android.airpocket.inputtwocolumns.first_column";
  public static final String KEY_SECOND_COLUMN = "com.ariel.android.airpocket.inputtwocolumns.second_column";

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

	OnCheckedChangeListener exposeTrigger = new OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			TextView inputPasswd = (TextView) findViewById(R.id.second_input);

			if(isChecked) {
				inputPasswd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
			} else {
				inputPasswd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
			}
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.input_two_dialog);

		setLabels();

		CheckBox checkExpose = (CheckBox) findViewById(R.id.expose_passwd);
		checkExpose.setOnCheckedChangeListener(exposeTrigger);
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
