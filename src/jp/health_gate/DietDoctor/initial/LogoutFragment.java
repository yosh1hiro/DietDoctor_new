package jp.health_gate.DietDoctor.initial;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import jp.health_gate.DietDoctor.models.ActiveUser;
import jp.health_gate.DietDoctor.R;

/**
 * ログアウト用のフラグメント
 * <p/>
 * Created by kazhida on 2013/10/07.
 */
class LogoutFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.logout_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setTitleText(R.string.welcome_title);

        View root = getView();

        root.findViewById(R.id.next_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        root.findViewById(R.id.logout_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActiveUser.sharedInstance().logout();

                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                editor.remove(InitialActivity.NOT_FIRST_TIME);
                editor.commit();

                getFragmentManager().popBackStack(InitialActivity.INITIAL_ROOT, 0);
            }
        });

        TextView textView = (TextView) root.findViewById(R.id.welcome_message);
        textView.setText(getString(R.string.welcome_prefix) +
                ActiveUser.sharedInstance().getUserName() +
                getString(R.string.welcome_suffix));

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        editor.putBoolean(InitialActivity.NOT_FIRST_TIME, true);
        editor.commit();
    }

    private void setTitleText(String title) {
        TextView textView = (TextView) getView().findViewById(R.id.title_text);
        textView.setText(title);
    }

    private void setTitleText(int resId) {
        setTitleText(getString(resId));
    }
}
