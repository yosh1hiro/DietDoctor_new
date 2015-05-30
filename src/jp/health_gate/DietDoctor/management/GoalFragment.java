package jp.health_gate.DietDoctor.management;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.haarman.supertooltips.ToolTipRelativeLayout;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;
import jp.health_gate.DietDoctor.*;
import jp.health_gate.DietDoctor.models.ActiveUser;
import jp.health_gate.DietDoctor.models.Backend;
import jp.health_gate.DietDoctor.models.DietActions;
import jp.health_gate.DietDoctor.models.DietNews;

/**
 * 目標設定のフラグメント
 * <p/>
 * Created by kazhida on 2013/10/07.
 */
class GoalFragment extends CustomFragment {

    private Scenario scenario;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.goal_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitleText(R.string.goal_title);

        View root = getView();

        root.findViewById(R.id.diet_action_button_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scenario != null) scenario.end();
                replaceFragment(SelectActionFragment.newInstance(0));
            }
        });
        root.findViewById(R.id.diet_action_button_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scenario != null) scenario.end();
                replaceFragment(SelectActionFragment.newInstance(1));
            }
        });
        root.findViewById(R.id.diet_action_button_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scenario != null) scenario.end();
                replaceFragment(SelectActionFragment.newInstance(2));
            }
        });
        root.findViewById(R.id.diet_action_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scenario != null) scenario.end();
                replaceFragment(SelectActionFragment.newInstance(0));
            }
        });
        root.findViewById(R.id.diet_action_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scenario != null) scenario.end();
                replaceFragment(SelectActionFragment.newInstance(1));
            }
        });
        root.findViewById(R.id.diet_action_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scenario != null) scenario.end();
                replaceFragment(SelectActionFragment.newInstance(2));
            }
        });

        final Backend.Profile profile = ActiveUser.sharedInstance().getProfile();

        final EditText goal = (EditText) root.findViewById(R.id.edit_diet_purpose);
        goal.setText(profile.getPurpose());
        goal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextDialog(R.string.diet_purpose, R.id.edit_diet_purpose, new Runnable() {
                    @Override
                    public void run() {
                        if (scenario != null) scenario.step(1);
                        profile.setPurpose(goal.getText().toString());
                        updateProfile(profile);
                    }
                });
            }
        });

        final EditText target = (EditText) root.findViewById(R.id.edit_target_weight);
        target.setText(String.format("%1.1f", profile.getTargetWeight()));
        target.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextDialog(R.string.target_weight, R.id.edit_target_weight, new Runnable() {
                    @Override
                    public void run() {
                        if (scenario != null) scenario.step(2);
                        profile.setTargetWeight(target.getText().toString());
                        updateProfile(profile);
                    }
                });
            }
        });

//        if (profile.getPurpose() == null || profile.getPurpose().length() == 0) {
        ToolTipRelativeLayout layout = (ToolTipRelativeLayout) getActivity().findViewById(R.id.tooltip_layout);
        int color = getResources().getColor(R.color.bg_tutorial);
        scenario = new Scenario(layout, color);
        scenario.setParentView(getView());
        scenario.addStep(1000, R.string.tutorial_purpose, R.id.edit_diet_purpose);
        scenario.addStep(500, R.string.tutorial_goal, R.id.edit_target_weight);
        scenario.addStep(500, R.string.tutorial_set_action, R.id.diet_action_button_1);
        scenario.step(0);
//        }
    }

    @Override
    public void onPause() {
        if (scenario != null) scenario.end();

        final Backend.Profile profile = ActiveUser.sharedInstance().getProfile();
        final EditText goal = (EditText) getView().findViewById(R.id.edit_diet_purpose);
        final EditText target = (EditText) getView().findViewById(R.id.edit_target_weight);

        profile.setPurpose(goal.getText().toString());
        profile.setTargetWeight(target.getText().toString());

        updateProfile(profile);

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //表示の初期化を遅延させる
                initAction(0, R.id.diet_action_1);
                initAction(1, R.id.diet_action_2);
                initAction(2, R.id.diet_action_3);
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

    private void initAction(int index, int id) {
        Activity activity = getActivity();
        DietActions.LeveledItem action = ActiveUser.sharedInstance().getAction(index);
        ViewGroup viewGroup = (ViewGroup) activity.findViewById(id);

        if (action != null) {
            setIcon(viewGroup, R.id.action_icon, action.getIcon());
            setCaption(viewGroup, R.id.action_title, action.getTitle());
            setCaption(viewGroup, R.id.action_level, getString(R.string.level_prefix) + action.getLevel());
            setCaption(viewGroup, R.id.action_star_count, getString(R.string.star_prefix) + action.getStar());
            setAchievementIndicator(viewGroup, action);
        } else {
            setCaption(viewGroup, R.id.action_title, null);
            setCaption(viewGroup, R.id.action_level, null);
            setCaption(viewGroup, R.id.action_star_count, null);
        }

        //  場所が狭いので、この3つは非表示
        viewGroup.findViewById(R.id.action_icon).setVisibility(View.GONE);
        viewGroup.findViewById(R.id.action_star).setVisibility(View.GONE);
    }

    private void updateProfile(Backend.Profile profile) {
        ActiveUser.sharedInstance().updateProfile(profile, new KinveyUserCallback() {
            @Override
            public void onSuccess(User user) {
                showProfile();
            }

            @Override
            public void onFailure(Throwable throwable) {
                Toast.makeText(getActivity(), R.string.err_cannot_sent_to_server, Toast.LENGTH_SHORT).show();
            }
        });
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
