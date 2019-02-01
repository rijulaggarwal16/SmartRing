package play.rijul.smartring;

/**
 * 
 * @author Rijul
 * A general utility class with strings for Day Names, along with their respective number in week.
 * Representation and values match those in Calendar class
 * For eg. Sunday = 1 (1st day of week)
 */
public class DaysOfWeek {

	public static final String MONDAY = "Monday";
	public static final String TUESDAY = "Tuesday";
	public static final String WEDNESDAY = "Wednesday";
	public static final String THURSDAY = "Thursday";
	public static final String FRIDAY = "Friday";
	public static final String SATURDAY = "Saturday";
	public static final String SUNDAY = "Sunday";

	public static final int DOW_SUNDAY = 1;
	public static final int DOW_MONDAY = 2;
	public static final int DOW_TUESDAY = 3;
	public static final int DOW_WEDNESDAY = 4;
	public static final int DOW_THURSDAY = 5;
	public static final int DOW_FRIDAY = 6;
	public static final int DOW_SATURDAY = 7;

	/**
	 * Convert an integer representation for a day of week into the respective String value.
	 * Value should be valid between 1 and 7 including. This representation follows that of Calendar class
	 * @param dow Integer representing the day of week
	 * @return String representing the passed day of week integer, if it is valid (1 to 7), null otherwise
	 */
	public static String getDayName(int dow) {
		if (dow > 0 && dow < 8) {
			String[] days = { SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY,
					FRIDAY, SATURDAY };
			return days[dow - 1];
		} else
			return null;
	}

}
