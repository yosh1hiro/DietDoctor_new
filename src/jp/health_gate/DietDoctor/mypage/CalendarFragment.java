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
 * Created by yoshihiro on 2014/11/03.
 */

class CalendarFragment extends CustomFragment {

//    private Weights.Item currentWeight;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.calendar_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitleText(R.string.history_title);

        ViewGroup root = (ViewGroup) getView();

        root.findViewById(R.id.calendar_button).setSelected(true);

        root.findViewById(R.id.calendar_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!emptyFragment()) {
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.calendar_container, new MonthCalendarFragment());
                    transaction.commit();
                    setSelected(R.id.calendar_button, R.id.graph_button);
                }
            }
        });

        root.findViewById(R.id.graph_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!emptyFragment()) {
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.calendar_container, new GraphFragment());
                    transaction.commit();
                    setSelected(R.id.graph_button, R.id.calendar_button);
                }
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
                    transaction.add(R.id.calendar_container, new MonthCalendarFragment());
                    transaction.commit();
                }
            }, 50);
        }
    }

    private boolean emptyFragment() {
        return getFragmentManager().findFragmentById(R.id.calendar_container) == null;
    }

    void setSelected(int selectedResId, int unselectedResId) {
        getView().findViewById(unselectedResId).setSelected(false);
        getView().findViewById(selectedResId).setSelected(true);
    }
}
