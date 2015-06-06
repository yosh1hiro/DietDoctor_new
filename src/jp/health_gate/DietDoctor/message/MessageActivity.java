package jp.health_gate.DietDoctor.message;

import android.support.v4.app.Fragment;
import jp.health_gate.DietDoctor.CustomActivity;

/**
 * Created by yoshihiro on 2014/11/22.
 */
public class MessageActivity extends CustomActivity {

    @Override
    protected Category getCategory() {
        return Category.MESSAGE;
    }

    @Override
    protected Fragment rootFragment() { return new DietNewsFragment();
    }
}
