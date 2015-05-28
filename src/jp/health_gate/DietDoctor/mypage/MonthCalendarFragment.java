package jp.health_gate.DietDoctor.mypage;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Calendar;

import jp.health_gate.DietDoctor.CalendarUtils;
import jp.health_gate.DietDoctor.R;
import jp.health_gate.DietDoctor.models.Achievements;
import jp.health_gate.DietDoctor.models.Weights;

/**
 * Created by yoshihiro on 2014/10/27.
 */

public class MonthCalendarFragment extends Fragment {

    //    private Weights.Item currentWeight;
    private ViewPager pager;
    private Calendar month = CalendarUtils.monthCalendar();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.month_calendar_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View root = getView();

        pager = (ViewPager) root.findViewById(R.id.calendar_pager);
        pager.setAdapter(new MonthPagerAdapter());

//        root.findViewById(R.id.memo_button).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (currentWeight != null) {
//                    Toast.makeText(getActivity(), currentWeight.getMemo(), Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

//        showDietAction(Calendar.getInstance());
    }

    @Override
    public void onResume() {
        super.onResume();
        pager.setCurrentItem(MonthPagerAdapter.INITIAL_PAGE_NUM);
    }

//    private void showDietAction(final Calendar date) {
//        currentWeight = Weights.sharedInstance().getRecentWeight(date);
//        if (currentWeight == null) {
//            setText(R.id.weight_text, null);
//        } else {
//            setWeight(R.id.weight_text, currentWeight.getWeight());
//        }
//
//        List<Achievements.Item> achievements = Achievements.sharedInstance().load(date);
//
//        int stars = 0;
//        for (Achievements.Item item : achievements) {
//            stars += item.getStar();
//        }
//        setValue(R.id.star_text, stars);
//
//        float density = getResources().getDisplayMetrics().density;
//
//        ViewGroup medals = (ViewGroup) getView().findViewById(R.id.medal_layout);
//        medals.removeAllViews();
//        for (Achievements.Item item : achievements) {
//            if (item.getMedal() > 0) {
//                DietActions.LeveledItem action = DietActions.sharedInstance().findAction(item.getGroupId(), item.getLevel());
//                if (action != null) {
//                    ImageView imageView = new ImageView(getActivity());
//                    int size = (int) (16 * density);
//                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
//                    params.gravity = Gravity.CENTER_VERTICAL;
//                    imageView.setLayoutParams(params);
//                    imageView.setImageDrawable(action.getIcon());
//                    medals.addView(imageView);
//                }
//            }
//        }
//
//        if (currentWeight != null && currentWeight.hasMemo() && currentWeight.isSameDay(date)) {
//            getView().findViewById(R.id.memo_button).setVisibility(View.VISIBLE);
//        } else {
//            getView().findViewById(R.id.memo_button).setVisibility(View.INVISIBLE);
//        }
//
//        getView().findViewById(R.id.day_info_panel).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                FragmentManager manager = getFragmentManager();
//                ActionOfTheDayFragment dialog = ActionOfTheDayFragment.newInstance(date);
//                dialog.show(manager, null);
//            }
//        });
//    }

//    private void setValue(int id, int value) {
//        setText(id, "" + value);
//    }
//
//    private void setWeight(int id, float value) {
//        if (value == 0) {
//            setText(id, "--");
//        } else {
//            setText(id, String.format("%1.1f", value));
//        }
//    }

//    private void setText(int id, String text) {
//        ViewGroup viewGroup = (ViewGroup) getView();
//        if (viewGroup != null) {
//            TextView textView = (TextView) viewGroup.findViewById(id);
//            if (textView != null) {
//                textView.setText(text);
//            }
//        }
//    }

//    private static final String OFFSET = "OFFSET";

    private class MonthPagerAdapter extends PagerAdapter {
        public static final int INITIAL_PAGE_NUM = 999;     //3で割りきれる数
        public static final int MAX_PAGE_NUM = 2000;


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View root = getActivity().getLayoutInflater().inflate(R.layout.month_calendar_page, null);

            int offset = position - INITIAL_PAGE_NUM;
            Calendar calendar = CalendarUtils.dateCalendar(month);
            calendar.add(Calendar.MONTH, offset);

            final CalendarView calendarView = (CalendarView) root.findViewById(R.id.calendar_view);
            calendarView.setSource(Weights.sharedInstance(), Achievements.sharedInstance());
            calendarView.setMonth(calendar);
            calendarView.setCallback(new CalendarView.Callback() {
                @Override
                public void onPrevMonth(Calendar month) {
                    pager.setCurrentItem(pager.getCurrentItem() - 1);
                }

                @Override
                public void onNextMonth(Calendar month) {
                    pager.setCurrentItem(pager.getCurrentItem() + 1);
                }

                @Override
                public void onPickDate(Calendar date) {
//                    showDietAction(date);
                    FragmentManager manager = getFragmentManager();
                    ActionOfTheDayFragment dialog = ActionOfTheDayFragment.newInstance(date);
                    dialog.show(manager, null);
                }
            });

            root.findViewById(R.id.arrow_l).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pager.setCurrentItem(pager.getCurrentItem() - 1);
                }
            });

            root.findViewById(R.id.arrow_r).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pager.setCurrentItem(pager.getCurrentItem() + 1);
                }
            });

            container.addView(root);

            return root;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return MAX_PAGE_NUM;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }


//    private class MonthCalendarAdapter extends FragmentPagerAdapter {
//        public static final int INITIAL_PAGE_NUM = 999;     //3で割りきれる数
//        public static final int MAX_PAGE_NUM = 2000;
//
//        public MonthCalendarAdapter(FragmentManager fm) {
//            super(fm);
//        }
//
//        @Override
//        public Fragment getItem(int position) {
//            int offset = position - INITIAL_PAGE_NUM;
//            Calendar calendar = CalendarUtils.dateCalendar(month);
//            calendar.add(Calendar.MONTH, offset);
//
//            Fragment fragment = new MonthPageFragment();
//
//            Bundle bundle = new Bundle();
//            bundle.putInt(OFFSET, offset);
//
//            fragment.setArguments(bundle);
//
//            return fragment;
//        }
//
//        @Override
//        public int getCount() {
//            return MAX_PAGE_NUM;
//        }
//    }
//
//    private class MonthPageFragment extends Fragment {
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//            return inflater.inflate(R.layout.month_calendar_page, container, false);
//        }
//
//        @Override
//        public void onActivityCreated(Bundle savesInstanceState) {
//            super.onActivityCreated(savesInstanceState);
//
//            int offset = getArguments().getInt(OFFSET);
//            Calendar calendar = CalendarUtils.dateCalendar(month);
//            calendar.add(Calendar.MONTH, offset);
//
//            View root = getView();
//
//            final CalendarView calendarView = (CalendarView) root.findViewById(R.id.calendar_view);
//            calendarView.setSource(Weights.sharedInstance(), Achievements.sharedInstance());
//            calendarView.setMonth(calendar);
//            calendarView.setCallback(new CalendarView.Callback() {
//                @Override
//                public void onPrevMonth(Calendar month) {
//                    pager.setCurrentItem(pager.getCurrentItem() - 1);
//                }
//
//                @Override
//                public void onNextMonth(Calendar month) {
//                    pager.setCurrentItem(pager.getCurrentItem() + 1);
//                }
//
//                @Override
//                public void onPickDate(Calendar date) {
////                    showDietAction(date);
//                    FragmentManager manager = getFragmentManager();
//                    ActionOfTheDayFragment dialog = ActionOfTheDayFragment.newInstance(date);
//                    dialog.show(manager, null);
//                }
//            });
//
//            root.findViewById(R.id.arrow_l).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    pager.setCurrentItem(pager.getCurrentItem() - 1);
//                }
//            });
//
//            root.findViewById(R.id.arrow_r).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    pager.setCurrentItem(pager.getCurrentItem() + 1);
//                }
//            });
//        }
//    }
}
