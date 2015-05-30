package jp.health_gate.DietDoctor.information;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import jp.health_gate.DietDoctor.CustomFragment;
import jp.health_gate.DietDoctor.R;
import jp.health_gate.DietDoctor.models.Achievements;
import jp.health_gate.DietDoctor.models.ActiveUser;
import jp.health_gate.DietDoctor.models.RankPoints;

/**
 * ランキング表示のフラグメント
 * <p/>
 * Created by kazhida on 2013/10/15.
 */
class RankingFragment extends CustomFragment {

    RankPoints rankPoints;
    RankingView rankingView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ranking_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitleText(R.string.ranking);

        ViewGroup root = (ViewGroup) getView();

        root.findViewById(R.id.graph_button).setSelected(true);
        root.findViewById(R.id.star_button).setSelected(true);

        root.findViewById(R.id.history_button).setOnClickListener(new ShowHistoryListener());
        root.findViewById(R.id.graph_button).setOnClickListener(new ShowGraphListener());
        root.findViewById(R.id.star_button).setOnClickListener(new ChangeGraphListener(RankingView.Target.STAR));
        root.findViewById(R.id.diff_button).setOnClickListener(new ChangeGraphListener(RankingView.Target.RATE));
        root.findViewById(R.id.bmi_button).setOnClickListener(new ChangeGraphListener(RankingView.Target.BMI));

        rankingView = (RankingView) root.findViewById(R.id.ranking_view);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (rankPoints == null) {
            rankPoints = new RankPoints(new RankPoints.Callback() {
                @Override
                public void onLoaded() {
                    rankingView.setRankPoints(rankPoints);
                    rankingView.setTarget(RankingView.Target.STAR, new RankingView.Callback() {
                        @Override
                        public void onPrepared() {
                            showRanking();
                        }
                    });
                }

                @Override
                public void onFailed() {
                }
            });
        }
    }


    void setSelected(int selectedResId, int unselectedResId) {
        getView().findViewById(unselectedResId).setSelected(false);
        getView().findViewById(selectedResId).setSelected(true);
    }

    void setSelected(int selectedResId, int unselectedResId1, int unselectedResId2) {
        getView().findViewById(unselectedResId2).setSelected(false);
        setSelected(selectedResId, unselectedResId1);
    }

    private class ShowHistoryListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            setSelected(R.id.history_button, R.id.graph_button);
        }
    }

    private class ShowGraphListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            setSelected(R.id.graph_button, R.id.history_button);
        }
    }

    private String currentTarget() {
        RankingView rankingView = (RankingView) getView().findViewById(R.id.ranking_view);
        switch (rankingView.getTarget()) {
            case STAR:
                return getString(R.string.rank_point_star);
            case RATE:
                return getString(R.string.rank_point_rate);
            case BMI:
                return getString(R.string.rank_point_bmi);
        }
        return "";
    }

    private String currentRankPoint() {
        RankingView rankingView = (RankingView) getView().findViewById(R.id.ranking_view);

        switch (rankingView.getTarget()) {
            case STAR:
                return "" + Achievements.sharedInstance().getStarCount();
            case RATE:
                return String.format("%1.1f", rankingView.getRate()) + "kg/day";
            case BMI:
                return String.format("%1.1f", rankingView.getBMI()) + "kg/m2";
        }
        return "";
    }

    private void showRanking() {
        RankingView rankingView = (RankingView) getView().findViewById(R.id.ranking_view);
        String userName = ActiveUser.sharedInstance().getUserName();
        setText(R.id.rank_point_prefix, getString(R.string.rank_point_prefix, userName, currentTarget()));
        setText(R.id.rank_point_text, currentRankPoint());
        setValue(R.id.ranking_text, rankingView.getRank());
        setText(R.id.ranking_suffix, getString(R.string.ranking_suffix, rankingView.getPopulation()));
    }

    private class ChangeGraphListener implements View.OnClickListener {
        RankingView.Target trg;

        ChangeGraphListener(RankingView.Target trg) {
            this.trg = trg;
        }

        @Override
        public void onClick(View v) {
            switch (trg) {
                case STAR:
                    setSelected(R.id.star_button, R.id.diff_button, R.id.bmi_button);
                    break;
                case RATE:
                    setSelected(R.id.diff_button, R.id.bmi_button, R.id.star_button);
                    break;
                case BMI:
                    setSelected(R.id.bmi_button, R.id.star_button, R.id.diff_button);
                    break;
            }
            rankingView.setTarget(trg, new RankingView.Callback() {
                @Override
                public void onPrepared() {
                    showRanking();
                }
            });
        }
    }
}
