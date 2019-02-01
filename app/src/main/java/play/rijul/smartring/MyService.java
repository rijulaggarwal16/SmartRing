package play.rijul.smartring;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;

public class MyService extends Service {

	private DBAdapter smartRingDB;
	private Tools tools;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		tools = new Tools(getBaseContext());
		smartRingDB = tools.openDB();
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String action = intent.getAction();
		boolean next = false;
		Calendar cal = Calendar.getInstance();
		Date curr = new Date();
		String currTime = Tools.dateFormatter.format(curr);
		try {
			curr = Tools.dateFormatter.parse(currTime);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		if (action != null && action.equals("skip")) {
			Cursor cursor = smartRingDB.getSettingsOfDay(cal
					.get(Calendar.DAY_OF_WEEK));
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				do {
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
				} while (cursor.moveToNext());
			}
			if (!next) {
				tools.updateAlarmManager();
			}
		} else {
			smartRingDB.updateStatus(cal.get(Calendar.DAY_OF_WEEK), 0);
			String dummyTime = "12:00 AM";
			tools.updateAlarmManager(dummyTime);
		}
		tools.deactivateProfile();
		tools.closeDB();
		stopSelf();
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
