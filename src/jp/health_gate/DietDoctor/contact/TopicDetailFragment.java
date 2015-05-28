package jp.health_gate.DietDoctor.contact;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import jp.health_gate.DietDoctor.CustomFragment;
import jp.health_gate.DietDoctor.R;
import jp.health_gate.DietDoctor.models.Counsels;

import java.text.SimpleDateFormat;

/**
 * 相談のやりとりを表示するフラグメント
 * <p/>
 * Created by kazhida on 2013/10/15.
 */
class TopicDetailFragment extends CustomFragment {

    private static final String INDEX = "INDEX";
    private Counsels.Item counsel;
    private SimpleDateFormat format = new SimpleDateFormat("MM/dd hh:mm");

    private TopicDetailFragment() {
        super();
    }

    public static TopicDetailFragment newInstance(int index) {
        TopicDetailFragment fragment = new TopicDetailFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(INDEX, index);
        fragment.setArguments(bundle);

        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        counsel = Counsels.sharedInstance().getItems().get(getArguments().getInt(INDEX, 0));
        return inflater.inflate(R.layout.topic_detail_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitleText(R.string.contact_title);

        getView().findViewById(R.id.caption_a).setOnClickListener(new ExpandListener(R.id.consultation_reply_a_panel));
        getView().findViewById(R.id.caption_b).setOnClickListener(new ExpandListener(R.id.consultation_reply_b_panel));
        getView().findViewById(R.id.caption_c).setOnClickListener(new ExpandListener(R.id.consultation_reply_c_panel));
        getView().findViewById(R.id.caption_d).setOnClickListener(new ExpandListener(R.id.consultation_reply_d_panel));
        getView().findViewById(R.id.caption_e).setOnClickListener(new ExpandListener(R.id.consultation_reply_e_panel));

        setText(R.id.consultation_reply_a, counsel.getAnswerByProfile());
        setText(R.id.consultation_reply_b, counsel.getAnswerByCustom());
        setText(R.id.consultation_reply_c, counsel.getAnswerByAction());
        setText(R.id.consultation_reply_d, counsel.getAnswerByIllness());
        setText(R.id.consultation_reply_e, recommendedAction());

        initComments();

        getView().findViewById(R.id.send_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setTitle(R.string.confirmation);
                builder.setMessage(R.string.caution_ticket_0);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendCounsel();
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);

                builder.create().show();
            }
        });
    }

    private void initComments() {
        LinearLayout parent = (LinearLayout) getView().findViewById(R.id.comments_area);
        parent.removeAllViews();
        int marginOffset = getResources().getDisplayMetrics().widthPixels / 6;
        int margin = (int) (4.0f * getResources().getDisplayMetrics().density);

        for (Counsels.Comment comment : counsel.getComments()) {
            int layout = comment.isFromUser() ? R.layout.topic_detail_user_comment : R.layout.topic_detail_doctor_comment;
            View view = getActivity().getLayoutInflater().inflate(layout, null, false);

            TextView content = (TextView) view.findViewById(R.id.content_text);
            TextView date = (TextView) view.findViewById(R.id.datetime_text);
            content.setText(comment.getComment());
            date.setText(format.format(comment.getTime().getTime()));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            if (comment.isFromUser()) {
                params.setMargins(margin, margin, margin + marginOffset, margin);
            } else {
                params.setMargins(margin + marginOffset, margin, margin, margin);
            }
            view.setLayoutParams(params);

            parent.addView(view);
        }
    }

    private String recommendedAction() {
        StringBuilder builder = new StringBuilder();
        for (String action : counsel.getRecommendedAction()) {
            builder.append("・").append(action).append("\n");
        }
        builder.append("がおススメです。");
        return builder.toString();
    }

    private void sendCounsel() {
        TextView textView = (TextView) getView().findViewById(R.id.consultation_text);

        counsel.addComment(textView.getText().toString());
        Counsels.sharedInstance().send(counsel, new Counsels.Callback() {
            @Override
            public void onSuccess() {
                Toast.makeText(getActivity(), R.string.send_successfully, Toast.LENGTH_SHORT).show();
                setText(R.id.consultation_text, null);
                initComments();
                hideKeyboard();
            }

            @Override
            public void onFailure() {
                Toast.makeText(getActivity(), R.string.send_failed, Toast.LENGTH_SHORT).show();
                hideKeyboard();
            }
        });
    }

    private class ExpandListener implements View.OnClickListener {
        private View associatedView;

        ExpandListener(int id) {
            associatedView = getView().findViewById(id);
        }

        private void setVisibility(int id) {
            View view = getView().findViewById(id);
            view.setVisibility(view == associatedView ? View.VISIBLE : View.GONE);
        }

        @Override
        public void onClick(View v) {
            if (associatedView.getVisibility() == View.VISIBLE) {
                associatedView.setVisibility(View.GONE);
            } else {
                setVisibility(R.id.consultation_reply_a_panel);
                setVisibility(R.id.consultation_reply_b_panel);
                setVisibility(R.id.consultation_reply_c_panel);
                setVisibility(R.id.consultation_reply_d_panel);
                setVisibility(R.id.consultation_reply_e_panel);
            }
        }
    }
}
