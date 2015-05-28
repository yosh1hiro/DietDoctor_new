package jp.health_gate.DietDoctor.reminder;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import jp.health_gate.DietDoctor.CalendarUtils;
import jp.health_gate.DietDoctor.MainActivity;
import jp.health_gate.DietDoctor.R;
import jp.health_gate.DietDoctor.models.Achievements;
import jp.health_gate.DietDoctor.models.DatabaseHelper;
import jp.health_gate.DietDoctor.models.Weights;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * AlarmManagerからの通知を受け取るレシーバー
 * <p/>
 * Created by kazhida on 2014/01/28.
 */
public class ReminderReceiver extends BroadcastReceiver {

    private static final int SCHEDULE_REQUEST_CODE = 1213;
    private static final int NOTIFICATION_REQUEST_CODE = 1214;

    public static void scheduleReminder(Context context) {
        Calendar calendar = CalendarUtils.dateCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, 21);
        Calendar now = Calendar.getInstance();
        if (now.after(calendar)) {
            calendar.add(Calendar.DATE, 1);
        }

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancelAll();

        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, SCHEDULE_REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        DatabaseHelper.initInstance(context);

        Calendar today = CalendarUtils.dateCalendar();
        Weights.Item weight = Weights.sharedInstance().getRecentWeight(today);
        boolean weightLogged = (weight != null && CalendarUtils.isToday(weight.getDate()));
        List<Achievements.Item> achievements = Achievements.sharedInstance().load(today);
        boolean actionLogged = starCount(achievements) > 0;

        Log.d("DietDoctor", "onHandleIntent (" + weightLogged + "," + actionLogged + ":" + achievements.size() + ")");

        if (actionLogged) {
            Achievements.Item item = achievements.get(achievements.size() - 1);
            Log.d("DietDoctor", new SimpleDateFormat("yy/MM/dd hh:mm:ss").format(item.getDate().getTime()) + ":" + item.getGroupId() + "-" + item.getLevel());
        }

        if (!weightLogged && !actionLogged) {
            sendNotification(context, R.string.notification_no_weight_action);
        } else if (!weightLogged) {
            sendNotification(context, R.string.notification_no_weight);
        } else if (!actionLogged) {
            sendNotification(context, R.string.notification_no_action);
        }
    }

    private int starCount(List<Achievements.Item> items) {
        int result = 0;

        for (Achievements.Item item : items) {
            result += item.getStar();
        }

        return result;
    }

    private static final String NOTIFICATION_NUMBER = "NOTIFICATION_NUMBER";

    private int notificationNumber(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        int number = preferences.getInt(NOTIFICATION_NUMBER, 1);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(NOTIFICATION_NUMBER, number + 1);
        editor.commit();

        return number;
    }

    private void sendNotification(Context context, int msgId) {

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, NOTIFICATION_REQUEST_CODE, intent, 0);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification();

        notification.icon = R.drawable.ic_launcher;
        notification.tickerText = context.getString(R.string.app_name);
        notification.number = notificationNumber(context);
        notification.setLatestEventInfo(context, context.getString(R.string.app_name), context.getString(msgId), pendingIntent);

        manager.notify(notification.number, notification);
    }
}
