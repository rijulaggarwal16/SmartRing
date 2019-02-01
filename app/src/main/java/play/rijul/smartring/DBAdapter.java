package play.rijul.smartring;

//------------------------------------ DBADapter.java ---------------------------------------------

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * 
 * @author Rijul Class for database operations. All methods defined to directly
 *         interact with the database on various tables.
 */
public class DBAdapter {

	// ///////////////////////////////////////////////////////////////////
	// Constants & Data
	// ///////////////////////////////////////////////////////////////////
	// For logging:
	private static final String TAG = "DBAdapter";

	// DB Fields
	// Main Profile Settings Table
	public static final String KEY_ID = "_id";
	public static final String KEY_DOW = "dow";
	public static final String KEY_STARTTIME = "starttime";
	public static final String KEY_ENDTIME = "endtime";
	public static final String KEY_PROFILE = "profile";
	public static final String KEY_STATUS = "status";
	// Field numbers
	public static final int COL_ID = 0;
	public static final int COL_DOW = 1;
	public static final int COL_STARTTIME = 2;
	public static final int COL_ENDTIME = 3;
	public static final int COL_PROFILE = 4;
	public static final int COL_STATUS = 5;

	// Preferences Table
	public static final String KEY_ID2 = "_id";
	public static final String KEY_WHITELIST = "listactive";
	public static final String KEY_EMERGENCY = "emergency";
	public static final String KEY_TEXTREJECT = "textreject";
	public static final String KEY_TEXT = "text";
	public static final String KEY_THROUGHCALLPROFILE = "profile";
	// Preferences Field Numbers
	public static final int COL_ID2 = 0;
	public static final int COL_WHITELIST = 1;
	public static final int COL_EMERGENCY = 2;
	public static final int COL_TEXTREJECT = 3;
	public static final int COL_TEXT = 4;
	public static final int COL_THROUGHCALLPROFILE = 5;

	// White List Table
	public static final String KEY_ID3 = "_id";
	public static final String KEY_NAME = "name";
	public static final String KEY_NUMBER = "number";
	// White List Field Numbers
	public static final int COL_ID3 = 0;
	public static final int COL_NAME = 1;
	public static final int COL_NUMBER = 2;

	// Call Records Table
	public static final String KEY_ID4 = "_id";
	public static final String KEY_NUM = "number";
	public static final String KEY_COUNT = "count";
	public static final String KEY_TIMESTAMP = "time";
	// Call Records Field Numbers
	public static final int COL_ID4 = 0;
	public static final int COL_NUM = 1;
	public static final int COL_COUNT = 2;
	public static final int COL_TIMESTAMP = 3;

	public static final String[] ALL_SETTINGS = new String[] { KEY_ID, KEY_DOW,
			KEY_STARTTIME, KEY_ENDTIME, KEY_PROFILE, KEY_STATUS };
	public static final String[] ALL_PREFERENCES = new String[] { KEY_ID2,
			KEY_WHITELIST, KEY_EMERGENCY, KEY_TEXTREJECT, KEY_TEXT,
			KEY_THROUGHCALLPROFILE };
	public static final String[] ALL_WHITELIST = new String[] { KEY_ID3,
			KEY_NAME, KEY_NUMBER };
	public static final String[] ALL_CALLRECORD = new String[] { KEY_ID4,
			KEY_NUM, KEY_COUNT, KEY_TIMESTAMP };

	// DB info
	public static final String DATABASE_NAME = "SmartRing";
	public static final String TABLE_SETTINGS = "settings";
	public static final String TABLE_PREFERENCES = "preferences";
	public static final String TABLE_WHITELIST = "whitelist";
	public static final String TABLE_CALLRECORD = "callrecord";
	// Track DB version if a new version of your app changes the format.
	public static final int DATABASE_VERSION = 9;

	// Create statement for Profile Settings table
	private static final String TABLE_SETTINGS_CREATE_SQL = "create table "
			+ TABLE_SETTINGS + " (" + KEY_ID
			+ " integer primary key autoincrement, " + KEY_DOW
			+ " integer not null, " + KEY_STARTTIME + " varchar(8) not null, "
			+ KEY_ENDTIME + " varchar(8) not null, " + KEY_PROFILE
			+ " integer not null, " + KEY_STATUS + " integer not null"
			// Rest of creation:
			+ ");";

	// Create statement for app preferences table
	private static final String TABLE_PREFERENCES_CREATE_SQL = "create table "
			+ TABLE_PREFERENCES + " (" + KEY_ID2
			+ " integer primary key autoincrement, " + KEY_WHITELIST
			+ " integer not null, " + KEY_EMERGENCY + " integer not null, "
			+ KEY_TEXTREJECT + " integer not null, " + KEY_TEXT
			+ " text not null, " + KEY_THROUGHCALLPROFILE + " integer not null"
			// Rest of creation:
			+ ");";

	// Create statement for white list storing table
	private static final String TABLE_WHITELIST_CREATE_SQL = "create table "
			+ TABLE_WHITELIST + " (" + KEY_ID3
			+ " integer primary key autoincrement, " + KEY_NAME
			+ " varchar(50) not null, " + KEY_NUMBER + " varchar(20) not null"
			// Rest of creation:
			+ ");";

	// Create statement for call record table used for emergency call
	// determination
	private static final String TABLE_CALLRECORD_CREATE_SQL = "create table "
			+ TABLE_CALLRECORD + " (" + KEY_ID4
			+ " integer primary key autoincrement, " + KEY_NUM
			+ " varchar(20) not null, " + KEY_COUNT + " integer not null, "
			+ KEY_TIMESTAMP + " long not null"
			// Rest of creation:
			+ ");";
	// Context of application who uses this.
	private final Context context;

	private DatabaseHelper myDBHelper;
	private SQLiteDatabase db;

	// ///////////////////////////////////////////////////////////////////
	// Public methods:
	// ///////////////////////////////////////////////////////////////////

	/**
	 * Constructor
	 * 
	 * @param ctx
	 *            Context of the calling application
	 */
	public DBAdapter(Context ctx) {
		this.context = ctx;
		myDBHelper = new DatabaseHelper(context);
	}

	/**
	 * Open the database connection.
	 * 
	 * @return DBAdapter object with database open
	 */
	public DBAdapter open() {
		db = myDBHelper.getWritableDatabase();
		return this;
	}

	/**
	 * Close the database connection.
	 */
	public void close() {
		myDBHelper.close();
	}

	// Initial values for the preferences table
	private static void initializePreferences(SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		values.put(KEY_WHITELIST, 0);
		values.put(KEY_EMERGENCY, 0);
		values.put(KEY_TEXTREJECT, 0);
		values.put(KEY_TEXT, Preferences.TEXT_DEFAULT);
		values.put(KEY_THROUGHCALLPROFILE, 0);

		db.insert(TABLE_PREFERENCES, null, values);
	}

	/**
	 * Check the record of a phone number for existence
	 * 
	 * @param phoneNumber
	 *            The phone number to check for
	 * @return true if previous record exists, false otherwise
	 */
	public boolean checkCallRecord(String phoneNumber) {
		String where = KEY_NUM + "='" + phoneNumber + "'";
		Cursor c = db.query(true, TABLE_CALLRECORD, ALL_CALLRECORD, where,
				null, null, null, KEY_ID4, null);
		if (c != null && c.getCount() > 0) {
			return true;
		}
		return false;
	}

	/**
	 * Store record for a phone number
	 * 
	 * @param phoneNumber
	 *            The phone number of the call to be recorded
	 * @param time
	 *            Time of record
	 * @return false if storing fails, true otherwise
	 */
	public boolean insertCallRecord(String phoneNumber, long time) {
		ContentValues values = new ContentValues();
		values.put(KEY_NUM, phoneNumber);
		values.put(KEY_COUNT, 1);
		values.put(KEY_TIMESTAMP, time);

		if (db.insert(TABLE_CALLRECORD, null, values) < 0)
			return false;
		return true;
	}

	/**
	 * Get all call record for a phone number
	 * 
	 * @param phoneNumber
	 *            Number to get the record for
	 * @return Cursor object containing the record, null if record doesn't exist
	 */
	public Cursor getCallRecord(String phoneNumber) {
		String where = KEY_NUM + "='" + phoneNumber + "'";
		Cursor c = db.query(true, TABLE_CALLRECORD, ALL_CALLRECORD, where,
				null, null, null, KEY_ID4, "1");
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	/**
	 * Get all call records stored
	 * 
	 * @return Cursor object containing the records, null if none exist
	 */
	public Cursor getCallRecord() {
		Cursor c = db.query(true, TABLE_CALLRECORD, ALL_CALLRECORD, null, null,
				null, null, KEY_ID4, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	/**
	 * Set the count frequency for a particular phone number
	 * 
	 * @param count
	 *            The count frequency to update
	 * @param phoneNumber
	 *            Phone number of the record already stored
	 * @return true if update of call count is successful, false otherwise
	 */
	public boolean setCallRecordCount(int count, String phoneNumber) {
		String where = KEY_NUM + "='" + phoneNumber + "'";
		ContentValues values = new ContentValues();
		values.put(KEY_COUNT, count);
		return db.update(TABLE_CALLRECORD, values, where, null) != 0;
	}

	/**
	 * Delete all call record of a phone number
	 * 
	 * @param phoneNumber
	 *            Number to delete the record for
	 * @return true if deletion successful, false otherwise
	 */
	public boolean deleteCallecord(String phoneNumber) {
		String where = KEY_NUM + "='" + phoneNumber + "'";
		return db.delete(TABLE_CALLRECORD, where, null) != 0;
	}

	/**
	 * Get the message of SMS on auto call reject
	 * @return Stored text message, null if an error occurred
	 */
	public String getRejectText() {
		Cursor c = db.query(true, TABLE_PREFERENCES, ALL_PREFERENCES, null,
				null, null, null, KEY_ID2, "1");
		if (c != null) {
			c.moveToFirst();
			return c.getString(COL_TEXT);
		}
		return null;
	}

	/**
	 * Get the selected status of SMS on auto call reject
	 * @return true if enabled, false otherwise
	 */
	public boolean getRejectTextStatus() {
		Cursor c = db.query(true, TABLE_PREFERENCES, ALL_PREFERENCES, null,
				null, null, null, KEY_ID2, "1");
		if (c != null && c.getCount() > 0) {
			c.moveToFirst();
			if (c.getInt(COL_TEXTREJECT) == 1) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Set the text message for the SMS on auto call reject
	 * @param message Text to store
	 * @return true on successful storage, false otherwise
	 */
	public boolean setRejectText(String message) {
		ContentValues values = new ContentValues();
		values.put(KEY_TEXT, message);
		return db.update(TABLE_PREFERENCES, values, null, null) != 0;
	}

	/**
	 * Set the enabled state of SMS on auto call reject
	 * @param status true if enabled, false otherwise
	 * @return true on successful update of state, false otherwise
	 */
	public boolean setRejectTextStatus(boolean status) {
		int stat = 0;
		if (status)
			stat = 1;
		else
			stat = 0;
		ContentValues values = new ContentValues();
		values.put(KEY_TEXTREJECT, stat);
		return db.update(TABLE_PREFERENCES, values, null, null) != 0;
	}

	/**
	 * Get the profile to be used when a call gets through in
	 * 'No Call Silent' mode
	 * @return Integer value representing the index of the spinner used to display profiles, default is 0
	 */
	public int getThroughProfile() {
		Cursor c = db.query(true, TABLE_PREFERENCES, ALL_PREFERENCES, null,
				null, null, null, KEY_ID2, "1");
		if (c != null && c.getCount() > 0) {
			c.moveToFirst();
			return c.getInt(COL_THROUGHCALLPROFILE);
		}
		return 0;
	}
	
	/**
	 * Set the profile to be used when a call gets through in
	 * 'No Call Silent' mode. It is the integer index of the spinner used
	 * @param profile Integer value of the spinner used to display the profiles
	 * @return true if update successful, false otherwise
	 */
	public boolean setThroughProfile(int profile){
		ContentValues values = new ContentValues();
		values.put(KEY_THROUGHCALLPROFILE, profile);
		return db.update(TABLE_PREFERENCES, values, null, null) != 0;
	}

	/**
	 * Get the status of the emergency calls setting
	 * @return true if enabled, false otherwise
	 */
	public boolean getEmergencyStatus() {
		Cursor c = db.query(true, TABLE_PREFERENCES, ALL_PREFERENCES, null,
				null, null, null, KEY_ID2, "1");
		if (c != null && c.getCount() > 0) {
			c.moveToFirst();
			if (c.getInt(COL_EMERGENCY) == 1) {
				return true;
			}
		}
		return false;
	}

	public boolean setEmergencyStatus(boolean status) {
		int stat = 0;
		if (status)
			stat = 1;
		else
			stat = 0;
		ContentValues values = new ContentValues();
		values.put(KEY_EMERGENCY, stat);
		return db.update(TABLE_PREFERENCES, values, null, null) != 0;
	}

	public boolean getWhiteListStatus() {
		Cursor c = db.query(true, TABLE_PREFERENCES, ALL_PREFERENCES, null,
				null, null, null, KEY_ID2, "1");
		if (c != null && c.getCount() > 0) {
			c.moveToFirst();
			if (c.getInt(COL_WHITELIST) == 1) {
				return true;
			}
		}
		return false;
	}

	public boolean setWhiteListStatus(boolean status) {
		int stat = 0;
		if (status)
			stat = 1;
		else
			stat = 0;
		ContentValues values = new ContentValues();
		values.put(KEY_WHITELIST, stat);
		return db.update(TABLE_PREFERENCES, values, null, null) != 0;
	}

	public boolean checkNumberInWhiteList(String number) {
		Cursor c = db.query(true, TABLE_WHITELIST, ALL_WHITELIST, null, null,
				null, null, KEY_ID3, null);
		if (c != null && c.getCount() > 0) {
			c.moveToFirst();
			for (int i = 0; i < c.getCount(); i++) {
				String whiteNum = c.getString(COL_NUMBER);
				if (number.contains(whiteNum) || whiteNum.contains(number)) {
					return true;
				}
				c.moveToNext();
			}
		}
		return false;
	}

	public boolean deleteAllWhiteList() {
		return db.delete(TABLE_WHITELIST, null, null) != 0;
	}

	// number -> name
	public boolean insertWhiteList(HashMap<String, String> contacts) {
		for (String number : contacts.keySet()) {
			ContentValues values = new ContentValues();
			values.put(KEY_NAME, contacts.get(number));
			values.put(KEY_NUMBER, number);
			if (db.insert(TABLE_WHITELIST, null, values) < 0)
				return false;
		}
		return true;
	}

	public Cursor getWhiteList() {
		Cursor c = db.query(true, TABLE_WHITELIST, ALL_WHITELIST, null, null,
				null, null, KEY_NAME, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	// Add a new set of values to the database.
	public long[] insertNewSetting(int dow, String[] startTimes,
			String[] endTimes, int[] profiles) {

		long[] rowsInserted = new long[profiles.length];
		// Create row's data:
		if ((startTimes.length != endTimes.length)
				|| (endTimes.length != profiles.length))
			return new long[] { -1 };

		class Data {
			private String end;
			private int profile;
		}
		HashMap<String, Data> order = new HashMap<String, Data>();
		for (int i = 0; i < startTimes.length; i++) {
			Data d = new Data();
			d.end = endTimes[i];
			d.profile = profiles[i];
			order.put(startTimes[i], d);
		}

		ArrayList<String> list = new ArrayList<String>(order.keySet());
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
		int i = 0;
		for (String start : list) {
			ContentValues values = new ContentValues();
			values.put(KEY_DOW, dow);
			values.put(KEY_STARTTIME, start);
			values.put(KEY_ENDTIME, order.get(start).end);
			values.put(KEY_PROFILE, order.get(start).profile);
			values.put(KEY_STATUS, 1);

			rowsInserted[i] = db.insert(TABLE_SETTINGS, null, values);
			if (rowsInserted[i] < 0) {
				break;
			}
			i++;
		}
		return rowsInserted;
	}

	public boolean updateStatus(int dow, int status) {
		String where = KEY_DOW + "=" + dow;
		ContentValues values = new ContentValues();
		values.put(KEY_STATUS, status);
		return db.update(TABLE_SETTINGS, values, where, null) != 0;
	}

	// Delete a row from the database, by DOW
	public boolean deleteDaySetting(int dow) {
		String where = KEY_DOW + "=" + dow;
		return db.delete(TABLE_SETTINGS, where, null) != 0;
	}

	// public void deleteAll() {
	// Cursor c = getAllRows();
	// long rowId = c.getColumnIndexOrThrow(KEY_ROWID);
	// if (c.moveToFirst()) {
	// do {
	// deleteRow(c.getLong((int) rowId));
	// } while (c.moveToNext());
	// }
	// c.close();
	// }

	// Return all data in the database.
	// public Cursor getAllSettings() {
	// String where = null;
	// Cursor c = db.query(true, TABLE_SETTINGS, ALL_SETTINGS, where, null,
	// null, null, KEY_ID, null);
	// if (c != null) {
	// c.moveToFirst();
	// }
	// return c;
	// }

	// Get a specific row (by rowId)

	public Cursor getSettingsOfDay(int dow) {
		String where = KEY_DOW + "=" + dow;
		Cursor c = db.query(true, TABLE_SETTINGS, ALL_SETTINGS, where, null,
				null, null, KEY_ID, null);
		if (c != null) {
			c.moveToFirst();
		}
		return c;
	}

	// public Cursor getSettingsForStartTime(int dow, String startTime) {
	// String where = KEY_DOW + "=" + dow + " AND " + KEY_STARTTIME + "= '"
	// + startTime + "'";
	// Cursor c = db.query(true, TABLE_SETTINGS, ALL_SETTINGS, where, null,
	// null, null, KEY_ID, "1");
	// if (c != null) {
	// c.moveToFirst();
	// }
	// return c;
	// }

	// public Cursor getSettingsForEndTime(int dow, String endTime) {
	// String where = KEY_DOW + "=" + dow + " AND " + KEY_ENDTIME + "= '"
	// + endTime + "'";
	// Cursor c = db.query(true, TABLE_SETTINGS, ALL_SETTINGS, where, null,
	// null, null, KEY_ID, "1");
	// if (c != null) {
	// c.moveToFirst();
	// }
	// return c;
	// }

	public boolean checkStatus(int dow) {
		String where = KEY_DOW + "=" + dow;
		Cursor c = db.query(true, TABLE_SETTINGS, ALL_SETTINGS, where, null,
				null, null, null, "1");
		if (c != null && c.getCount() > 0) {
			c.moveToFirst();
			if (c.getInt(COL_STATUS) == 1)
				return true;
		}
		return false;
	}

	// Change an existing row to be equal to new data.
	// public boolean updateRow(long rowId, String name, int studentNum, String
	// favColour) {
	// String where = KEY_ROWID + "=" + rowId;
	//
	// /*
	// * CHANGE 4:
	// */
	// // TODO: Update data in the row with new fields.
	// // TODO: Also change the function's arguments to be what you need!
	// // Create row's data:
	// ContentValues newValues = new ContentValues();
	// newValues.put(KEY_NAME, name);
	// newValues.put(KEY_STUDENTNUM, studentNum);
	// newValues.put(KEY_FAVCOLOUR, favColour);
	//
	// // Insert it into the database.
	// return db.update(DATABASE_TABLE, newValues, where, null) != 0;
	// }

	// ///////////////////////////////////////////////////////////////////
	// Private Helper Classes:
	// ///////////////////////////////////////////////////////////////////

	/**
	 * Private class which handles database creation and upgrading. Used to
	 * handle low-level database access.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase _db) {
			_db.execSQL(TABLE_SETTINGS_CREATE_SQL);
			_db.execSQL(TABLE_PREFERENCES_CREATE_SQL);
			_db.execSQL(TABLE_WHITELIST_CREATE_SQL);
			_db.execSQL(TABLE_CALLRECORD_CREATE_SQL);
			initializePreferences(_db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading application's database from version "
					+ oldVersion + " to " + newVersion
					+ ", which will destroy all old data!");

			// Destroy old database:
			_db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
			_db.execSQL("DROP TABLE IF EXISTS " + TABLE_PREFERENCES);
			_db.execSQL("DROP TABLE IF EXISTS " + TABLE_WHITELIST);
			_db.execSQL("DROP TABLE IF EXISTS " + TABLE_CALLRECORD);

			// Recreate new database:
			onCreate(_db);
		}
	}
}
