package jp.health_gate.DietDoctor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import jp.health_gate.DietDoctor.contact.ContactActivity;
import jp.health_gate.DietDoctor.information.InformationActivity;
import jp.health_gate.DietDoctor.message.MessageActivity;
import jp.health_gate.DietDoctor.models.Achievements;
import jp.health_gate.DietDoctor.models.ActiveUser;
import jp.health_gate.DietDoctor.models.Backend;
import jp.health_gate.DietDoctor.models.DietActions;
import jp.health_gate.DietDoctor.mypage.MyPageActivity;
import jp.health_gate.DietDoctor.record.RecordActivity;

/**
 * メインページの共通部分を実装した抽象クラス
 * <p/>
 * Created by kazhida on 2013/10/07.
 */
abstract public class CustomFragment extends Fragment implements KeyboardDetectLayout.OnKeyboardDetectListener {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View footer = getView().findViewById(R.id.footer_tabs);

        if (footer != null) {
            highlightTab();
            footer.findViewById(R.id.tab_mypage).setOnClickListener(new TabClickListener(CustomActivity.Category.MYPAGE));
            footer.findViewById(R.id.tab_message).setOnClickListener(new TabClickListener(CustomActivity.Category.MESSAGE));
            footer.findViewById(R.id.tab_record).setOnClickListener(new TabClickListener(CustomActivity.Category.RECORD));
            footer.findViewById(R.id.tab_contact).setOnClickListener(new TabClickListener(CustomActivity.Category.CONTACT));
            footer.findViewById(R.id.tab_information).setOnClickListener(new TabClickListener(CustomActivity.Category.INFORMATION));
        }
/*
        View proButton = getView().findViewById(R.id.upgrade_button);
        if (proButton != null) {
            proButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    replaceFragment(new AdProFragment());
                }
            });
        }
        */
    }

    protected void highlightTab() {
        View footer = getView().findViewById(R.id.footer_tabs);

        if (footer != null) {
            ImageView mypage = (ImageView) footer.findViewById(R.id.tab_mypage);
            ImageView message = (ImageView) footer.findViewById(R.id.tab_message);
            ImageView record = (ImageView) footer.findViewById(R.id.tab_record);
            ImageView contact = (ImageView) footer.findViewById(R.id.tab_contact);
            ImageView information = (ImageView) footer.findViewById(R.id.tab_information);

            mypage.setSelected(false);
            message.setSelected(false);
            record.setSelected(false);
            contact.setSelected(false);
            information.setSelected(false);


            CustomActivity activity = (CustomActivity) getActivity();
            switch (activity.getCategory()) {
                case MYPAGE:
                    mypage.setSelected(true);
                    break;
                case MESSAGE:
                    message.setSelected(true);
                    break;
                case RECORD:
                    record.setSelected(true);
                    break;
                case CONTACT:
                    contact.setSelected(true);
                    break;
                case INFORMATION:
                    information.setSelected(true);
                    break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        showProfile();
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                showProfile();
//            }
//        }, 20);
    }

    protected void showProfile() {
        showProfile(0);
    }

    protected void showProfile(int id) {

        Backend.Profile profile = ActiveUser.sharedInstance().getProfile();
        if (id > 0) {
            Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.achievement_highlight);
            View view = getView().findViewById(id);
            view.startAnimation(animation);

            Achievements achievements = Achievements.sharedInstance();
            profile.setAchievementInfo(achievements.getStarCount(), achievements.getMedalCount());

        }
/*
        //  ヘッダ部分の表示
        setText(R.id.user_name, profile.getUsername());
        setText(R.id.date, nowAsString());
        setText(R.id.goal, profile.getPurpose());

        float recent = Weights.sharedInstance().getRecentWeight();
        float lastWeek = Weights.sharedInstance().getLastWeekWeight();
        float target = profile.getTargetWeight();

        setWeight(R.id.header_weight, recent);
        setWeight(R.id.header_weight_diff_with_week, recent, lastWeek);
        setWeight(R.id.header_weight_diff_with_target, recent, target);

        setValue(R.id.header_medal_count, profile.getMedalCount());
        setValue(R.id.header_star_count, profile.getStarCount());
        */
    }

    private String nowAsString() {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd(EEE)");
        return format.format(Calendar.getInstance().getTime());
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

    protected void setValue(int id, int value) {
        setText(id, "" + value);
    }

    protected void setRate(int valueId, int unitId, long v0, long v1) {
        if (v1 == 0) {
            setText(valueId, "--");
        } else if (v0 < v1) {
            setText(valueId, "" + (v0 * 100 / v1));
            setText(unitId, getString(R.string.percent));
        } else {
            setText(valueId, "" + (v0 / v1));
            setText(unitId, getString(R.string.achievement_pace_suffix));
        }
    }

    protected void setWeight(int id, float value) {
        if (value == 0) {
            setText(id, "--");
        } else {
            setText(id, String.format("%1.1f", value));
        }
    }

    protected void setWeight(int id, float value1, float value2) {
        if (value1 == 0 || value2 == 0) {
            setText(id, "--");
        } else {
            setText(id, "" + String.format("%1.1f", value1 - value2));
        }
    }

    protected void setText(int id, String text) {
        ViewGroup viewGroup = (ViewGroup) getView();
        if (viewGroup != null) {
            TextView textView = (TextView) viewGroup.findViewById(id);
            if (textView != null) {
                textView.setText(text);
            }
        }
    }

    protected void setText(int id, String text, int color) {
        ViewGroup viewGroup = (ViewGroup) getView();
        if (viewGroup != null) {
            TextView textView = (TextView) viewGroup.findViewById(id);
            if (textView != null) {
                textView.setText(text);
                textView.setTextColor(color);
            }
        }
    }

    protected void setTitleText(String text) {
        setText(R.id.title_text, text);
    }

    protected void setTitleText(int resId) {
        setTitleText(getString(resId));
    }

    protected void setIcon(int id, Drawable icon) {
        ImageView imageView = (ImageView) getView().findViewById(id);
        imageView.setImageDrawable(icon);
    }

    protected void setAchievementIndicator(View parent, DietActions.LeveledItem action) {
        ImageView imageView = (ImageView) parent.findViewById(R.id.action_achievement);
        switch (action.getAchievementMax()) {
            case 1:
                switch (action.getAchievement()) {
                    case 0:
                        imageView.setImageResource(R.drawable.achievement_indicator_1_0);
                        break;
                    default:
                        imageView.setImageResource(R.drawable.achievement_indicator_1_1);
                        break;
                }
                break;
            case 2:
                switch (action.getAchievement()) {
                    case 0:
                        imageView.setImageResource(R.drawable.achievement_indicator_2_0);
                        break;
                    case 1:
                        imageView.setImageResource(R.drawable.achievement_indicator_2_1);
                        break;
                    default:
                        imageView.setImageResource(R.drawable.achievement_indicator_2_2);
                        break;
                }
                break;
            default:
                switch (action.getAchievement()) {
                    case 0:
                        imageView.setImageResource(R.drawable.achievement_indicator_3_0);
                        break;
                    case 1:
                        imageView.setImageResource(R.drawable.achievement_indicator_3_1);
                        break;
                    case 2:
                        imageView.setImageResource(R.drawable.achievement_indicator_3_2);
                        break;
                    default:
                        imageView.setImageResource(R.drawable.achievement_indicator_3_3);
                        break;
                }
                break;
        }
    }

    protected void popToRoot() {
        CustomActivity activity = (CustomActivity) getActivity();
        activity.popToRoot();
    }

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    protected long dateSerial(Date date) {
        String dateString = dateFormat.format(date);
        try {
            return Long.parseLong(dateString);
        } catch (NumberFormatException e) {
            //来ないけどね
            return 0;
        }
    }

    protected void hideKeyboard() {
        //  キーボードを隠す
        View focused = getActivity().getCurrentFocus();
        if (focused != null) {
            InputMethodManager manager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(focused.getWindowToken(), 0);
        }
    }

    static final int EX_LEN = 20;

    protected String excerpt(String source) {
        if (source.length() > EX_LEN) {
            return source.substring(0, EX_LEN - 1) + "……";
        } else {
            return source;
        }
    }

    @Override
    public void onKeyboardShown() {
        //なにもしない
    }

    @Override
    public void onKeyboardHidden() {
        //なにもしない
    }

    private class TabClickListener implements View.OnClickListener {

        private CustomActivity.Category category;

        TabClickListener(CustomActivity.Category category) {
            this.category = category;
        }

        private void startActivity(Class<?> cls) {
            Intent intent = new Intent(getActivity(), cls);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            getActivity().startActivity(intent);
        }

        @Override
        public void onClick(View v) {
            switch (category) {
                case MYPAGE:
                    startActivity(MyPageActivity.class);
                    break;
                case MESSAGE:
                    startActivity(MessageActivity.class);
                    break;
                case RECORD:
                    startActivity(RecordActivity.class);
                    break;
                case CONTACT:
                    startActivity(ContactActivity.class);
                    break;
                case INFORMATION:
                    startActivity(InformationActivity.class);
                    break;
            }
        }
    }

    protected void editTextDialog(int resId, int id, final Runnable notify) {
        final EditText sourceView = (EditText) getView().findViewById(id);
        final EditText editView = new EditText(getActivity());
        editView.setInputType(sourceView.getInputType());
        editView.setMinLines(sourceView.getLineCount());
        editView.setText(sourceView.getText().toString());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle(resId);
        builder.setView(editView);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                sourceView.setText(editView.getText().toString());
                notify.run();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        editView.requestFocus();
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                InputMethodManager manager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                manager.showSoftInput(editView, 0);
                editView.selectAll();
            }
        });
        dialog.show();
    }
}
