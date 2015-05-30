package jp.health_gate.DietDoctor.information;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;
import jp.health_gate.DietDoctor.CustomFragment;
import jp.health_gate.DietDoctor.initial.InitialActivity;
import jp.health_gate.DietDoctor.R;
import jp.health_gate.DietDoctor.models.Achievements;
import jp.health_gate.DietDoctor.models.ActiveUser;
import jp.health_gate.DietDoctor.models.Backend;
import jp.health_gate.DietDoctor.models.Weights;

/**
 * プロフィール変更のフラグメント
 * <p/>
 * Created by kazhida on 2013/10/15.
 */
class ProfileFragment extends CustomFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.profile_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitleText("ユーザ情報変更");

        Backend.Profile profile = ActiveUser.sharedInstance().getProfile();

        setText(R.id.edit_mail_address, profile.getEmail());
        setText(R.id.edit_birthday, profile.getBirthday());
        setWeight(R.id.edit_height, profile.getHeight());
        setWeight(R.id.edit_weight, profile.getInitialWeight());
        if (getString(R.string.male).equals(profile.getGender())) {
            checkRadioButton(R.id.radio_male);
        } else {
            checkRadioButton(R.id.radio_female);
        }
        initSpinner(R.id.edit_occupation, R.array.occupation_list, profile.getOccupation());
        initSpinner(R.id.edit_exercising_custom, R.array.exercising_custom_list, profile.getExercisingCustom());
        initSpinner(R.id.edit_meal_custom, R.array.meal_custom_list, profile.getMealCustom());

        getView().findViewById(R.id.update_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile();
            }
        });

        getView().findViewById(R.id.logout_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    private void checkRadioButton(int id) {
        RadioButton button = (RadioButton) getView().findViewById(id);
        button.setChecked(true);
    }

    private void initSpinner(int id, int resId, String current) {
        Spinner spinner = (Spinner) getView().findViewById(id);
        Resources resources = getActivity().getResources();
        String[] items = resources.getStringArray(resId);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_text, items);
        spinner.setAdapter(adapter);
        for (int i = 0; i < items.length; i++) {
            if (items[i].equals(current)) {
                spinner.setSelection(i);
            }
        }
    }

    private String getEditText(int id) {
        EditText text = (EditText) getView().findViewById(id);
        return text.getText().toString();
    }

    private String getRadioText(int id) {
        RadioGroup radio = (RadioGroup) getView().findViewById(id);
        RadioButton button = (RadioButton) getView().findViewById(radio.getCheckedRadioButtonId());
        return button.getText().toString();
    }

    private String getSpinnerText(int id) {
        Spinner spinner = (Spinner) getView().findViewById(id);
        return (String) spinner.getSelectedItem();
    }

    private void updateProfile() {
        Backend.Profile profile = ActiveUser.sharedInstance().getProfile();

        profile.setRegisterInfo(
                getEditText(R.id.edit_mail_address),
                getRadioText(R.id.radio_gender),
                getEditText(R.id.edit_birthday),
                getEditText(R.id.edit_height),
                getEditText(R.id.edit_weight),
                getSpinnerText(R.id.edit_occupation),
                getSpinnerText(R.id.edit_exercising_custom),
                getSpinnerText(R.id.edit_meal_custom));

        ActiveUser.sharedInstance().updateProfile(profile, new KinveyUserCallback() {
            @Override
            public void onSuccess(User user) {
                getFragmentManager().popBackStack();
            }

            @Override
            public void onFailure(Throwable throwable) {
                Toast.makeText(getActivity(), R.string.err_cannot_sent_to_server, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        ActiveUser.sharedInstance().logout();

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        editor.remove(InitialActivity.NOT_FIRST_TIME);
        editor.commit();

        Achievements.sharedInstance().deleteAll();
        Weights.sharedInstance().deleteAll();

        getActivity().startActivity(new Intent(getActivity(), InitialActivity.class));
    }
}
