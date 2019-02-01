package play.rijul.smartring;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

public class ProfileReceiver extends BroadcastReceiver {

	// private AudioManager mode;
	private Calendar cal = Calendar.getInstance();
	private DBAdapter smartRingDB;
	private int dow;
	private Tools tools;

	// private Context context;
	// private NotificationManager mNotificationManager;
	// private int notifyID = 1;

	@Override
	public void onReceive(Context context, Intent arg1) {
		tools = new Tools(context);
		smartRingDB = tools.openDB();
		dow = cal.get(Calendar.DAY_OF_WEEK);
		boolean next = false;

		if (smartRingDB.checkStatus(dow)) {
			Date curr = new Date();
			String currTime = Tools.dateFormatter.format(curr);
			try {
				curr = Tools.dateFormatter.parse(currTime);
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			Cursor cursor = smartRingDB.getSettingsOfDay(dow);
			if (cursor != null && cursor.getCount() > 0) {
				for (int i = 0; i < cursor.getCount(); i++) {
					try {
						Date start = Tools.dateFormatter.parse(cursor
								.getString(DBAdapter.COL_STARTTIME));
						Date end = Tools.dateFormatter.parse(cursor
								.getString(DBAdapter.COL_ENDTIME));
						if (currTime.equals(cursor
								.getString(DBAdapter.COL_STARTTIME))
								|| (curr.after(start) && curr.before(end))) {
							tools.activateProfile(
									cursor.getInt(DBAdapter.COL_PROFILE),
									cursor.getString(DBAdapter.COL_ENDTIME));
							tools.updateAlarmManager(cursor
									.getString(DBAdapter.COL_ENDTIME));
							next = true;
							break;
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
					cursor.moveToNext();
				}
			}
			if (!next) {
				tools.deactivateProfile();

				cursor = smartRingDB.getSettingsOfDay(dow);
				if (cursor != null && cursor.getCount() > 0) {
					for (int i = 0; i < cursor.getCount(); i++) {
						String nextStart = cursor
								.getString(DBAdapter.COL_STARTTIME);
						try {
							Date d = Tools.dateFormatter.parse(nextStart);
							if (d.after(curr)) {
								tools.updateAlarmManager(nextStart);
								next = true;
								break;
							}
						} catch (ParseException e) {
							e.printStackTrace();
						}
						cursor.moveToNext();
					}
				}
			}
		}
		if (!next) {
			tools.deactivateProfile();
			tools.updateAlarmManager();
		}

		tools.closeDB();
	}

}
