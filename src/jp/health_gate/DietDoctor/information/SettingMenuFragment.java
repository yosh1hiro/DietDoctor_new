package jp.health_gate.DietDoctor.information;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import jp.health_gate.DietDoctor.CustomFragment;
import jp.health_gate.DietDoctor.R;

/**
 * ヘルプ・設定メニューのフラグメント
 * <p/>
 * Created by kazhida on 2013/10/15.
 */
class SettingMenuFragment extends CustomFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.setting_menu_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitleText("ヘルプ・設定");

        getView().findViewById(R.id.preferences_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new ProfileFragment());
            }
        });

        getView().findViewById(R.id.about_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new AboutUsFragment());
            }
        });
        getView().findViewById(R.id.agreement_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new AgreementFragment());
            }
        });
        getView().findViewById(R.id.privacy_policy_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new PrivacyPolicyFragment());
            }
        });

        getView().findViewById(R.id.ad_pro_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new AdProFragment());
            }
        });
    }
}
