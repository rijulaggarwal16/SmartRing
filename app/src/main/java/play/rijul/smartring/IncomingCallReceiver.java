package play.rijul.smartring;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.ITelephony;

public class IncomingCallReceiver extends BroadcastReceiver {

	private ITelephony telephonyService;
	private DBAdapter smartRingDB;
	private Context context;
	private static final int MILLIS_TO_MINUTES = 60000;
	private String phoneNumber;
	private AudioManager mode;
	private int dow;
	private NotificationManager mNotificationManager;
	private int notifyID = 2;
	private static final String APP_SIGNATURE = "This user is currently using Smart Ring app on android";

	@Override
	public void onReceive(Context context, Intent intent) {
		mode = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		this.context = context;
		openDB();
		Bundle bundle = intent.getExtras();
		if (null == bundle) {
			return;
		}

		String state = bundle.getString(TelephonyManager.EXTRA_STATE);
		phoneNumber = bundle.getString("incoming_number");
		Calendar curr = Calendar.getInstance();
		dow = curr.get(Calendar.DAY_OF_WEEK);
		if (!(smartRingDB.checkStatus(dow) && noCallActive())) {
			return;
		}

		if (TelephonyManager.EXTRA_STATE_RINGING.equalsIgnoreCase(state)) {
			if (smartRingDB.getWhiteListStatus()
					&& smartRingDB.checkNumberInWhiteList(phoneNumber)) {
				changeProfile();
				return;
			}

			Cursor cursor = smartRingDB.getCallRecord();
			if (cursor != null && cursor.getCount() > 0) {
				for (int i = 0; i < cursor.getCount(); i++) {
					long prev = cursor.getLong(DBAdapter.COL_TIMESTAMP);
					if ((curr.getTimeInMillis() - prev) / MILLIS_TO_MINUTES >= 15) {
						smartRingDB.deleteCallecord(cursor
								.getString(DBAdapter.COL_NUM));
					}
					cursor.moveToNext();
				}
			}
			if (smartRingDB.getEmergencyStatus()) {
				if (!smartRingDB.checkCallRecord(phoneNumber)) {

					smartRingDB.insertCallRecord(phoneNumber,
							curr.getTimeInMillis());
					endCall();
					return;
				} else {
					cursor = smartRingDB.getCallRecord(phoneNumber);
					if (cursor != null && cursor.getCount() > 0) {
						long prev = cursor.getLong(DBAdapter.COL_TIMESTAMP);
						int count = cursor.getInt(DBAdapter.COL_COUNT);
						switch (count) {
						case 1:
							if ((curr.getTimeInMillis() - prev)
									/ MILLIS_TO_MINUTES < 15) {
								smartRingDB.setCallRecordCount(count + 1,
										phoneNumber);
								endCall();
								return;
							}
							break;

						case 2:
							smartRingDB.deleteCallecord(phoneNumber);
							if ((curr.getTimeInMillis() - prev)
									/ MILLIS_TO_MINUTES < 15) {
								changeProfile();
								return;
							}
							break;
						}
					}
				}
			}
			endCall();

		} else if (TelephonyManager.EXTRA_STATE_IDLE.equalsIgnoreCase(state)) {
			if (smartRingDB.checkStatus(dow) && noCallActive()) {
				mode.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			}
			return;
		} else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equalsIgnoreCase(state)) {
			if (smartRingDB.checkStatus(dow) && noCallActive()) {
				mode.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			}
			return;
		}
	}

	private void changeProfile() {
		if (smartRingDB.getThroughProfile() == Tools.PROFILE_NORMAL) {
			mode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		} else if (smartRingDB.getThroughProfile() == Tools.PROFILE_VIBRATE) {
			mode.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
		} else if (smartRingDB.getThroughProfile() == Tools.PROFILE_SILENT) {
			mode.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		}
	}

	private boolean noCallActive() {
		Date curr = new Date();
		String currTime = Tools.dateFormatter.format(curr);
		try {
			curr = Tools.dateFormatter.parse(currTime);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		Cursor c = smartRingDB.getSettingsOfDay(dow);
		if (c != null && c.getCount() > 0) {
			for (int i = 0; i < c.getCount(); i++) {
				try {
					Date start = Tools.dateFormatter.parse(c
							.getString(DBAdapter.COL_STARTTIME));
					Date end = Tools.dateFormatter.parse(c
							.getString(DBAdapter.COL_ENDTIME));
					if ((curr.after(start) || curr.equals(start))
							&& curr.before(end)) {
						if (c.getInt(DBAdapter.COL_PROFILE) == Tools.PROFILE_NO_CALL_SILENT) {
							return true;
						} else
							return false;
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
				c.moveToNext();
			}
		}
		return false;
	}

	private void endCall() {
		mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		try {
			Class<?> c = Class.forName(tm.getClass().getName());
			Method m = c.getDeclaredMethod("getITelephony");
			m.setAccessible(true);
			telephonyService = (ITelephony) m.invoke(tm);

			// telephonyService.silenceRinger();
			Class<?> telephonyInterfaceClass = Class.forName(telephonyService
					.getClass().getName());
			Method methodEndCall = telephonyInterfaceClass
					.getDeclaredMethod("endCall");
			methodEndCall.invoke(telephonyService);
			// telephonyService.endCall();
			if (phoneNumber != null)
				sendText(phoneNumber);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String notifyMessage = "Missed Call. Check Call Log";
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("Smart Ring").setContentText(notifyMessage);
		// Creates an explicit intent for an Activity in your app
//		Intent resultIntent = new Intent(context, MainActivity.class);
		Intent showCallLog = new Intent();
		showCallLog.setAction(Intent.ACTION_VIEW);
		showCallLog.setType(CallLog.Calls.CONTENT_TYPE);

		PendingIntent resultPendingIntent = PendingIntent.getActivity(context,
				notifyID, showCallLog, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		mBuilder.setAutoCancel(true);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(notifyID, mBuilder.build());
		
		closeDB();
	}

	private void sendText(String number) {
		if (smartRingDB.getRejectTextStatus()) {
			String text = smartRingDB.getRejectText() + "\n\n" + APP_SIGNATURE;
			try {
				SmsManager sms = SmsManager.getDefault(); // using android
															// SmsManager
				sms.sendTextMessage(number, null, text,
						null, null); // adding number and text
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void openDB() {
		smartRingDB = new DBAdapter(context);
		smartRingDB.open();
	}

	private void closeDB() {
		smartRingDB.close();
	}

}
