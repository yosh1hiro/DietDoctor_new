package jp.health_gate.DietDoctor.mypage;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import jp.health_gate.DietDoctor.CustomFragment;
import jp.health_gate.DietDoctor.R;
import jp.health_gate.DietDoctor.models.Achievements;
import jp.health_gate.DietDoctor.models.DietActions;

/**
 * Created by yoshihiro on 2014/10/27.
 */
class MedalDescriptionFragment extends CustomFragment {

    private static final String GROUP = "GROUP";
    private static final String LEVEL = "LEVEL";

    public static MedalDescriptionFragment newInstance(int groupId, int level) {
        MedalDescriptionFragment fragment = new MedalDescriptionFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(GROUP, groupId);
        bundle.putInt(LEVEL, level);

        fragment.setArguments(bundle);

        return fragment;
    }

    private int groupId;
    private int level;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        groupId = getArguments().getInt(GROUP);
        level = getArguments().getInt(LEVEL);
        return inflater.inflate(R.layout.medal_description_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitleText("メダル・ライブラリ");

        ViewPager pager = (ViewPager) getView().findViewById(R.id.medal_pager);
        MedalAdapter adapter = new MedalAdapter();
        pager.setAdapter(adapter);
        pager.setCurrentItem(adapter.indexOf(groupId, level));
    }

    private class MedalAdapter extends PagerAdapter {

        List<Achievements.Item> items = Achievements.sharedInstance().getItemsForMedals();

        private void setText(View view, int id, String text) {
            TextView textView = (TextView) view.findViewById(id);
            textView.setText(text);
        }

        private String starCount(int count) {
            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < count; i++) {
                builder.append("★");
            }

            return builder.toString();
        }

        private String medalStage(int stage) {
            switch (stage) {
                case 1:
                    return getString(R.string.bronze_medal);
                case 2:
                    return getString(R.string.silver_medal);
                case 3:
                    return getString(R.string.gold_medal);
                case 4:
                    return getString(R.string.master_medal);
                default:
                    return "";
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View result = getActivity().getLayoutInflater().inflate(R.layout.medal_description_page, null);

            Achievements.Item item = items.get(position);
            DietActions.LeveledItem action = DietActions.sharedInstance().findAction(item.getGroupId(), item.getLevel());
            DietActions.ChallengeItem group = DietActions.sharedInstance().findAction(item.getGroupId());

            setText(result, R.id.group_id, "No." + String.format("%02d", item.getGroupId()));
            setText(result, R.id.star_count, starCount(action.getStar()));
            setText(result, R.id.action_category, group.getCategory());
            setText(result, R.id.medal_stage, medalStage(action.getStage()));
            setText(result, R.id.medal_description, group.getDescription(action.getStage()));

            ImageView imageView = (ImageView) result.findViewById(R.id.medal_image);
            imageView.setImageDrawable(action.getIcon());

            container.addView(result);

            return result;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }


        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public int indexOf(int groupId, int level) {
            int index = 0;
            for (Achievements.Item item : items) {
                if (item.getGroupId() == groupId && item.getLevel() == level) return index;
                index++;
            }
            return 0;   //ここには来ないはず
        }
    }
//
//    private class MedalAdapter extends BaseAdapter {
//
//        List<Achievements.Item> items = new ArrayList<Achievements.Item>();
//
//        MedalAdapter() {
//            super();
//            items = Achievements.sharedInstance().getItemsForMedals();
//        }
//
//        @Override
//        public int getCount() {
//            return items.size();
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return items.get(position);
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return position;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            ViewGroup viewGroup = (ViewGroup) convertView;
//
//
//            if (viewGroup == null) {
//                viewGroup = (ViewGroup) getActivity().getLayoutInflater().inflate(R.layout.medal_item, null, false);
//            }
//            Achievements.Item item = items.get(position);
//            DietActions.LeveledItem action = DietActions.sharedInstance().findAction(item.getGroupId(), item.getLevel());
//
//            ImageView imageView = (ImageView) viewGroup.findViewById(R.id.medal_image);
//            imageView.setImageDrawable(action.getIcon());
//
//            TextView textView = (TextView) viewGroup.findViewById(R.id.medal_count);
//            textView.setText("×" + item.getMedal());
//            if (item.getMedal() > 1) {
//                textView.setVisibility(View.VISIBLE);
//            } else {
//                textView.setVisibility(View.INVISIBLE);
//            }
//
//            viewGroup.setTag(item);
//
//            return viewGroup;
//        }
//    }
}