package jp.health_gate.DietDoctor.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 起動時にリマインダーをAlarmに設定する
 * <p/>
 * Created by kazhida on 2014/01/29.
 */
public class ReminderReceiverOnBoot extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ReminderReceiver.scheduleReminder(context);
    }
}
