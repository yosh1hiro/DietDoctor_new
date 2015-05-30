package jp.health_gate.DietDoctor.information;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.health_gate.DietDoctor.CustomFragment;
import jp.health_gate.DietDoctor.R;

/**
 * 情報メニューのフラグメント
 * <p/>
 * Created by kazhida on 2013/10/15.
 */
class InformationMenuFragment extends CustomFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.information_menu_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitleText(R.string.information_title);
        /*
        getView().findViewById(R.id.diet_news_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new DietNewsFragment());
            }
        });

         getView().findViewById(R.id.ranking_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new RankingFragment());
            }
        });




        getView().findViewById(R.id.library_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new MedalLibraryFragment());
            }
        });
         */

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
/*
        getView().findViewById(R.id.ad_pro_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new AdProFragment());
            }
        });

        getView().findViewById(R.id.setting_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new SettingMenuFragment());
            }
        });

        getView().findViewById(R.id.analysis_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new AnalysisFragment());
            }
        });


        getView().findViewById(R.id.logout_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActiveUser.sharedInstance().logout();

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                editor.remove(InitialActivity.NOT_FIRST_TIME);
                editor.commit();

                Achievements.sharedInstance().deleteAll();
                Weights.sharedInstance().deleteAll();

                getActivity().startActivity(new Intent(getActivity(), InitialActivity.class));
            }
        });
        */
    }
}
