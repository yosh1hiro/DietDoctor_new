package jp.health_gate.DietDoctor.information;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import jp.health_gate.DietDoctor.CustomFragment;
import jp.health_gate.DietDoctor.models.Achievements;
import jp.health_gate.DietDoctor.models.DietActions;
import jp.health_gate.DietDoctor.R;

import java.util.ArrayList;
import java.util.List;

/**
 * メダルライブラリのフラグメント
 * <p/>
 * Created by kazhida on 2013/10/15.
 */
class MedalLibraryFragment extends CustomFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.medal_library_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitleText("メダル・ライブラリ");

        GridView gridView = (GridView) getView().findViewById(R.id.medal_grid);
        gridView.setAdapter(new MedalAdapter());
    }

    private class MedalAdapter extends BaseAdapter {

        List<Achievements.Item> items = new ArrayList<Achievements.Item>();

        MedalAdapter() {
            super();
            items = Achievements.sharedInstance().getItemsForMedals();
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup viewGroup = (ViewGroup) convertView;


            if (viewGroup == null) {
                viewGroup = (ViewGroup) getActivity().getLayoutInflater().inflate(R.layout.medal_item, null, false);
            }
            final Achievements.Item item = items.get(position);
            final DietActions.LeveledItem action = DietActions.sharedInstance().findAction(item.getGroupId(), item.getLevel());

            ImageView imageView = (ImageView) viewGroup.findViewById(R.id.medal_image);
            imageView.setImageDrawable(action.getIcon());
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    replaceFragment(MedalDescriptionFragment.newInstance(action.getGroupId(), action.getLevel()));
                }
            });

            TextView textView = (TextView) viewGroup.findViewById(R.id.medal_count);
            textView.setText("×" + item.getMedal());
            if (item.getMedal() > 1) {
                textView.setVisibility(View.VISIBLE);
            } else {
                textView.setVisibility(View.INVISIBLE);
            }

            viewGroup.setTag(item);

            return viewGroup;
        }
    }
}
