package jp.health_gate.DietDoctor.information;

import android.support.v4.app.Fragment;
import jp.health_gate.DietDoctor.CustomActivity;

/**
 * 情報画面用のActivity
 * <p/>
 * Created by kazhida on 2014/01/15.
 */
public class InformationActivity extends CustomActivity {

    @Override
    protected Category getCategory() {
        return Category.INFORMATION;
    }

    @Override
    protected Fragment rootFragment() {
        return new InformationMenuFragment();
    }
}
