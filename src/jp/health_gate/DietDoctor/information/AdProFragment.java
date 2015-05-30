package jp.health_gate.DietDoctor.information;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import jp.health_gate.DietDoctor.CustomActivity;
import jp.health_gate.DietDoctor.CustomFragment;
import jp.health_gate.DietDoctor.R;

/**
 * プロ版にアップデートのフラグメント
 * <p/>
 * Created by kazhida on 2013/10/15.
 */
public class AdProFragment extends CustomFragment {

    private boolean shownAd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ad_pro_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
/*
        getView().findViewById(R.id.upgrade_button).setVisibility(View.GONE);

        setTitleText("ダイエット・ドクター プロ");
        */
    }

    @Override
    public void onResume() {
        super.onResume();
        CustomActivity activity = (CustomActivity) getActivity();
        shownAd = activity.isShownAd();
        activity.hideAd();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (shownAd) {
            CustomActivity activity = (CustomActivity) getActivity();
            activity.showAd();
        }
    }
}
