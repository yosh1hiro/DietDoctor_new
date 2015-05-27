package jp.health_gate.DietDoctor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

/**
 * 共通部分を切り出したアクティビティ
 * <p/>
 * Created by kazhida on 2014/01/15.
 */
public abstract class CustomActivity extends FragmentActivity implements KeyboardDetectLayout.OnKeyboardDetectListener {

    public static final String ROOT_FRAGMENT = "ROOT_FRAGMENT";

    public enum Category {
        INITIAL,
        MYPAGE,
        MANAGEMENT,
        RECORD,
        CONTACT,
        INFORMATION,
        MESSAGE
    }

    protected abstract Category getCategory();

    protected abstract Fragment rootFragment();

    private AdView adView;
    private boolean shownAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.main);

        KeyboardDetectLayout detector = (KeyboardDetectLayout) findViewById(R.id.keyboard_detector);
        if (detector != null) {
            detector.setOnKeyboardDetectListener(this);
        }

        //  最初の画面用フラグメントを追加
        FragmentManager.enableDebugLogging(true);
        Fragment fragment = rootFragment();
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.fragment_container, fragment);
        transaction.commit();


        adView = new AdView(this, AdSize.BANNER, getString(R.string.banner_unit_id));
        FrameLayout frame = (FrameLayout) findViewById(R.id.ad_frame);
        frame.addView(adView);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle bundle = intent.getExtras();
        if (bundle != null && bundle.getBoolean(ROOT_FRAGMENT, false)) {
            //  最初のフラグメントに戻す。
            popToRoot();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        adView.loadAd(new AdRequest());
    }

    @Override
    protected void onPause() {
        adView.stopLoading();
        super.onPause();
    }


    protected void onDestroy() {
        adView.destroy();
        super.onDestroy();
    }

    @Override
    public void onKeyboardShown() {
        shownAd = isShownAd();
        hideAd();

        View footer = findViewById(R.id.footer_tabs);
        if (footer != null) {
            footer.setVisibility(View.GONE);
        }

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment instanceof KeyboardDetectLayout.OnKeyboardDetectListener) {
            KeyboardDetectLayout.OnKeyboardDetectListener listener = (KeyboardDetectLayout.OnKeyboardDetectListener) fragment;
            listener.onKeyboardShown();
        }
    }

    @Override
    public void onKeyboardHidden() {
        if (shownAd) showAd();

        View footer = findViewById(R.id.footer_tabs);
        if (footer != null) {
            footer.setVisibility(View.VISIBLE);
        }

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment instanceof KeyboardDetectLayout.OnKeyboardDetectListener) {
            KeyboardDetectLayout.OnKeyboardDetectListener listener = (KeyboardDetectLayout.OnKeyboardDetectListener) fragment;
            listener.onKeyboardHidden();
        }
    }

    public void showAd() {
        View frame = findViewById(R.id.ad_frame);
        if (frame != null) {
            frame.setVisibility(View.VISIBLE);
        }
    }

    public void hideAd() {
        View frame = findViewById(R.id.ad_frame);
        if (frame != null) {
            frame.setVisibility(View.GONE);
        }
    }

    public boolean isShownAd() {
        View frame = findViewById(R.id.ad_frame);
        return frame != null && frame.getVisibility() == View.VISIBLE;
    }

    public void popToRoot() {
        FragmentManager manager = getSupportFragmentManager();

        while (manager.getBackStackEntryCount() > 0) {
            Log.d("DietDoctor", "stack count = " + manager.getBackStackEntryCount());
            manager.popBackStackImmediate();
        }
    }
}
