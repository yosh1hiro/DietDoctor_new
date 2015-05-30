package jp.health_gate.DietDoctor.management;

import android.support.v4.app.Fragment;
import jp.health_gate.DietDoctor.CustomActivity;

/**
 * 目標設定画面のActivity
 * <p/>
 * Created by kazhida on 2014/01/15.
 */
public class ManagementActivity extends CustomActivity {

    @Override
    protected Category getCategory() {
        return Category.MANAGEMENT;
    }

    @Override
    protected Fragment rootFragment() {
        return new GoalFragment();
    }
}
