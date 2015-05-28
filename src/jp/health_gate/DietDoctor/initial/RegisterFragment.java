package jp.health_gate.DietDoctor.initial;

import android.app.AlertDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;
import jp.health_gate.DietDoctor.models.ActiveUser;
import jp.health_gate.DietDoctor.R;
import jp.health_gate.DietDoctor.models.Backend;
import jp.health_gate.DietDoctor.models.Weights;

import java.util.Calendar;

/**
 * ユーザ登録用のフラグメント
 * <p/>
 * Created by kazhida on 2013/10/05.
 */
class RegisterFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.register_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View root = getView();
        TextView title = (TextView) root.findViewById(R.id.title_text);

        title.setText(R.string.register_title);

        root.findViewById(R.id.prev_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                manager.popBackStack();
            }
        });

        root.findViewById(R.id.next_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

        initSpinner(R.id.edit_occupation, R.array.occupation_list);
        initSpinner(R.id.edit_exercising_custom, R.array.exercising_custom_list);
        initSpinner(R.id.edit_meal_custom, R.array.meal_custom_list);
    }

    @Override
    public void onResume() {
        super.onResume();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.consideration);
        builder.setMessage(R.string.qualification);
        builder.setPositiveButton(R.string.close, null);

        builder.create().show();
    }

    private void initSpinner(int id, int resId) {
        Spinner spinner = (Spinner) getView().findViewById(id);
        Resources resources = getActivity().getResources();
        String[] items = resources.getStringArray(resId);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_text, items);
        spinner.setAdapter(adapter);
    }

    private void signUp() {
        String userName = getEditText(R.id.edit_user_name);
        String password = getEditText(R.id.edit_password);

        if (userName.startsWith("admin.")) {
            Toast.makeText(getActivity(), R.string.err_admin_username, Toast.LENGTH_SHORT).show();
            return;
        }

        final Backend.Profile profile = ActiveUser.sharedInstance().getProfile();

        profile.setRegisterInfo(
                getEditText(R.id.edit_mail_address),
                getRadioText(R.id.radio_gender),
                getEditText(R.id.edit_birthday),
                getEditText(R.id.edit_height),
                getEditText(R.id.edit_initial_weight),
                getSpinnerText(R.id.edit_occupation),
                getSpinnerText(R.id.edit_exercising_custom),
                getSpinnerText(R.id.edit_meal_custom));

        ActiveUser.sharedInstance().signUp(userName, password, new KinveyUserCallback() {
            @Override
            public void onSuccess(User user) {
                try {
                    //最初の体重データを入れておく
                    float weight = profile.getInitialWeight();
                    String memo = getString(R.string.initial_weight);
                    Weights.sharedInstance().addWeight(Calendar.getInstance(), weight, memo);
                } catch (NumberFormatException e) {
                    //nop
                }
                replaceToLogout();
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e("DietDoctor", throwable.getLocalizedMessage());

                //todo: 一時的なものなので、あとで外す
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("SignUpエラー");
                String msg = "SignUpできませんでした。";
                if (throwable != null) {
                    msg = msg + "[" + throwable.getLocalizedMessage() + "]";
                } else {
                    msg = msg + "[原因不明]";
                }
                builder.setMessage(msg);
                builder.setPositiveButton("OK", null);
                builder.create().show();

//                Toast.makeText(getActivity(), R.string.err_register, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void replaceToLogout() {
        LogoutFragment fragment = new LogoutFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
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
}
