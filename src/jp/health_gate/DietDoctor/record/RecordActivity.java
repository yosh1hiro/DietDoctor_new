package jp.health_gate.DietDoctor.record;

import android.support.v4.app.Fragment;
import jp.health_gate.DietDoctor.CustomActivity;

/**
 * 記録画面のActivity
 * <p/>
 * Created by kazhida on 2014/01/15.
 */
public class RecordActivity extends CustomActivity {

    @Override
    protected Category getCategory() {
        return Category.RECORD;
    }

    @Override
    protected Fragment rootFragment() {
        return new EntryFragment();
    }
}
