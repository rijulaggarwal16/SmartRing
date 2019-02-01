package play.rijul.smartring;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.v4.app.NotificationCompat;

public class Tools {

	public static SimpleDateFormat dateFormatter = new SimpleDateFormat(
			"hh:mm aa", Locale.US);
	public static final int PROFILE_NORMAL = 0;
	public static final int PROFILE_VIBRATE = 1;
	public static final int PROFILE_SILENT = 2;
	public static final int PROFILE_NO_CALL_SILENT = 3;
	private NotificationManager mNotificationManager;
	private AudioManager mode;
	private Context context;
	private static final int notifyID = 1;
	private DBAdapter smartRingDB;

	public Tools(Context context) {
		this.context = context;
		mNotificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		smartRingDB = new DBAdapter(context);
	}

	// Update Alarm Manager to a specific time defined by a string of time in
	// format 'hh:mm aa'
	public void updateAlarmManager(String time) {
		Calendar cal = Calendar.getInstance();
		try {
			Date d = dateFormatter.parse(time);
			cal.setTime(d);
			Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
			c.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
			c.set(Calendar.SECOND, cal.get(Calendar.SECOND));
			updateAlarmManager(c);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Update Alarm Manager to 12 Am Next Day
	public void updateAlarmManager() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.add(Calendar.DAY_OF_MONTH, 1);
		updateAlarmManager(cal);
	}

	// Update Alarm Manager to specific time, defined in Calendar object
	private void updateAlarmManager(Calendar cal) {
		AlarmManager alarmMgr;
		PendingIntent alarmIntent;
		alarmMgr = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, ProfileReceiver.class);
		alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		alarmMgr.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
				alarmIntent);
	}

	public void deactivateProfile() {
		mode = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		mode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		mNotificationManager.cancel(notifyID);
	}

	public void activateProfile(int profile, String until) {
		mode = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		String notifyMessage = "";
		switch (profile) {
		case Tools.PROFILE_NORMAL:
			mode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
			notifyMessage = "Current mode - NORMAL, until " + until;
			break;
		case Tools.PROFILE_VIBRATE:
			mode.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
			notifyMessage = "Current mode - VIBRATE, until " + until;
			break;
		case Tools.PROFILE_SILENT:
			mode.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			notifyMessage = "Current mode - SILENT, until " + until;
			break;
		case Tools.PROFILE_NO_CALL_SILENT:
			mode.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			notifyMessage = "Current mode - NO CALL SILENT, until " + until;
			break;

		}
		issueNotification(notifyMessage);
	}

	@SuppressLint("InlinedApi")
	private void issueNotification(String notifyMessage) {
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				context).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("Smart Ring").setContentText(notifyMessage);
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(context, MainActivity.class);
		Intent skipIntent = new Intent(context, MyService.class);
		Intent disableIntent = new Intent(context, MyService.class);
		skipIntent.setAction("skip");
		disableIntent.setAction("disable");
		PendingIntent skipPendingIntent = PendingIntent.getService(context,
				notifyID, skipIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent disablePendingIntent = PendingIntent.getService(context,
				notifyID, disableIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent resultPendingIntent = PendingIntent.getActivity(context,
				notifyID, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		mBuilder.addAction(0, "Skip", skipPendingIntent);
		mBuilder.addAction(0, "Disable", disablePendingIntent);
		mBuilder.setPriority(Notification.PRIORITY_MAX);
		mBuilder.setWhen(0);
		mBuilder.setDefaults(Notification.DEFAULT_SOUND);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(notifyID, mBuilder.build());
	}

	public DBAdapter openDB() {
		smartRingDB.open();
		return smartRingDB;
	}

	public void closeDB() {
		smartRingDB.close();
	}

}
