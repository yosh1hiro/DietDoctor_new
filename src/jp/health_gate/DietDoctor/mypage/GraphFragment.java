package jp.health_gate.DietDoctor.mypage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import jp.health_gate.DietDoctor.R;
import jp.health_gate.DietDoctor.models.Achievements;
import jp.health_gate.DietDoctor.models.Weights;

/**
 * Created by yoshihiro on 2014/10/27.
 */
public class GraphFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.graph_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View root = getView();

        root.findViewById(R.id.w1_button).setSelected(true);

        root.findViewById(R.id.w1_button).setOnClickListener(new ChangeGraphListener(jp.health_gate.DietDoctor.mypage.GraphView.Term.W1));
        root.findViewById(R.id.m1_button).setOnClickListener(new ChangeGraphListener(jp.health_gate.DietDoctor.mypage.GraphView.Term.M1));
        root.findViewById(R.id.m3_button).setOnClickListener(new ChangeGraphListener(jp.health_gate.DietDoctor.mypage.GraphView.Term.M3));

        jp.health_gate.DietDoctor.mypage.GraphView graphView = (jp.health_gate.DietDoctor.mypage.GraphView) root.findViewById(R.id.graph_view);
        graphView.setSource(Weights.sharedInstance(), Achievements.sharedInstance());
    }

    void setSelected(int selectedResId, int unselectedResId) {
        getView().findViewById(unselectedResId).setSelected(false);
        getView().findViewById(selectedResId).setSelected(true);
    }

    void setSelected(int selectedResId, int unselectedResId1, int unselectedResId2) {
        getView().findViewById(unselectedResId2).setSelected(false);
        setSelected(selectedResId, unselectedResId1);
    }

    private class ChangeGraphListener implements View.OnClickListener {
        jp.health_gate.DietDoctor.mypage.GraphView.Term term;

        ChangeGraphListener(jp.health_gate.DietDoctor.mypage.GraphView.Term term) {
            this.term = term;
        }

        @Override
        public void onClick(View v) {
            jp.health_gate.DietDoctor.mypage.GraphView graphView = (jp.health_gate.DietDoctor.mypage.GraphView) getView().findViewById(R.id.graph_view);
            graphView.setTerm(term);

            switch (term) {
                case W1:
                    setSelected(R.id.w1_button, R.id.m1_button, R.id.m3_button);
                    break;
                case M1:
                    setSelected(R.id.m1_button, R.id.m3_button, R.id.w1_button);
                    break;
                case M3:
                    setSelected(R.id.m3_button, R.id.w1_button, R.id.m1_button);
                    break;
            }
        }
    }
}
