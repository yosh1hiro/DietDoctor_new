package jp.health_gate.DietDoctor.contact;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;

import jp.health_gate.DietDoctor.CustomFragment;
import jp.health_gate.DietDoctor.R;
import jp.health_gate.DietDoctor.models.Counsels;

/**
 * 相談リストのフラグメント
 * <p/>
 * Created by kazhida on 2013/10/15.
 */
class TopicListFragment extends CustomFragment {

    private ListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.topic_list_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitleText(R.string.contact_title);

        adapter = new ListAdapter();

        ListView listView = (ListView) getView().findViewById(R.id.topic_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                replaceFragment(TopicDetailFragment.newInstance(position));
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Counsels.sharedInstance().load(new Counsels.Callback() {
            @Override
            public void onSuccess() {
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure() {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private class ListAdapter extends BaseAdapter {

        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");

        @Override
        public int getCount() {
            return Counsels.sharedInstance().getItems().size();
        }

        @Override
        public Object getItem(int position) {
            return Counsels.sharedInstance().getItems().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewGroup viewGroup = (ViewGroup) convertView;

            if (viewGroup == null) {
                viewGroup = (ViewGroup) getActivity().getLayoutInflater().inflate(R.layout.topic_item, null, false);
            }

            Counsels.Item item = Counsels.sharedInstance().getItems().get(position);
            Counsels.Comment comment = item.getComments().get(0);

            getTextView(viewGroup, R.id.title_text).setText(item.title);
            getTextView(viewGroup, R.id.datetime_text).setText(format.format(comment.getTime().getTime()));
            getTextView(viewGroup, R.id.content_text).setText(excerpt(comment.getComment()));

            if (item.isAnswered()) {
                viewGroup.findViewById(R.id.remark_text).setVisibility(View.VISIBLE);
            } else {
                viewGroup.findViewById(R.id.remark_text).setVisibility(View.INVISIBLE);
            }

            viewGroup.setTag(item);

            return viewGroup;
        }


        TextView getTextView(ViewGroup viewGroup, int resId) {
            return (TextView) viewGroup.findViewById(resId);
        }
    }
}
