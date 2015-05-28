package jp.health_gate.DietDoctor.mypage;

import android.support.v4.app.Fragment;
import jp.health_gate.DietDoctor.CustomActivity;

/**
 * MyPage画面のActivity
 * <p/>
 * Created by yoshihiro on 2014/10/27.
 */
public class MyPageActivity extends CustomActivity {

    @Override
    protected Category getCategory() {
        return Category.MYPAGE;
    }

    @Override
    protected Fragment rootFragment() {
        return new HistoryFragment();
    }



}