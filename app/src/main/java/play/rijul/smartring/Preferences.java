package play.rijul.smartring;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Preferences extends MainMenu {

	public static final String TEXT_DEFAULT = "This user currently has Smart Ring app activated, which is set to auto-reject incoming calls. Please try again later.";
	private DBAdapter smartRingPrefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.preferences);

		openDB();
		initiateUI();

		CheckBox cb = (CheckBox) findViewById(R.id.emergency);
		cb.setOnCheckedChangeListener(checkBoxChanged);
	}

	public void changeWhiteList(View view) {
		Intent intent = new Intent(this, WhiteListActivity.class);
		startActivity(intent);
	}

	OnCheckedChangeListener checkBoxChanged = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			smartRingPrefs.setEmergencyStatus(isChecked);
		}
	};

	private void initiateUI() {
		ToggleButton textOnCallReject = (ToggleButton) findViewById(R.id.text_reject);
		textOnCallReject.setChecked(smartRingPrefs.getRejectTextStatus());
		ToggleButton whiteListStatus = (ToggleButton) findViewById(R.id.whiteList);
		whiteListStatus.setChecked(smartRingPrefs.getWhiteListStatus());
		CheckBox cb = (CheckBox) findViewById(R.id.emergency);
		cb.setChecked(smartRingPrefs.getEmergencyStatus());
		Spinner prof = (Spinner) findViewById(R.id.nocallRingChoice);
		prof.setSelection(smartRingPrefs.getThroughProfile());
		prof.setOnItemSelectedListener(throughProfile);
	}
	
	OnItemSelectedListener throughProfile = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int pos,
				long arg3) {
			smartRingPrefs.setThroughProfile(pos);
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {}
	};

	public void textReject(View v) {
		AlertDialog.Builder alert = new AlertDialog.Builder(Preferences.this);

		alert.setTitle("Enter Auto-Reject Text");

		// Set an EditText view to get user input
		final EditText input = new EditText(Preferences.this);
		String message = smartRingPrefs.getRejectText();
		input.setHint(TEXT_DEFAULT);
		if (!message.equals(TEXT_DEFAULT))
			input.setText(message);

		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				if (value.length() > 0) {
					smartRingPrefs.setRejectText(value);
				} else {
					smartRingPrefs.setRejectText(TEXT_DEFAULT);
				}
			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});

		alert.show();
	}

	public void onTextReject(View view) {
		boolean on = ((ToggleButton) view).isChecked();
		if(on){
			shortToast("Warning: Standard SMS charges may apply as set by your network provider");
		}
		smartRingPrefs.setRejectTextStatus(on);
	}
	
	public void onWhiteList(View view){
		boolean on = ((ToggleButton) view).isChecked();
		smartRingPrefs.setWhiteListStatus(on);
	}

	public void emergencyCalls(View view) {
		CheckBox cb = (CheckBox) findViewById(R.id.emergency);
		smartRingPrefs.setEmergencyStatus(!cb.isChecked());
		if (cb.isChecked()) {
			cb.setChecked(false);
		} else {
			cb.setChecked(true);
		}
	}
	
	private void shortToast(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}

	private void openDB() {
		smartRingPrefs = new DBAdapter(this);
		smartRingPrefs.open();
	}

	private void closeDB() {
		smartRingPrefs.close();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		closeDB();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu);
		menu.removeItem(R.id.action_settings);
		return true;
	}

}
