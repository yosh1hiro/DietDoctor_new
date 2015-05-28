package jp.health_gate.DietDoctor.mypage;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jp.health_gate.DietDoctor.CalendarUtils;
import jp.health_gate.DietDoctor.R;
import jp.health_gate.DietDoctor.models.Achievements;
import jp.health_gate.DietDoctor.models.DietActions;
import jp.health_gate.DietDoctor.models.Weights;

/**
 * Created by yoshihiro on 2014/10/27.
 */
class ActionOfTheDayFragment extends DialogFragment {

    private static final String THE_DAY = "THE_DAY";

    public static ActionOfTheDayFragment newInstance(Calendar date) {
        ActionOfTheDayFragment fragment = new ActionOfTheDayFragment();

        Bundle bundle = new Bundle();
        bundle.putLong(THE_DAY, date.getTimeInMillis());
        fragment.setArguments(bundle);

        return fragment;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Calendar theDay = CalendarUtils.dateCalendar(getArguments().getLong(THE_DAY), 0);
        View contentView = createContentView(theDay);

        DateFormat format = android.text.format.DateFormat.getDateFormat(getActivity());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(format.format(theDay.getTime()));
        builder.setPositiveButton(R.string.close, null);
        builder.setView(contentView);

        return builder.create();
    }

    private View createContentView(Calendar theDay) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.action_the_day_fragment, null, false);

        List<Achievements.Item> achievements = Achievements.sharedInstance().load(theDay);
        Weights.Item weight = Weights.sharedInstance().getRecentWeight(theDay);
        List<DietActions.LeveledItem> actions = new ArrayList<DietActions.LeveledItem>();

        for (Achievements.Item item : achievements) {
            if (item.getMedal() > 0) {
                DietActions.LeveledItem action = DietActions.sharedInstance().findAction(item.getGroupId(), item.getLevel());
                if (action != null) {
                    actions.add(action);
                }
            }
        }

        if (weight == null) {
            setText(viewGroup, R.id.weight_text, null);
        } else {
            setText(viewGroup, R.id.weight_text, String.format("%1.0f ", weight.getWeight()) + getString(R.string.kg));
        }

        int stars = 0;
        for (Achievements.Item item : achievements) {
            stars += item.getStar();
        }
        setText(viewGroup, R.id.star_count, "" + stars);

        if (weight.hasMemo() && weight.isSameDay(theDay)) {
            viewGroup.findViewById(R.id.memo_text).setVisibility(View.VISIBLE);
            setText(viewGroup, R.id.memo_text, weight.getMemo());
        } else {
            viewGroup.findViewById(R.id.memo_text).setVisibility(View.INVISIBLE);
        }

        if (actions.size() > 0) {
            setAction(viewGroup, R.id.action_caption_0, R.id.action_icon_0, actions.get(0));
        } else {
            viewGroup.findViewById(R.id.action_icon_0).setVisibility(View.GONE);
            viewGroup.findViewById(R.id.action_caption_0).setVisibility(View.GONE);
        }

        if (actions.size() > 1) {
            setAction(viewGroup, R.id.action_caption_1, R.id.action_icon_1, actions.get(1));
        } else {
            viewGroup.findViewById(R.id.action_icon_1).setVisibility(View.GONE);
            viewGroup.findViewById(R.id.action_caption_1).setVisibility(View.GONE);
        }

        if (actions.size() > 2) {
            setAction(viewGroup, R.id.action_caption_2, R.id.action_icon_2, actions.get(2));
        } else {
            viewGroup.findViewById(R.id.action_icon_2).setVisibility(View.GONE);
            viewGroup.findViewById(R.id.action_caption_2).setVisibility(View.GONE);
        }

        return viewGroup;
    }

    private void setText(View parent, int id, String s) {
        TextView textView = (TextView) parent.findViewById(id);
        textView.setText(s);
    }

    private void setAction(View parent, int textId, int imageId, DietActions.LeveledItem action) {
        TextView textView = (TextView) parent.findViewById(textId);
        textView.setVisibility(View.VISIBLE);
        textView.setText(action.getTitle());

        ImageView imageView = (ImageView) parent.findViewById(imageId);
        imageView.setImageDrawable(action.getIcon());
    }
}
