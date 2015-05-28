package jp.health_gate.DietDoctor;

import java.util.Calendar;

/**
 * カレンダーを生成したりするクラス
 * <p/>
 * Created by kazhida on 2013/12/15.
 */
public class CalendarUtils {

    public static Calendar clearTime(Calendar calendar) {
        calendar.clear(Calendar.HOUR_OF_DAY);
        calendar.clear(Calendar.HOUR);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);

        return calendar;
    }

    public static Calendar monthCalendar() {
        Calendar calendar = dateCalendar();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return calendar;
    }

    public static Calendar dateCalendar() {
        return clearTime(Calendar.getInstance());
    }

    public static Calendar dateCalendar(Calendar source) {
        return dateCalendar(source, 0);
    }

    public static Calendar dateCalendar(Calendar source, int dayOffset) {
        return dateCalendar(source.getTimeInMillis(), dayOffset);
    }

    public static Calendar dateCalendar(long source, int dayOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(source);
        calendar.add(Calendar.DATE, dayOffset);
        return clearTime(calendar);
    }

    public static boolean isToday(Calendar calendar) {
        Calendar today = Calendar.getInstance();
        return today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                today.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                today.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH);


    }
}
