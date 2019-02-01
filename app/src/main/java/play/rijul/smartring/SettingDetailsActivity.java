package play.rijul.smartring;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

public class SettingDetailsActivity extends MainMenu {

	private DBAdapter smartRingDB;
    private int dow = 0;
	private LinkedHashSet<View> settingRows = new LinkedHashSet<View>();
//	private Context context;
	private Tools tools;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_details);
		this.tools = new Tools(getApplicationContext());
		smartRingDB = tools.openDB();

//		context = getApplicationContext();

        TextView cancel = (TextView) findViewById(R.id.cancel);
        TextView save = (TextView) findViewById(R.id.save);
		cancel.setOnClickListener(cancelSetting);
		save.setOnClickListener(saveSetting);

        TextView addSlot = (TextView) findViewById(R.id.addNewSlot);
		addSlot.setOnClickListener(addTimeSlot);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			dow = extras.getInt("DOW");
		}

		TextView dayName = (TextView) findViewById(R.id.dayName);
		dayName.setText(DaysOfWeek.getDayName(dow));

		Spinner same = (Spinner) findViewById(R.id.sameas);
		same.setOnItemSelectedListener(sameRows);
		same.setSelection(dow - 1);
	}

	OnItemSelectedListener sameRows = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int pos,
				long arg3) {
			displaySettingsOfDay(pos + 1);
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {}
	};

	private void displaySettingsOfDay(int dow) {
		for (View v : settingRows) {
			((LinearLayout) v.getParent()).removeView(v);
		}
		settingRows.removeAll(settingRows);
		Cursor cursor = smartRingDB.getSettingsOfDay(dow);

		if (cursor != null && cursor.getCount() > 0) {
			for (int i = 0; i < cursor.getCount(); i++) {
				View v = addSettingRow();
				Button start = (Button) v.findViewById(R.id.start);
				Button end = (Button) v.findViewById(R.id.end);
				Spinner prof = (Spinner) v.findViewById(R.id.action);

				start.setText(cursor.getString(DBAdapter.COL_STARTTIME));
				end.setText(cursor.getString(DBAdapter.COL_ENDTIME));
				prof.setSelection(cursor.getInt(DBAdapter.COL_PROFILE), true);
				cursor.moveToNext();
			}
		}
	}

	private void removeInfo() {
		View info = findViewById(R.id.info);
		if (info != null) {
			((RelativeLayout) info.getParent()).removeView(info);
		}
	}

	// private void displaySettingsOfDay() {
	// displaySettingsOfDay(this.dow);
	// }

	OnClickListener addTimeSlot = new OnClickListener() {

		@Override
		public void onClick(View v) {
			addSettingRow();

		}
	};

	private View addSettingRow() {
		removeInfo();
		// Parent layout
		LinearLayout parent = (LinearLayout) findViewById(R.id.schedule);
		// Layout inflater
		LayoutInflater layoutInflater = getLayoutInflater();
		View view;

		view = layoutInflater.inflate(R.layout.time_constraint_row, parent,
				false);

		settingRows.add(view);

		ImageView discard = (ImageView) view.findViewById(R.id.delete);
		discard.setOnClickListener(deleteAnim);
		Button start = (Button) view.findViewById(R.id.start);
		Button end = (Button) view.findViewById(R.id.end);
		start.setOnClickListener(startTime);
		end.setOnClickListener(endTime);
		parent.addView(view);
		Animation anim = AnimationUtils.makeInAnimation(this, true);
		view.startAnimation(anim);
		return view;
	}

	OnClickListener deleteAnim = new OnClickListener() {

		@Override
		public void onClick(View v) {
			Animation anim = AnimationUtils.makeOutAnimation(
					SettingDetailsActivity.this, false);
			View row = (View) v.getParent().getParent();
			row.startAnimation(anim);
			((LinearLayout) row.getParent()).removeView(row);
			settingRows.remove(row);
		}
	};

	OnClickListener endTime = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int hour, minute;
			final Button end = (Button) v.findViewById(R.id.end);
			String time = end.getText().toString();
			try {
				Calendar cal = Calendar.getInstance();
				cal.setTime(Tools.dateFormatter.parse(time));
				hour = cal.get(Calendar.HOUR_OF_DAY);
				minute = cal.get(Calendar.MINUTE);
			} catch (ParseException e) {
				hour = 0;
				minute = 0;
			}
            //Calendar calendar = Calendar.getInstance();
            final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(RadialPickerLayout radialPickerLayout, int selectedHour, int selectedMinute) {
                    end.setText(formatTime(selectedHour, selectedMinute));
                }
            }, hour ,minute, false, false);
            timePickerDialog.show(getSupportFragmentManager(), "Select End Time");
//			TimePickerDialog mTimePicker;
//			mTimePicker = new TimePickerDialog(SettingDetailsActivity.this,
//					new TimePickerDialog.OnTimeSetListener() {
//						@Override
//						public void onTimeSet(TimePicker timePicker,
//								int selectedHour, int selectedMinute) {
//							end.setText(formatTime(selectedHour, selectedMinute));
//						}
//					}, hour, minute, false);// Yes/No 24 hour time
//			mTimePicker.setTitle("Select End Time");
//			mTimePicker.show();
		}
	};

	OnClickListener startTime = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int hour, minute;
			final Button start = (Button) v.findViewById(R.id.start);
			String time = start.getText().toString();
			try {
				Calendar cal = Calendar.getInstance();
				cal.setTime(Tools.dateFormatter.parse(time));
				hour = cal.get(Calendar.HOUR_OF_DAY);
				minute = cal.get(Calendar.MINUTE);
			} catch (ParseException e) {
				hour = 0;
				minute = 0;
			}
            final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(RadialPickerLayout radialPickerLayout, int selectedHour, int selectedMinute) {
                    start.setText(formatTime(selectedHour, selectedMinute));
                }
            }, hour ,minute, false, false);
            timePickerDialog.show(getSupportFragmentManager(), "Select Start Time");
//			TimePickerDialog mTimePicker;
//			mTimePicker = new TimePickerDialog(SettingDetailsActivity.this,
//					new TimePickerDialog.OnTimeSetListener() {
//						@Override
//						public void onTimeSet(TimePicker timePicker,
//								int selectedHour, int selectedMinute) {
//							start.setText(formatTime(selectedHour,
//									selectedMinute));
//						}
//					}, hour, minute, false);// Yes 24 hour time
//			mTimePicker.setTitle("Select Start Time");
//			mTimePicker.show();

		}
	};

	OnClickListener cancelSetting = new OnClickListener() {

		@Override
		public void onClick(View v) {
			finish();
		}
	};

	OnClickListener saveSetting = new OnClickListener() {

		@Override
		public void onClick(View v) {
			smartRingDB.deleteDaySetting(dow);
			if (settingRows.size() != 0) {
				if (checkOverlap()) {
					return;
				}
				String[] starts = new String[settingRows.size()], ends = new String[settingRows
						.size()];
				int[] profiles = new int[settingRows.size()];

				int i = 0;
				for (View row : settingRows) {
					Button start = (Button) row.findViewById(R.id.start);
					Button end = (Button) row.findViewById(R.id.end);
					Spinner prof = (Spinner) row.findViewById(R.id.action);
					profiles[i] = prof.getSelectedItemPosition();
					starts[i] = start.getText().toString();
					ends[i] = end.getText().toString();
					i++;
				}
				smartRingDB.insertNewSetting(dow, starts, ends, profiles);

				Calendar cal = Calendar.getInstance();
				if (cal.get(Calendar.DAY_OF_WEEK) == dow) {
					List<String> list = Arrays.asList(starts);
					Collections.sort(list, new Comparator<String>() {
						SimpleDateFormat f = Tools.dateFormatter;

						@Override
						public int compare(String o1, String o2) {
							try {
								return f.parse(o1).compareTo(f.parse(o2));
							} catch (ParseException e) {
								throw new IllegalArgumentException(e);
							}
						}
					});
					tools.updateAlarmManager(list.get(0));
				}
			}
			shortToast("Successfully saved the settings");

			finish();
		}

	};

	private boolean checkOverlap() {
		LinkedHashSet<Date> startTimes = new LinkedHashSet<Date>();
		LinkedHashSet<Date> endTimes = new LinkedHashSet<Date>();
		SimpleDateFormat parser = Tools.dateFormatter;
		for (View v : settingRows) {
			Button start = (Button) v.findViewById(R.id.start);
			Button end = (Button) v.findViewById(R.id.end);
			try {
				startTimes.add(parser.parse(start.getText().toString()));
				endTimes.add(parser.parse(end.getText().toString()));
			} catch (ParseException e) {
				e.printStackTrace();
				shortToast("Error in saving");
				return true;
			}
		}
		if (startTimes.size() != endTimes.size()) {
			shortToast("2 or more time intervals overlap");
			return true;
		}

		Date[] st = new Date[startTimes.size()];
		Date[] et = new Date[endTimes.size()];

		startTimes.toArray(st);
		endTimes.toArray(et);

		for (int i = 0; i < startTimes.size(); i++) {
			for (int j = i; j < endTimes.size(); j++) {
				if (i == j) {
					if (st[i].after(et[j])) {
						shortToast("Start time should be before End time");
						return true;
					} else if (st[i].equals(et[j])) {
						shortToast("Start time and End time should be different");
						return true;
					} // else continue;
				} else if (st[i].before(et[j]) && st[j].before(et[i])) {
					shortToast("2 or more time intervals overlap");
					return true;
				}
			}
		}
		return false;
	}

	private void shortToast(String msg) {
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		tools.closeDB();
	}

	private CharSequence formatTime(int hour, int minutes) {
		String time = null;
		try {
			final SimpleDateFormat sdf = new SimpleDateFormat("H:mm", Locale.US);
			final Date dateObj = sdf.parse(hour + ":" + minutes);
			time = (Tools.dateFormatter.format(dateObj));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return time;
	}

}
