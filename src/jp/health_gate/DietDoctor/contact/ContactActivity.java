package jp.health_gate.DietDoctor.contact;

import android.support.v4.app.Fragment;
import jp.health_gate.DietDoctor.CustomActivity;

/**
 * DDクリニックのActivity
 * <p/>
 * Created by kazhida on 2014/01/15.
 */
public class ContactActivity extends CustomActivity {

    @Override
    protected Category getCategory() {
        return Category.CONTACT;
    }

    @Override
    protected Fragment rootFragment() {
        return new ConsultationFragment();
    }
}
