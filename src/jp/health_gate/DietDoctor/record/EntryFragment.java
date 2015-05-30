package jp.health_gate.DietDoctor.record;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

import jp.health_gate.DietDoctor.CalendarUtils;
import jp.health_gate.DietDoctor.CustomFragment;
import jp.health_gate.DietDoctor.R;
import jp.health_gate.DietDoctor.models.Achievements;
import jp.health_gate.DietDoctor.models.ActiveUser;
import jp.health_gate.DietDoctor.models.DietActions;
import jp.health_gate.DietDoctor.models.DietNews;
import jp.health_gate.DietDoctor.models.Weights;

/**
 * 記録をつけるためのフラグメント
 * <p/>
 * Created by kazhida on 2013/10/07.
 */
public class EntryFragment extends CustomFragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    public static EntryFragment newInstance(int sectionNumber) {
        EntryFragment fragment = new EntryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public EntryFragment() {

    }

    private static final String ACHIEVEMENT_DATE_ = "ACHIEVEMENT_DATE_";
    private Calendar today = Calendar.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.entry_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitleText(R.string.entry_title);

        View root = getView();
/*
        root.findViewById(R.id.diet_action_button_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incAchievement(0);
            }
        });
        root.findViewById(R.id.diet_action_button_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incAchievement(1);
            }
        });
        root.findViewById(R.id.diet_action_button_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incAchievement(2);
            }
        });
        */

        //todo: デバッグ用なので後で外す
        root.findViewById(R.id.diet_action_button_1).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return removeAchievementForDebug(0);
            }
        });
        root.findViewById(R.id.diet_action_button_2).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return removeAchievementForDebug(1);
            }
        });
        root.findViewById(R.id.diet_action_button_3).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return removeAchievementForDebug(2);
            }
        });

        Weights.Item weightItem = Weights.sharedInstance().getWeight(today);
        if (weightItem != null) {
            setWeight(R.id.edit_weight, weightItem.getWeight());
            setText(R.id.edit_memo, weightItem.getMemo());
        }

        final EditText weightEdit = (EditText) root.findViewById(R.id.edit_weight);
        final EditText memoEdit = (EditText) root.findViewById(R.id.edit_memo);

        weightEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextDialog(R.string.weight_of_today, R.id.edit_weight, new Runnable() {
                    @Override
                    public void run() {
                        recordWeight();
                    }
                });
            }
        });

        /*
        * ManagementFragmentへ遷移するボタン設定
        * */

        memoEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextDialog(R.string.memo, R.id.edit_memo, new Runnable() {
                    @Override
                    public void run() {
                        recordWeight();
                    }
                });
            }
        });

        root.findViewById(R.id.management_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), jp.health_gate.DietDoctor.management.ManagementActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //表示の初期化を遅延させる
                initAction(0);
                initAction(1);
                initAction(2);

                setButtonColor(0);
                setButtonColor(1);
                setButtonColor(2);
            }
        }, 50);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String title = preferences.getString(DietNews.TIPS_TITLE, null);
        String content = preferences.getString(DietNews.TIPS_CONTENT, null);
        if (title != null && content != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.remove(DietNews.TIPS_TITLE);
            editor.remove(DietNews.TIPS_CONTENT);
            editor.commit();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(title);
            builder.setMessage(content);
            builder.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(getActivity(), R.string.msg_new_tips, Toast.LENGTH_LONG).show();
                }
            });
            builder.create().show();
        }
    }

    @Override
    public void onPause() {
        final EditText weightEdit = (EditText) getView().findViewById(R.id.edit_weight);
        final EditText memoEdit = (EditText) getView().findViewById(R.id.edit_memo);

        float weight = Weights.parseWeight(weightEdit.getText().toString());
        String memo = memoEdit.getText().toString().trim();

        Weights.sharedInstance().addWeight(today, weight, memo);

        super.onPause();
    }

    private void initAction(int index) {
        int id;
        switch (index) {
            case 0:
                id = R.id.diet_action_1;
                break;
            case 1:
                id = R.id.diet_action_2;
                break;
            case 2:
                id = R.id.diet_action_3;
                break;
            default:
                return;
        }
        Activity activity = getActivity();
        final DietActions.LeveledItem action = ActiveUser.sharedInstance().getAction(index);
        ViewGroup viewGroup = (ViewGroup) activity.findViewById(id);

        if (action != null) {
            setIcon(viewGroup, R.id.action_icon, action.getIcon());
            setCaption(viewGroup, R.id.action_title, action.getTitle());
            setCaption(viewGroup, R.id.action_level, getString(R.string.level_prefix) + action.getLevel());
            setCaption(viewGroup, R.id.action_star_count, getString(R.string.star_prefix) + action.getStar());
            setAchievementIndicator(viewGroup, action);
            viewGroup.findViewById(R.id.action_icon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTips(action);
                }
            });
        } else {
            setCaption(viewGroup, R.id.action_title, null);
            setCaption(viewGroup, R.id.action_level, null);
            setCaption(viewGroup, R.id.action_star_count, null);
        }
    }

    private void setButtonColor(int index) {
        TextView textView = null;
        switch (index) {
            case 0:
                textView = (TextView) getView().findViewById(R.id.diet_action_button_1);
                break;
            case 1:
                textView = (TextView) getView().findViewById(R.id.diet_action_button_2);
                break;
            case 2:
                textView = (TextView) getView().findViewById(R.id.diet_action_button_3);
                break;
        }
        if (textView != null) {
            if (isAchievedByDatabase(index)) {
                textView.setText(R.string.done);
                if (isAchievedByPreferences(index)) {
                    textView.setBackgroundResource(R.drawable.over_button);
                } else {
                    textView.setBackgroundResource(R.drawable.standard_button);
                }
            } else {
                textView.setText(R.string.not_doing);
                if (isAchievedByPreferences(index)) {
                    textView.setBackgroundResource(R.drawable.select_button);
                } else {
                    textView.setBackgroundResource(R.drawable.standard_button);
                }
            }
            int padding = (int) (8 * getResources().getDisplayMetrics().density);
            textView.setPadding(padding, padding, padding, padding);
        }
    }

    private void setCaption(ViewGroup viewGroup, int id, String text) {
        TextView textView = (TextView) viewGroup.findViewById(id);
        textView.setText(text);
    }

    private void setIcon(ViewGroup viewGroup, int id, Drawable icon) {
        ImageView imageView = (ImageView) viewGroup.findViewById(id);
        imageView.setImageDrawable(icon);
    }

    private boolean isAchievedByPreferences(int index) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        long achievedDate = preferences.getLong(ACHIEVEMENT_DATE_ + index, 0);
        return achievedDate == dateSerial(today.getTime());
    }

    private boolean isAchievedByDatabase(int index) {
        DietActions.LeveledItem action = ActiveUser.sharedInstance().getAction(index);
        if (action != null) {
            List<Achievements.Item> items = Achievements.sharedInstance().load(CalendarUtils.dateCalendar());
            for (Achievements.Item item : items) {
                if (item.getGroupId() == action.getGroupId() &&
                        item.getLevel() == action.getLevel() &&
                        item.getStar() > 0) return true;
            }
        }
        return false;
    }

    private void recordWeight() {
        final EditText weightEdit = (EditText) getView().findViewById(R.id.edit_weight);
        final EditText memoEdit = (EditText) getView().findViewById(R.id.edit_memo);

        float weight = Weights.parseWeight(weightEdit.getText().toString());
        String memo = memoEdit.getText().toString();

        Weights.sharedInstance().addWeight(today, weight, memo);
        showProfile();
    }
/*
    private void incAchievement(int index) {
        DietActions.LeveledItem action = ActiveUser.sharedInstance().getAction(index);
        if (action != null && !isAchievedByPreferences(index)) {
            action.incAchievement(today);
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
            editor.putLong(ACHIEVEMENT_DATE_ + index, dateSerial(today.getTime()));
            editor.commit();
            if (action.getAchievement() == action.getAchievementMax()) {
                replaceFragment(AchievementFragment.newInstance(index));
            }
            initAction(index);
            setButtonColor(index);
            showProfile(R.id.star_panel);
        }
    } */

    private void showTips(DietActions.LeveledItem action) {
        DietNews.Item news = DietNews.getLevelTips(action.getGroupId(), action.getLevel());
        if (news != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(news.getTitle());
            builder.setMessage(news.getContent());
            builder.setPositiveButton(R.string.close, null);

            builder.create().show();
        }
    }

    private boolean removeAchievementForDebug(int index) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        editor.remove(ACHIEVEMENT_DATE_ + index);
        editor.commit();
//        setButtonColor(index);
        return true;
    }
}
