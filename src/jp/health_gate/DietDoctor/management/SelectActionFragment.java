package jp.health_gate.DietDoctor.management;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import com.haarman.supertooltips.ToolTipRelativeLayout;
import jp.health_gate.DietDoctor.CustomFragment;
import jp.health_gate.DietDoctor.Scenario;
import jp.health_gate.DietDoctor.models.DietActions;
import jp.health_gate.DietDoctor.R;

/**
 * 減量行動選択用のフラグメント
 * <p/>
 * Created by kazhida on 2013/10/09.
 */
class SelectActionFragment extends CustomFragment {

    private static final String INDEX = "INDEX";
    private int goalIndex;
    private Scenario scenario;

    public static SelectActionFragment newInstance(int index) {
        SelectActionFragment fragment = new SelectActionFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(INDEX, index);

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        goalIndex = getArguments().getInt(INDEX);
        return inflater.inflate(R.layout.select_action_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitleText(getString(R.string.diet_action_title) + "." + (goalIndex + 1));

        GridView grid = (GridView) getView().findViewById(R.id.action_grid);

        GridAdapter adapter = new GridAdapter();
        SwingBottomInAnimationAdapter animator = new SwingBottomInAnimationAdapter(adapter);

        animator.setAbsListView(grid);
        grid.setAdapter(animator);
        grid.setOnItemClickListener(adapter);

        ToolTipRelativeLayout layout = (ToolTipRelativeLayout) getActivity().findViewById(R.id.tooltip_layout);
        int color = getResources().getColor(R.color.bg_tutorial);
        scenario = new Scenario(layout, color);
        scenario.setParentView(getView());
        scenario.addStep(1000, R.string.tutorial_select_action, R.id.action_grid);
        scenario.step(0);
    }

    @Override
    public void onPause() {
        if (scenario != null) scenario.end();
        super.onPause();
    }

    private class GridAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

        @Override
        public int getCount() {
            return DietActions.sharedInstance().getActions().size();
        }

        @Override
        public Object getItem(int position) {
            return DietActions.sharedInstance().getActions().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup view = (ViewGroup) convertView;

            if (view == null) {
                view = (ViewGroup) getActivity().getLayoutInflater().inflate(R.layout.select_action_item, null, false);
            }
            DietActions.ChallengeItem item = (DietActions.ChallengeItem) getItem(position);

            ImageView imageView = (ImageView) view.findViewById(R.id.icon);
            TextView textView = (TextView) view.findViewById(R.id.caption);

            imageView.setImageDrawable(item.getIcon());
            textView.setText(item.getCaption());

            view.setTag(item);

            return view;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            DietActions.ChallengeItem action = (DietActions.ChallengeItem) view.getTag();
            replaceFragment(DetailActionFragment.newInstance(goalIndex, action.getGroupId()));
        }
    }
}
