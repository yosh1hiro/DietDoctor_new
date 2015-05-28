package jp.health_gate.DietDoctor.contact;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import jp.health_gate.DietDoctor.CustomFragment;
import jp.health_gate.DietDoctor.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * DDクリニックの返答のサンプル
 * <p/>
 * Created by kazhida on 2013/12/20.
 */
class TopicSampleFragment extends CustomFragment {

    private SimpleDateFormat format = new SimpleDateFormat("MM/dd hh:mm");

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

        //  どうせ、さんぷるなので、文字列直書き
        setText(R.id.consultation_reply_a, "ここには、あなたのプロフィールや、基本的な情報に基づいたアドバイスが入ります。\n\n各見出しをタップすると内容が表示されます。");
        setText(R.id.consultation_reply_b, "ここには、あなたの生活習慣に基づいたアドバイスが入ります。");
        setText(R.id.consultation_reply_c, "ここには、このアプリ(DietDoctor)の実行状況に基づいたアドバイスが入ります。");
        setText(R.id.consultation_reply_d, "ここには、");
        setText(R.id.consultation_reply_e, "・POSE\n・ORDER\nがおススメです。\nのように、適切なダイエット方法が入ります。");

        initComments();

        getView().findViewById(R.id.send_layout).setVisibility(View.GONE);
        getView().findViewById(R.id.consultation_reply_a).setVisibility(View.VISIBLE);
    }

    private void initComments() {
        LinearLayout parent = (LinearLayout) getView().findViewById(R.id.comments_area);
        parent.removeAllViews();
        int marginOffset = getResources().getDisplayMetrics().widthPixels / 6;
        int margin = (int) (4.0f * getResources().getDisplayMetrics().density);

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -3);

        DummyComment[] comments = new DummyComment[]{
                new DummyComment("ユーザの相談内容", true),
                new DummyComment("ドクターの回答", false),
                new DummyComment("さらに、質問することも", true)
        };

        for (DummyComment comment : comments) {
            int layout = comment.isFromUser() ? R.layout.topic_detail_user_comment : R.layout.topic_detail_doctor_comment;
            View view = getActivity().getLayoutInflater().inflate(layout, null, false);

            TextView content = (TextView) view.findViewById(R.id.content_text);
            TextView date = (TextView) view.findViewById(R.id.datetime_text);
            content.setText(comment.getComment());
            date.setText(format.format(calendar.getTime()));

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

    private class DummyComment {
        boolean fromUser;
        String comment;

        DummyComment(String comment, boolean fromUser) {
            this.comment = comment;
            this.fromUser = fromUser;
        }

        boolean isFromUser() {
            return fromUser;
        }

        String getComment() {
            return comment;
        }
    }
}
