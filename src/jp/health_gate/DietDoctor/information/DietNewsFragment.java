package jp.health_gate.DietDoctor.information;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.health_gate.DietDoctor.CustomFragment;
import jp.health_gate.DietDoctor.R;
import jp.health_gate.DietDoctor.models.DietActions;
import jp.health_gate.DietDoctor.models.DietNews;

/**
 * ダイエット通信のフラグメント
 * <p/>
 * Created by kazhida on 2013/10/15.
 */
class DietNewsFragment extends CustomFragment {

    private DietNews dietNews;
    private ListAdapter listAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.diet_news_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitleText("ダイエット通信");

        dietNews = new DietNews();

//        getView().findViewById(R.id.action_grid).setVisibility(View.GONE);
//        setSelected(R.id.whats_new_button, R.id.items_button);

        getView().findViewById(R.id.items_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showGrid();
            }
        });
        getView().findViewById(R.id.whats_new_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showList();
            }
        });

        GridAdapter gridAdapter = new GridAdapter();
        GridView gridView = (GridView) getView().findViewById(R.id.action_grid);
        gridView.setAdapter(gridAdapter);
        gridView.setOnItemClickListener(gridAdapter);

        listAdapter = new ListAdapter();
        ListView listView = (ListView) getView().findViewById(R.id.topic_list);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(listAdapter);
        listView.setTextFilterEnabled(true);

        getSearchEdit().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                setFilterText(s.toString());
            }
        });

        showGrid();
    }

    @Override
    public void onKeyboardShown() {
        getView().findViewById(R.id.bottom_buttons).setVisibility(View.GONE);
    }

    @Override
    public void onKeyboardHidden() {
        getView().findViewById(R.id.bottom_buttons).setVisibility(View.VISIBLE);
    }

    private void showGrid() {
        getView().findViewById(R.id.action_grid).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.whats_new_layout).setVisibility(View.GONE);
        setSelected(R.id.items_button, R.id.whats_new_button);
        getSearchEdit().setText(null);
        hideKeyboard();
    }

    private void showList() {
        getView().findViewById(R.id.action_grid).setVisibility(View.GONE);
        getView().findViewById(R.id.whats_new_layout).setVisibility(View.VISIBLE);
        setSelected(R.id.whats_new_button, R.id.items_button);
    }

    private void setSelected(int selectedResId, int unselectedResId) {
        getView().findViewById(unselectedResId).setSelected(false);
        getView().findViewById(selectedResId).setSelected(true);
    }

    private void setFilterText(String constraint) {
        listAdapter.getFilter().filter(constraint);
    }

    private EditText getSearchEdit() {
        return (EditText) getView().findViewById(R.id.search_text);
    }

    private class GridAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

        @Override
        public int getCount() {
            return DietActions.sharedInstance().getActions().size() + 1;
        }

        @Override
        public Object getItem(int position) {
            List<DietActions.ChallengeItem> actions = DietActions.sharedInstance().getActions();
            if (position < actions.size()) {
                return actions.get(position);
            } else {
                return null;
            }
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

            if (item != null) {
                imageView.setImageDrawable(item.getIcon());
                textView.setText(item.getCaption());
            } else {
                imageView.setImageResource(R.drawable.ic_non_category);
                textView.setText(R.string.non_category);
            }

            if (item != null) {
                DietNews.Item tips = dietNews.find(item.getGroupId());
                view.setTag(tips);

                if (dietNews.findUnread(item.getGroupId()) == null) {
                    view.findViewById(R.id.news).setVisibility(View.GONE);
                } else {
                    view.findViewById(R.id.news).setVisibility(View.VISIBLE);
                }
            } else {
                view.findViewById(R.id.news).setVisibility(View.GONE);
            }

            return view;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final DietNews.Item tips = (DietNews.Item) view.getTag();
            if (tips != null) {
                String constraint = "&Tips:" + tips.getGroupId() + ";";
                showList();
                getSearchEdit().setText(constraint);
            }
        }
    }

//    private class DummyAdapter extends ArrayAdapter<DietNews.Item> {
//
//    }

    private class ListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, Filterable {

        private SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        private int selectedIndex = -1;
        private final Object lock = new Object();
        private List<DietNews.Item> items = new ArrayList<DietNews.Item>(dietNews.getItems());
        private ListFilter filter;

        private class ListFilter extends Filter {

            final Pattern pattern = Pattern.compile("&Tips:(\\d+); ?(.*)");

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                List<DietNews.Item> items;

                if (constraint == null || constraint.length() == 0) {
                    synchronized (lock) {
                        items = new ArrayList<DietNews.Item>(dietNews.getItems());
                    }
                } else {
                    items = new ArrayList<DietNews.Item>();
                    Matcher matcher = pattern.matcher(constraint);
                    if (matcher.find()) {
                        //  チャレンジ項目で絞り込み
                        int groupId = Integer.parseInt(matcher.group(1));
                        synchronized (lock) {
                            for (DietNews.Item item : dietNews.getItems()) {
                                if (item.getGroupId() == groupId && item.match(matcher.group(2))) {
                                    items.add(item);
                                }
                            }
                        }
                    } else {
                        //  普通の文字列検索
                        synchronized (lock) {
                            for (DietNews.Item item : dietNews.getItems()) {
                                if (item.match(constraint)) {
                                    items.add(item);
                                }
                            }
                        }
                    }
                }

                results.values = items;
                results.count = items.size();

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                synchronized (lock) {
                    if (results.values instanceof List<?>) {
                        items.clear();

                        List<?> list = (List<?>) results.values;
                        for (Object item : list) {
                            items.add((DietNews.Item) item);
                        }
                    }
                }
                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
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
                viewGroup = (ViewGroup) getActivity().getLayoutInflater().inflate(R.layout.topic_item, null, false);
            }

            DietNews.Item item = items.get(position);

            getTextView(viewGroup, R.id.title_text).setText(item.getTitle());
            getTextView(viewGroup, R.id.datetime_text).setText(format.format(item.getDate().getTime()));
            if (position == selectedIndex) {
                getTextView(viewGroup, R.id.content_text).setText(item.getContent());
            } else {
                getTextView(viewGroup, R.id.content_text).setText(excerpt(item.getContent()));
            }
            getTextView(viewGroup, R.id.remark_text).setText(R.string.unread);      //DDクリニックのトピックを流用しているので、文字列を差し替える

            if (item.isOpened()) {
                viewGroup.findViewById(R.id.remark_text).setVisibility(View.INVISIBLE);
            } else {
                viewGroup.findViewById(R.id.remark_text).setVisibility(View.VISIBLE);
            }

            viewGroup.setTag(item);

            return viewGroup;
        }

        TextView getTextView(View viewGroup, int resId) {
            return (TextView) viewGroup.findViewById(resId);
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            DietNews.Item item = (DietNews.Item) view.getTag();

            if (selectedIndex == position) {
                selectedIndex = -1;
            } else {
                selectedIndex = position;
            }
            item.setRead();
            view.findViewById(R.id.remark_text).setVisibility(View.INVISIBLE);

            hideKeyboard();

            notifyDataSetChanged();
        }

        @Override
        public Filter getFilter() {
            if (filter == null) filter = new ListFilter();
            return filter;
        }
    }
}
