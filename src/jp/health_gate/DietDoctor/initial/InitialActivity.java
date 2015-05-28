package jp.health_gate.DietDoctor.initial;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import jp.health_gate.DietDoctor.R;

/**
 * 起動時のアクティビティ
 * といっても、単なるフラグメントのコンテナ
 * <p/>
 * 登録済みの場合は、スプラッシュとして機能する
 * <p/>
 * Created by kazhida on 2013/10/04.
 */
public class InitialActivity extends FragmentActivity {

    public static final String INITIAL_ROOT = "INITIAL_ROOT";
    public static final String NOT_FIRST_TIME = "NOT_FIRST_TIME";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.initial);

        //  最初の画面用フラグメントを追加
        Fragment fragment = IntroductionFragment.newInstance(0);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.fragment_container, fragment);
        transaction.addToBackStack(INITIAL_ROOT);
        transaction.commit();
    }
}
