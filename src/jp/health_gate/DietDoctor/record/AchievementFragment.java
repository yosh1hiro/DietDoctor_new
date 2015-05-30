package jp.health_gate.DietDoctor.record;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import jp.health_gate.DietDoctor.CustomFragment;
import jp.health_gate.DietDoctor.R;
import jp.health_gate.DietDoctor.contact.ContactActivity;
import jp.health_gate.DietDoctor.management.ManagementActivity;
import jp.health_gate.DietDoctor.models.Achievements;
import jp.health_gate.DietDoctor.models.ActiveUser;
import jp.health_gate.DietDoctor.models.DietActions;
import jp.health_gate.DietDoctor.models.DietNews;
import jp.health_gate.DietDoctor.models.Weights;

/**
 * 目標達成画面
 * <p/>
 * Created by kazhida on 2013/10/10.
 */
class AchievementFragment extends CustomFragment {

    private static final String INDEX = "INDEX";
    private int goalIndex;
    private DietActions.LeveledItem action;

    static AchievementFragment newInstance(int index) {
        AchievementFragment fragment = new AchievementFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(INDEX, index);

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        goalIndex = getArguments().getInt(INDEX);
        action = ActiveUser.sharedInstance().getAction(goalIndex);
        return inflater.inflate(R.layout.achievement_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitleText(R.string.entry_title);

        if (action == null) {
            //  不正に呼ばれたので、終了
            getFragmentManager().popBackStack();
        } else {
            View root = getView();

            initAction();
            initCaptions();
            initMedal();

            root.findViewById(R.id.next_level_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ActiveUser.sharedInstance().incActionLevel(goalIndex)) {
                        //  レベルを上げたら、一つ戻る
                        DietActions.LeveledItem item = ActiveUser.sharedInstance().getAction(goalIndex);
                        if (item != null) {
                            DietNews.Item tips = DietNews.getLevelTips(item.getGroupId(), item.getLevel());
                            if (tips != null) {
                                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                                editor.putString(DietNews.TIPS_TITLE, tips.getTitle());
                                editor.putString(DietNews.TIPS_CONTENT, tips.getContent());
                                editor.commit();
                            }
                        }
                        getFragmentManager().popBackStack();
                    } else {
                        Toast.makeText(getActivity(), R.string.msg_level10, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            root.findViewById(R.id.change_action_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //  一つ空欄にして、目標設定に戻る
                    ActiveUser.sharedInstance().removeAction(goalIndex);

                    Intent intent = new Intent(getActivity(), ManagementActivity.class);
                    intent.putExtra(ManagementActivity.ROOT_FRAGMENT, true);
                    startActivity(intent);
                }
            });
            root.findViewById(R.id.consultingButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), ContactActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
/*
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.achievement_comment);
        builder.setMessage(action.getAchievementComment());
        builder.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showProfile(R.id.medal_panel);
            }
        });

        builder.create().show();
        */
    }

    private void initAction() {
        ViewGroup viewGroup = (ViewGroup) getView();

        setIcon(viewGroup, R.id.action_icon, action.getIcon());
        setCaption(viewGroup, R.id.action_title, action.getTitle());
        setCaption(viewGroup, R.id.action_level, getString(R.string.level_prefix) + action.getLevel());
        setCaption(viewGroup, R.id.action_star_count, getString(R.string.star_prefix) + action.getStar());
        setAchievementIndicator(viewGroup, action);
    }

    private void initCaptions() {
        List<Achievements.Item> items = Achievements.sharedInstance().getAchievements(action.getGroupId(), action.getLevel());

        if (items.size() > 0) {
            int stars = 0;
            for (Achievements.Item item : items) {
                stars += item.getStar();
            }
            setValue(R.id.star_count, stars);

            Achievements.Item first = items.get(0);
            Achievements.Item last = items.get(items.size() - 1);

            Weights.Item firstWeight = Weights.sharedInstance().getRecentWeight(first.getDate());
            Weights.Item lastWeight = Weights.sharedInstance().getRecentWeight(last.getDate());

            if (firstWeight != null && lastWeight != null) {
                setWeight(R.id.weight_loss, firstWeight.getWeight(), lastWeight.getWeight());
            } else {
                setWeight(R.id.weight_loss, 0);
            }

            long diff = last.getDate().getTimeInMillis() - first.getDate().getTimeInMillis();
            diff /= 1000L * 60 * 60 * 24;
            if (diff < 1) diff = 1;
            long pace = action.getAchievementMax() * 3; //推奨ペースは最短ペースの3倍

            setRate(R.id.pace_rate, R.id.pace_unit, pace, diff);
        } else {
            setValue(R.id.star_count, 0);
            setWeight(R.id.weight_loss, 0);
            setText(R.id.pace_rate, "--");
        }
    }

    private void initMedal() {
        setIcon(R.id.medal_image, action.getIcon());

        DietActions.ChallengeItem parent = DietActions.sharedInstance().findAction(action.getGroupId());

        setText(R.id.action_caption, parent.getCaption());
        setValue(R.id.level_caption, action.getLevel());
        switch (action.getStage()) {
            case 1:
                setText(R.id.stage_caption, getString(R.string.bronze), getResources().getColor(R.color.bronze));
                break;
            case 2:
                setText(R.id.stage_caption, getString(R.string.silver), getResources().getColor(R.color.silver));
                break;
            case 3:
                setText(R.id.stage_caption, getString(R.string.gold), getResources().getColor(R.color.gold));
                break;
            case 4:
                setText(R.id.stage_caption, getString(R.string.master), getResources().getColor(R.color.gold));
                break;
            default:
                setText(R.id.stage_caption, null);
                break;
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
}
