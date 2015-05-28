package jp.health_gate.DietDoctor.initial;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import jp.health_gate.DietDoctor.models.ActiveUser;
import jp.health_gate.DietDoctor.R;

/**
 * アプリの紹介ページ用フラグメント
 * <p/>
 * Created by kazhida on 2013/10/04.
 */
class IntroductionFragment extends Fragment {

    private static final String PAGE = "PAGE";
    private int page;

    public static IntroductionFragment newInstance(int page) {
        IntroductionFragment fragment = new IntroductionFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(PAGE, page);

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        page = getArguments().getInt(PAGE);
        return inflater.inflate(R.layout.introduction_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ImageView bg = (ImageView) getView().findViewById(R.id.bg_intro_image);

        switch (page) {
            case 0:
                bg.setImageResource(R.drawable.bg_introduction_0);
                getView().findViewById(R.id.left_arrow).setVisibility(View.GONE);
                break;
            default:
                bg.setImageResource(R.drawable.bg_introduction_1);
                break;
        }

        getView().findViewById(R.id.left_arrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        getView().findViewById(R.id.right_arrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNext();
            }
        });

        bg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNext();
            }
        });

        //  初回でなければスキップ
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (preferences.getBoolean(InitialActivity.NOT_FIRST_TIME, false)) {
            onNext();
        }
    }

    private void onNext() {
        switch (page) {
            case 0:
                replaceToNext();
                break;
            default:
                if (ActiveUser.sharedInstance().isLoggedIn()) {
                    replaceToLogout();
                } else {
                    replaceToLogin();
                }
                break;
        }
    }

    private void replaceToNext() {
        IntroductionFragment fragment = IntroductionFragment.newInstance(page + 1);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void replaceToLogin() {
        LoginFragment fragment = new LoginFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void replaceToLogout() {
        LogoutFragment fragment = new LogoutFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
