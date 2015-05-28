package jp.health_gate.DietDoctor.mypage;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.health_gate.DietDoctor.CustomFragment;
import jp.health_gate.DietDoctor.R;

/**
 * Created by yoshihiro on 2014/10/27.
 */
public class HistoryFragment extends CustomFragment {
    //    private Weights.Item currentWeight;

    private static final String ARG_SECTION_NUMBER = "section_number";

    public static HistoryFragment newInstance(int sectionNumber) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public HistoryFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.history_fragment, container, false);

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitleText(R.string.history_title);

        ViewGroup root = (ViewGroup) getView();

        root.findViewById(R.id.action_history_button).setSelected(true);

        root.findViewById(R.id.action_history_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!emptyFragment()) {
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.action_history, new GraphFragment());
                    transaction.commit();
                    setSelected(R.id.action_history_button, R.id.information_analyse_button, R.id.information_medal_library_button);
                }
            }
        });

        root.findViewById(R.id.information_analyse_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!emptyFragment()) {
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.action_history, new AnalysisFragment());
                    transaction.commit();
                    setSelected(R.id.information_analyse_button, R.id.action_history_button, R.id.information_medal_library_button);
                }
            }
        });

        root.findViewById(R.id.information_medal_library_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.action_history, new MedalLibraryFragment());
                    transaction.commit();
                    setSelected(R.id.information_medal_library_button, R.id.information_analyse_button, R.id.action_history_button);
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        if (emptyFragment()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.add(R.id.action_history, new GraphFragment());
                    transaction.commit();
                }
            }, 50);
        }
    }


    private boolean emptyFragment() {
        return getFragmentManager().findFragmentById(R.id.action_history) == null;
    }


    void setSelected(int selectedResId, int unselectedResId, int unselectedResId2) {
        getView().findViewById(unselectedResId).setSelected(false);
        getView().findViewById(unselectedResId2).setSelected(false);
        getView().findViewById(selectedResId).setSelected(true);
    }


}
