package jp.health_gate.DietDoctor.mypage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import jp.health_gate.DietDoctor.R;
import jp.health_gate.DietDoctor.models.DietActions;

/**
 * Created by yoshihiro on 2014/10/27.
 */
public class AnalysisFragment extends Fragment {

    private enum Target {
        YOURSELF,
        SIMILAR,
        AVERAGE
    }

    private Target currentTarget = Target.YOURSELF;
    private boolean showPace = true;
    private DummyAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.analysis_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        /*setTitleText("ダイエット分析"); */



        adapter = new DummyAdapter();

        ViewGroup root = (ViewGroup) getView();

        root.findViewById(R.id.pace_button).setSelected(true);
        root.findViewById(R.id.yourself_button).setSelected(true);
        setCaptionText(R.id.caption_text, "達成ペースの速い項目ランキング(推奨ペース比)");
        setCaptionText(R.id.eval_unit_text, "倍");

        root.findViewById(R.id.pace_button).setOnClickListener(new ShowPaceListener());
        root.findViewById(R.id.effect_button).setOnClickListener(new ShowEffectListener());
        root.findViewById(R.id.yourself_button).setOnClickListener(new ChangeGraphListener(Target.YOURSELF));
        root.findViewById(R.id.similar_button).setOnClickListener(new ChangeGraphListener(Target.SIMILAR));
        root.findViewById(R.id.average_button).setOnClickListener(new ChangeGraphListener(Target.AVERAGE));

        ListView listView = (ListView) root.findViewById(R.id.action_list);
        listView.setAdapter(adapter);
    }

    void setSelected(int selectedResId, int unselectedResId) {
        getView().findViewById(unselectedResId).setSelected(false);
        getView().findViewById(selectedResId).setSelected(true);
    }

    void setSelected(int selectedResId, int unselectedResId1, int unselectedResId2) {
        getView().findViewById(unselectedResId2).setSelected(false);
        setSelected(selectedResId, unselectedResId1);
    }

    void setCaptionText(int id, String text) {
        TextView textView = (TextView) getView().findViewById(id);
        textView.setText(text);
    }

    private class ShowPaceListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            setSelected(R.id.pace_button, R.id.effect_button);
            setCaptionText(R.id.caption_text, "達成ペースの速い項目ランキング(推奨ペース比)");
            setCaptionText(R.id.eval_unit_text, "倍");
            showPace = true;
            adapter.notifyDataSetChanged();
        }
    }

    private class ShowEffectListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            setSelected(R.id.effect_button, R.id.pace_button);
            setCaptionText(R.id.caption_text, "減量効果の大きい項目ランキング");
            setCaptionText(R.id.eval_unit_text, "kg");
            showPace = false;
            adapter.notifyDataSetChanged();
        }
    }

    private class ChangeGraphListener implements View.OnClickListener {
        Target target;

        ChangeGraphListener(Target target) {
            this.target = target;
        }

        @Override
        public void onClick(View v) {
            switch (target) {
                case YOURSELF:
                    setSelected(R.id.yourself_button, R.id.similar_button, R.id.average_button);
                    break;
                case SIMILAR:
                    setSelected(R.id.similar_button, R.id.average_button, R.id.yourself_button);
                    break;
                case AVERAGE:
                    setSelected(R.id.average_button, R.id.yourself_button, R.id.similar_button);
                    break;
            }
            currentTarget = target;
            adapter.notifyDataSetChanged();
        }
    }

    private class DummyAdapter extends BaseAdapter {

        private class ListItem {
            int groupId;
            int level;
            float point;

            ListItem(int groupId, int level, float point) {
                this.groupId = groupId;
                this.level = level;
                this.point = point;
            }
        }

        private ListItem[] paceYou = new ListItem[]{
                new ListItem(1, 7, 3.0f),
                new ListItem(2, 6, 2.8f),
                new ListItem(3, 7, 2.7f),
                new ListItem(4, 8, 2.4f),
                new ListItem(5, 7, 2.3f),
                new ListItem(6, 6, 2.1f),
                new ListItem(7, 7, 1.8f),
                new ListItem(8, 8, 1.5f),
        };

        private ListItem[] paceSimilar = new ListItem[]{
                new ListItem(3, 7, 2.9f),
                new ListItem(3, 6, 2.8f),
                new ListItem(2, 7, 2.7f),
                new ListItem(4, 8, 2.6f),
                new ListItem(6, 6, 2.2f),
                new ListItem(5, 7, 2.0f),
                new ListItem(8, 8, 1.9f),
                new ListItem(7, 7, 1.7f),
        };

        private ListItem[] paceAverage = new ListItem[]{
                new ListItem(1, 7, 2.9f),
                new ListItem(2, 6, 2.8f),
                new ListItem(3, 7, 2.5f),
                new ListItem(4, 8, 2.4f),
                new ListItem(5, 7, 2.3f),
                new ListItem(8, 8, 2.2f),
                new ListItem(6, 6, 1.8f),
                new ListItem(7, 7, 1.7f),
        };


        private ListItem[] effectYou = new ListItem[]{
                new ListItem(6, 10, 2.5f),
                new ListItem(7, 6, 1.4f),
                new ListItem(8, 2, 0.9f),
                new ListItem(9, 3, 0.8f),
                new ListItem(5, 9, 0.5f),
                new ListItem(4, 5, 0.3f),
                new ListItem(3, 3, 0.3f),
                new ListItem(2, 3, 0.3f),
        };

        private ListItem[] effectSimilar = new ListItem[]{
                new ListItem(6, 1, 1.2f),
                new ListItem(6, 2, 0.8f),
                new ListItem(3, 3, 0.7f),
                new ListItem(5, 4, 0.6f),
                new ListItem(5, 9, 0.5f),
                new ListItem(2, 3, 0.3f),
                new ListItem(4, 5, 0.2f),
                new ListItem(3, 3, 0.2f),
        };

        private ListItem[] effectAverage = new ListItem[]{
                new ListItem(7, 4, 1.2f),
                new ListItem(6, 2, 0.9f),
                new ListItem(2, 7, 0.9f),
                new ListItem(9, 9, 0.8f),
                new ListItem(4, 5, 0.6f),
                new ListItem(3, 3, 0.4f),
                new ListItem(5, 9, 0.3f),
                new ListItem(2, 3, 0.2f),
        };

        private ListItem[] getItems() {
            switch (currentTarget) {
                case YOURSELF:
                    return showPace ? paceYou : effectYou;
                case SIMILAR:
                    return showPace ? paceSimilar : effectSimilar;
                default:
                    return showPace ? paceAverage : effectAverage;
            }
        }


        @Override
        public int getCount() {
            return getItems().length;
        }

        @Override
        public Object getItem(int position) {
            return getItems()[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup viewGroup = (ViewGroup) convertView;

            if (viewGroup == null) {
                viewGroup = (ViewGroup) getActivity().getLayoutInflater().inflate(R.layout.analysis_item, null, false);
            }

            ListItem item = getItems()[position];
            DietActions.LeveledItem action = DietActions.sharedInstance().findAction(item.groupId, item.level);

            TextView order = (TextView) viewGroup.findViewById(R.id.order_text);
            ImageView icon = (ImageView) viewGroup.findViewById(R.id.action_icon);
            TextView level = (TextView) viewGroup.findViewById(R.id.action_level);
            TextView title = (TextView) viewGroup.findViewById(R.id.action_title);
            TextView point = (TextView) viewGroup.findViewById(R.id.rank_point_text);

            order.setText("" + (position + 1));
            icon.setImageDrawable(action.getIcon());
            level.setText("" + action.getLevel());
            title.setText(action.getTitle());
            point.setText(String.format("%1.1f", item.point));

            return viewGroup;
        }
    }
}