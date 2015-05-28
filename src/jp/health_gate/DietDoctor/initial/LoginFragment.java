package jp.health_gate.DietDoctor.initial;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;

import jp.health_gate.DietDoctor.models.ActiveUser;
import jp.health_gate.DietDoctor.R;

/**
 * ログイン用のフラグメント
 * <p/>
 * Created by kazhida on 2013/10/07.
 */
class LoginFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.login_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View root = getView();

        setTitleText(R.string.login_title);
        setEditText(R.id.edit_user_name, ActiveUser.sharedInstance().getUserName());

        root.findViewById(R.id.next_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceToRegister();
            }
        });

        root.findViewById(R.id.login_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        root.findViewById(R.id.reset_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActiveUser.sharedInstance().resetPassword();
            }
        });
    }

    private void replaceToRegister() {
        RegisterFragment fragment = new RegisterFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void replaceToLogout() {
        LogoutFragment fragment = new LogoutFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }


    private void login() {
        //  ログイン
        String userName = getEditText(R.id.edit_user_name);
        String password = getEditText(R.id.edit_password);
        ActiveUser.sharedInstance().login(userName, password, new KinveyUserCallback() {
            @Override
            public void onSuccess(User user) {
                replaceToLogout();
            }

            @Override
            public void onFailure(Throwable throwable) {
                Log.e("DietDoctor", throwable.getLocalizedMessage());
                Toast.makeText(getActivity(), getString(R.string.err_login), Toast.LENGTH_SHORT).show();
            }
        });
        //  キーボードを隠す
        View focused = getActivity().getCurrentFocus();
        if (focused != null) {
            InputMethodManager manager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(focused.getWindowToken(), 0);
        }
    }

    private void setTitleText(String title) {
        TextView textView = (TextView) getView().findViewById(R.id.title_text);
        textView.setText(title);
    }

    private void setTitleText(int resId) {
        setTitleText(getString(resId));
    }

    private String getEditText(int id) {
        EditText text = (EditText) getView().findViewById(id);
        Editable edit = text.getText();
        return edit == null ? null : edit.toString();
    }

    private void setEditText(int id, String text) {
        EditText textView = (EditText) getView().findViewById(id);
        textView.setText(text);
    }
}
