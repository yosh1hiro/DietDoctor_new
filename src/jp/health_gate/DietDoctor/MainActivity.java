package jp.health_gate.DietDoctor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import jp.health_gate.DietDoctor.initial.InitialActivity;
import jp.health_gate.DietDoctor.models.*;
import jp.health_gate.DietDoctor.reminder.ReminderReceiver;

/**
 * メインアクティビティ
 * といっても、単なるフラグメントのコンテナ
 * <p/>
 * Created by kazhida on 2013/10/07.
 */
public class MainActivity extends CustomActivity {

    protected Category getCategory() {
        return Category.INITIAL;
    }

    protected Fragment rootFragment() {
        return new StartFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //  内部データの初期化
        ActiveUser.initInstance(this, null);
        DietActions.initInstance(this);
        if (!ActiveUser.sharedInstance().isLoggedIn()) {
            startActivity(new Intent(this, InitialActivity.class));
        }

        ReminderReceiver.scheduleReminder(this.getBaseContext());

        hideAd();
    }

    protected void onDestroy() {
        DietActions.sharedInstance().abortLoadImages();
        super.onDestroy();
    }
}
