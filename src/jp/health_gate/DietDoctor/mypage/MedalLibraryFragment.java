package jp.health_gate.DietDoctor.mypage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import jp.health_gate.DietDoctor.CustomFragment;
import jp.health_gate.DietDoctor.R;
import jp.health_gate.DietDoctor.models.Achievements;
import jp.health_gate.DietDoctor.models.DietActions;

/**
 * Created by yoshihiro on 2014/10/27.
 */
public class MedalLibraryFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.medal_library_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        /*setTitleText("メダル・ライブラリ"); */



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
    protected void replaceFragment(CustomFragment fragment) {
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(
                R.anim.fragment_push_enter,
                R.anim.fragment_push_exit);
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
