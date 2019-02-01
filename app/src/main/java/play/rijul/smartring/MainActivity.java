package play.rijul.smartring;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends MainMenu {

	private AudioManager mode;
	private DBAdapter smartRingDB;
	private HashMap<Integer, Integer> settingsIcons = new HashMap<Integer, Integer>();
	private HashMap<Integer, Integer> toggles = new HashMap<Integer, Integer>();
//	private Context context;
	private final int DEFAULT_COLOR = 0xCCA0DED6;
	private final int LOUD_COLOR = 0xCC238276;
	private final int SILENT_COLOR = 0xCCCE0A31;
	private final int VIBRATE_COLOR = 0xCCF9D423;
	private Tools tools;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// mode = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		settingsIcons.put(R.id.settingsDay1, DaysOfWeek.DOW_MONDAY);
		settingsIcons.put(R.id.settingsDay2, DaysOfWeek.DOW_TUESDAY);
		settingsIcons.put(R.id.settingsDay3, DaysOfWeek.DOW_WEDNESDAY);
		settingsIcons.put(R.id.settingsDay4, DaysOfWeek.DOW_THURSDAY);
		settingsIcons.put(R.id.settingsDay5, DaysOfWeek.DOW_FRIDAY);
		settingsIcons.put(R.id.settingsDay6, DaysOfWeek.DOW_SATURDAY);
		settingsIcons.put(R.id.settingsDay7, DaysOfWeek.DOW_SUNDAY);

		toggles.put(R.id.enableDay1, DaysOfWeek.DOW_MONDAY);
		toggles.put(R.id.enableDay2, DaysOfWeek.DOW_TUESDAY);
		toggles.put(R.id.enableDay3, DaysOfWeek.DOW_WEDNESDAY);
		toggles.put(R.id.enableDay4, DaysOfWeek.DOW_THURSDAY);
		toggles.put(R.id.enableDay5, DaysOfWeek.DOW_FRIDAY);
		toggles.put(R.id.enableDay6, DaysOfWeek.DOW_SATURDAY);
		toggles.put(R.id.enableDay7, DaysOfWeek.DOW_SUNDAY);

		for (int i : settingsIcons.keySet()) {
			ImageView setting = (ImageView) findViewById(i);
			setting.setOnClickListener(settingsForDay);
		}
		tools = new Tools(getApplicationContext());
		smartRingDB = tools.openDB();
		
	}

	private void updateUI() {
		setStatus();
		setProfileDesc();
		setProfileDistrib();
		animate();
	}

	private void animate() {
		int[] left = { R.id.row1, R.id.row3, R.id.row5, R.id.row7 };
		int[] right = { R.id.row2, R.id.row4, R.id.row6 };
		Animation anim;
		View row;
		for (int i = 0; i < left.length; i++) {
			anim = AnimationUtils.makeInAnimation(this, true);
			row = (View) findViewById(left[i]);
			row.startAnimation(anim);
		}
		for (int i = 0; i < right.length; i++) {
			anim = AnimationUtils.makeInAnimation(this, false);
			row = (View) findViewById(right[i]);
			row.startAnimation(anim);
		}
	}

	private void setProfileDistrib() {
		int[] distribs = { R.id.distribDay7, R.id.distribDay1,
				R.id.distribDay2, R.id.distribDay3, R.id.distribDay4,
				R.id.distribDay5, R.id.distribDay6 };
		float fullDay = 1;
		try {
			fullDay = Tools.dateFormatter.parse("11:59 PM").getTime()
					- Tools.dateFormatter.parse("12:00 AM").getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		for (int dow = 0; dow < distribs.length; dow++) {
			LinearLayout l = (LinearLayout) findViewById(distribs[dow]);
			l.removeAllViews();
			l.setWeightSum(1.0f);

			float[] profileHours = getProfileDitribution(dow + 1);
			float sum = 0;
			for (int i = 0; i < profileHours.length; i++) {
				int color = DEFAULT_COLOR;
				switch (i) {
				case 0:
					color = LOUD_COLOR;
					break;
				case 1:
					color = VIBRATE_COLOR;
					break;
				case 2:
					color = SILENT_COLOR;
					break;
				}
				if (profileHours[i] != 0.0f) {
					float weight = profileHours[i] / fullDay;
					View v = makeWeightView(weight, color);
					l.addView(v);
				}
				sum += profileHours[i];
			}
			if (sum / fullDay < 1.0f) {
				View v = makeWeightView(1.0f - (sum / fullDay), DEFAULT_COLOR);
				l.addView(v);
			}
		}
	}

	private float[] getProfileDitribution(int dow) {
		float[] profileHours = new float[3];
		Cursor cursor = smartRingDB.getSettingsOfDay(dow);
		if (cursor != null && cursor.getCount() > 0) {
			for (int i = 0; i < cursor.getCount(); i++) {
				try {
					Date start = Tools.dateFormatter.parse(cursor
							.getString(DBAdapter.COL_STARTTIME));
					Date end = Tools.dateFormatter.parse(cursor
							.getString(DBAdapter.COL_ENDTIME));
					int profile = cursor.getInt(DBAdapter.COL_PROFILE);
					int index = 0;
					switch (profile) {
					case 0:
					case 4: {
						index = 0;
						break;
					}
					case 1: {
						index = 1;
						break;
					}
					case 2:
					case 3: {
						index = 2;
						break;
					}
					}
					profileHours[index] += (end.getTime() - start.getTime());
				} catch (ParseException e) {
					e.printStackTrace();
				}
				cursor.moveToNext();
			}
		}
		return profileHours;
	}

	private View makeWeightView(float weight, int background) {
		View v = new View(this);
		LayoutParams lp = new LayoutParams(0, LayoutParams.MATCH_PARENT, weight);
		v.setLayoutParams(lp);
		v.setBackgroundColor(background);
		return v;
	}

	private void setProfileDesc() {
		String profileName;
		int[] desc = { R.id.displayDay7, R.id.displayDay1, R.id.displayDay2,
				R.id.displayDay3, R.id.displayDay4, R.id.displayDay5,
				R.id.displayDay6 };
		for (int dow = 0; dow < 7; dow++) {
			profileName = "DEFAULT";
			Cursor cursor = smartRingDB.getSettingsOfDay(dow + 1);
			if (cursor != null && cursor.getCount() > 0) {
				for (int i = 0; i < cursor.getCount(); i++) {
					boolean flag = false;
					try {
						Date start = Tools.dateFormatter.parse(cursor
								.getString(DBAdapter.COL_STARTTIME));
						Date end = Tools.dateFormatter.parse(cursor
								.getString(DBAdapter.COL_ENDTIME));
						Date curr = Tools.dateFormatter
								.parse(Tools.dateFormatter.format(new Date()));
						if (curr.before(end) && curr.after(start)) {
							int profile = cursor.getInt(DBAdapter.COL_PROFILE);
							switch (profile) {
							case 0:
								profileName = "NORMAL";
								flag = true;
								break;
							case 1:
								profileName = "VIBRATE";
								flag = true;
								break;
							case 3:
								profileName = "SILENT";
								flag = true;
								break;
							}
							if (flag)
								break;
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}
			TextView description = (TextView) findViewById(desc[dow]);
			description.setText("Current Profile - " + profileName);
		}

	}

	private void setStatus() {
		for (int id : toggles.keySet()) {
			ToggleButton tb = (ToggleButton) findViewById(id);
			tb.setChecked(smartRingDB.checkStatus(toggles.get(id)));
		}
	}

	public void onToggleClicked(View view) {
		// Is the toggle on?
		boolean on = ((ToggleButton) view).isChecked();
		int dow = toggles.get(view.getId());
		Calendar cal = Calendar.getInstance();

		if (on) {
			smartRingDB.updateStatus(dow, 1);
			if (cal.get(Calendar.DAY_OF_WEEK) == dow) {
				Cursor cursor = smartRingDB.getSettingsOfDay(dow);
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					String time = cursor.getString(DBAdapter.COL_STARTTIME);
					tools.updateAlarmManager(time);
				}
			}
		} else {
			smartRingDB.updateStatus(dow, 0);
			String dummyTime = "12:00 AM";
			if (cal.get(Calendar.DAY_OF_WEEK) == dow) {
				tools.updateAlarmManager(dummyTime);
			}
		}

	}

	// private void populateList() {
	// // TODO Auto-generated method stub
	// String[] fromColumns = { DBAdapter.KEY_SETTINGNAME };
	// int[] toViews = { R.id.profile };
	//
	// Cursor cursor = smartRingDB.getAllSettings();
	//
	// if (cursor != null) {
	// SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
	// R.layout.profile_listitem_layout, cursor, fromColumns,
	// toViews, 0);
	// profileList.setAdapter(adapter);
	// }
	// }

	OnClickListener settingsForDay = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(MainActivity.this,
					SettingDetailsActivity.class);
			intent.putExtra("DOW", settingsIcons.get(v.getId()));
			startActivity(intent);
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		updateUI();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		tools.closeDB();
	}

	public OnClickListener change = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			mode.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);

		}
	};

}
