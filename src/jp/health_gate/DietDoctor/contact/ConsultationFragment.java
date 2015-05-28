package jp.health_gate.DietDoctor.contact;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import jp.health_gate.DietDoctor.CustomFragment;
import jp.health_gate.DietDoctor.R;
import jp.health_gate.DietDoctor.models.ActiveUser;
import jp.health_gate.DietDoctor.models.Counsels;

import java.util.ArrayList;
import java.util.List;

/**
 * ドクターに相談のフラグメント
 * <p/>
 * Created by kazhida on 2013/10/15.
 */
class ConsultationFragment extends CustomFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.consultation_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitleText(R.string.contact_title);

        getView().findViewById(R.id.ticket_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new TicketFragment());
            }
        });

        getView().findViewById(R.id.require_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActiveUser.sharedInstance().getProfile().getTicketCount() == 0) {
                    replaceFragment(new TicketFragment());
                } else {
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
            }
        });

        getView().findViewById(R.id.response_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new TopicListFragment());
            }
        });

        TextView ticket = (TextView) getView().findViewById(R.id.num_tickets_text);
        ticket.setText("" + ActiveUser.sharedInstance().getProfile().getTicketCount());

        getView().findViewById(R.id.sample_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceFragment(new TopicSampleFragment());
            }
        });
    }

    private String getEditText(int id) {
        EditText editText = (EditText) getView().findViewById(id);
        return editText.getText().toString();
    }

    private String getSelectedText(int[] ids) {
        for (int id : ids) {
            RadioButton button = (RadioButton) getView().findViewById(id);
            if (button.isChecked()) return button.getText().toString();
        }
        return null;
    }

    private List<String> getIllness() {
        List<String> illness = new ArrayList<String>();

        int[] ids = new int[]{
                R.id.consultation_Illness_0,
                R.id.consultation_Illness_1,
                R.id.consultation_Illness_2,
                R.id.consultation_Illness_3,
                R.id.consultation_Illness_4,
        };
        for (int id : ids) {
            CheckBox checkBox = (CheckBox) getView().findViewById(id);
            if (checkBox.isChecked()) {
                illness.add(checkBox.getText().toString());
            }
        }
        return illness;
    }

    private void sendCounsel() {
        String work = getSelectedText(new int[]{
                R.id.consultation_work_0,
                R.id.consultation_work_1,
                R.id.consultation_work_2,
        });
        String meal = getSelectedText(new int[]{
                R.id.consultation_meal_0,
                R.id.consultation_meal_1,
                R.id.consultation_meal_2,
                R.id.consultation_meal_3,
        });
        String exercise = getSelectedText(new int[]{
                R.id.consultation_exercise_0,
                R.id.consultation_exercise_1,
                R.id.consultation_exercise_2,
        });
        String snack = getSelectedText(new int[]{
                R.id.consultation_snack_0,
                R.id.consultation_snack_1,
                R.id.consultation_snack_2,
        });
        String drink = getSelectedText(new int[]{
                R.id.consultation_drink_0,
                R.id.consultation_drink_1,
        });
        List<String> illness = getIllness();
        String title = getEditText(R.id.consultation_title);
        String comment = getEditText(R.id.consultation_text);

        if (work == null || meal == null || exercise == null || snack == null || drink == null) {
            Toast.makeText(getActivity(), R.string.err_non_anamnesis, Toast.LENGTH_SHORT).show();
        } else if (comment == null || comment.trim().length() == 0) {
            Toast.makeText(getActivity(), R.string.err_no_comment, Toast.LENGTH_SHORT).show();
        } else {
            if (title == null || title.trim().length() == 0) {
                title = getString(R.string.counsel);
            }
            Counsels.Item item = Counsels.newCounsel(comment.trim());
            item.title = title.trim();
            item.work = work;
            item.meal = meal;
            item.exercise = exercise;
            item.snack = snack;
            item.drink = drink;
            item.illness = illness;
            Counsels.sharedInstance().send(item, new Counsels.Callback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getActivity(), R.string.send_successfully, Toast.LENGTH_SHORT).show();
                    setText(R.id.consultation_title, null);
                    setText(R.id.consultation_text, null);
                    hideKeyboard();
                }

                @Override
                public void onFailure() {
                    Toast.makeText(getActivity(), R.string.send_failed, Toast.LENGTH_SHORT).show();
                    hideKeyboard();
                }
            });
        }
    }
}
