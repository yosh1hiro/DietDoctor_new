package jp.health_gate.DietDoctor.management;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;
import com.haarman.supertooltips.ToolTipRelativeLayout;
import jp.health_gate.DietDoctor.*;
import jp.health_gate.DietDoctor.models.ActiveUser;
import jp.health_gate.DietDoctor.models.DietActions;
import jp.health_gate.DietDoctor.models.DietNews;

import java.util.Calendar;

/**
 * レベル選択用フラグメント
 * <p/>
 * Created by kazhida on 2013/10/09.
 */
class SelectLevelFragment extends CustomFragment {

    private static final String INDEX = "INDEX";
    private static final String ID = "ID";
    private DietActions.ChallengeItem action;
    private int goalIndex;
    private ViewPager pager;
    private Scenario scenario;

    static SelectLevelFragment newInstance(int index, int id) {
        SelectLevelFragment fragment = new SelectLevelFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(INDEX, index);
        bundle.putInt(ID, id);

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        goalIndex = getArguments().getInt(INDEX);
        int groupId = getArguments().getInt(ID);
        action = DietActions.sharedInstance().findAction(groupId);
        return inflater.inflate(R.layout.select_level_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitleText(getString(R.string.select_level_title));

        pager = (ViewPager) getView().findViewById(R.id.level_pager);
        pager.setAdapter(new LevelAdapter());

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        editor.remove(DietNews.TIPS_TITLE);
        editor.remove(DietNews.TIPS_CONTENT);
        editor.commit();

        getView().findViewById(R.id.select_level_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int level = pager.getCurrentItem() + 1;
                ActiveUser.sharedInstance().storeSelected(goalIndex, action.getGroupId(), level);
                DietActions.LeveledItem item = DietActions.sharedInstance().findAction(action.getGroupId(), level);
                item.initAchievement(Calendar.getInstance());
                DietNews.Item tips = DietNews.getLevelTips(item.getGroupId(), item.getLevel());
                if (tips != null) {
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                    editor.putString(DietNews.TIPS_TITLE, tips.getTitle());
                    editor.putString(DietNews.TIPS_CONTENT, tips.getContent());
                    editor.commit();
                }
                popToRoot();
            }
        });

        ToolTipRelativeLayout layout = (ToolTipRelativeLayout) getActivity().findViewById(R.id.tooltip_layout);
        int color = getResources().getColor(R.color.bg_tutorial);
        scenario = new Scenario(layout, color);
        scenario.setParentView(getView());
        scenario.addStep(1000, R.string.tutorial_set_level, R.id.level_pager);
        scenario.addStep(500, R.string.tutorial_select_level, R.id.arrow_r);
        scenario.addStep(500, R.string.tutorial_decide_level, R.id.select_level_button);
        scenario.step(0);
    }

    @Override
    public void onPause() {
        if (scenario != null) scenario.end();
        super.onPause();
    }

    private class LevelAdapter extends PagerAdapter {

        private void setText(View layout, int id, String text) {
            TextView textView = (TextView) layout.findViewById(id);
            textView.setText(text);
        }

        private void setImage(View layout, int id, Drawable drawable) {
            ImageView imageView = (ImageView) layout.findViewById(id);
            imageView.setImageDrawable(drawable);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View result = getActivity().getLayoutInflater().inflate(R.layout.level_page, null);
            DietActions.LeveledItem level = action.getLevels().get(position);

            setImage(result, R.id.action_icon, level.getIcon());
            setText(result, R.id.action_title, level.getTitle());
            setText(result, R.id.action_level, getString(R.string.level_prefix) + level.getLevel());
            setText(result, R.id.action_star_count, getString(R.string.star_prefix) + level.getStar());
            setAchievementIndicator(result, level);
            setImage(result, R.id.level_scene, level.getScene());
            setText(result, R.id.level_description, level.getDescription());

            if (position == 0) {
                result.findViewById(R.id.arrow_l).setVisibility(View.INVISIBLE);
                result.findViewById(R.id.arrow_r).setVisibility(View.VISIBLE);
            } else if (position == action.getLevels().size() - 1) {
                result.findViewById(R.id.arrow_l).setVisibility(View.VISIBLE);
                result.findViewById(R.id.arrow_r).setVisibility(View.INVISIBLE);
            } else {
                result.findViewById(R.id.arrow_l).setVisibility(View.VISIBLE);
                result.findViewById(R.id.arrow_r).setVisibility(View.VISIBLE);
            }

            result.findViewById(R.id.arrow_l).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = pager.getCurrentItem();
                    if (v.getVisibility() == View.VISIBLE && index > 0) {
                        pager.setCurrentItem(index - 1);
                    }
                }
            });

            result.findViewById(R.id.arrow_r).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int index = pager.getCurrentItem();
                    if (v.getVisibility() == View.VISIBLE && index < getCount() - 1) {
                        pager.setCurrentItem(index + 1);
                    }
                }
            });


            setAchievementIndicator(result, level);

            container.addView(result);

            return result;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return action.getLevels().size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }
    }
}
