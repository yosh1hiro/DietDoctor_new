package jp.health_gate.DietDoctor.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import android.widget.Toast;
import com.kinvey.android.AsyncUser;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.android.callback.KinveyUserManagementCallback;
import com.kinvey.java.User;
import jp.health_gate.DietDoctor.R;

/**
 * アカウント管理クラス
 * シングルトン
 * <p/>
 * Created by kazhida on 2013/10/07.
 */
public class ActiveUser {

    public interface LoadedNotify {
        void onLoaded(boolean loggedIn);
    }

    private static final String ACTION_GROUP_ID = "ACTION_GROUP_ID";
    private static final String ACTION_LEVEL = "ACTION_LEVEL_";

    private Context context;
    private DietActions.LeveledItem[] actions = new DietActions.LeveledItem[3];
    private Backend.Profile profile = new Backend.Profile();

    private ActiveUser(final Context context, final LoadedNotify notify) {
        super();
        this.context = context;

        if (isLoggedIn()) {
            AsyncUser user = Backend.sharedInstance().getUser();
            profile.id = user.getId();
            //  通信できないときのために、ローカルの内容で初期化しておく
            loadProfileFromPreferences();
            user.retrieve(new KinveyUserCallback() {
                @Override
                public void onSuccess(User user) {
                    loadProfileFromBackend(user);
                    //  通信できないときのために、SharedPreferenceにも書いておく
                    saveProfileToPreferences();

                    //todo: これはテスト用なので、後で外す
                    profile.ticket_count = 10;

                    if (notify != null) notify.onLoaded(true);
                }

                @Override
                public void onFailure(Throwable throwable) {
                    if (notify != null) notify.onLoaded(true);
                }
            });
        } else {
            if (notify != null) notify.onLoaded(false);
        }
    }

    private static ActiveUser shared = null;

    public static ActiveUser initInstance(Context context, LoadedNotify notify) {
        if (shared == null && context != null) {
            Backend.initInstance(context);
            DatabaseHelper.initInstance(context).getWritableDatabase();
            shared = new ActiveUser(context, notify);
        }
        return shared;
    }

    public static ActiveUser sharedInstance() {
        return shared;
    }

    public boolean isLoggedIn() {
        return Backend.sharedInstance().isUserLoggedIn();
    }

    public void login(String userName, String password, final KinveyUserCallback callback) {
        Backend.sharedInstance().login(userName, password, new KinveyUserCallback() {
            @Override
            public void onSuccess(User user) {
                profile.id = user.getId();
                loadProfileFromBackend(user);
                callback.onSuccess(user);
            }

            @Override
            public void onFailure(Throwable throwable) {
                callback.onFailure(throwable);
            }
        });
    }

    public void logout() {
        Backend.sharedInstance().logout();
        profile.id = null;
    }

    public void signUp(final String userName, final String password, final KinveyUserCallback callback) {
        Backend.sharedInstance().signUp(userName, password, new KinveyUserCallback() {
            @Override
            public void onSuccess(User user) {
                profile.id = user.getId();
                profile.username = userName;
                updateProfile(profile, callback);
            }

            @Override
            public void onFailure(Throwable throwable) {
                callback.onFailure(throwable);
            }
        });
    }

    private int asInt(Object value) {
        if (value != null) {
            try {
                return Integer.parseInt(value.toString());
            } catch (NumberFormatException e) {
                return 0;
            }
        } else {
            return 0;
        }
    }

    private double asDouble(Object value) {
        if (value != null) {
            try {
                return Double.parseDouble(value.toString());
            } catch (NumberFormatException e) {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public void updateProfile(Backend.Profile profile, KinveyUserCallback callback) {
        AsyncUser user = Backend.sharedInstance().getUser();

        user.put("email", profile.email);
        user.put("gender", profile.gender);
        user.put("birthday", profile.birthday);
        user.put("height", profile.height);
        user.put("initial_weight", profile.initial_weight);
        user.put("occupation", profile.occupation);
        user.put("exercising_custom", profile.exercising_custom);
        user.put("meal_custom", profile.meal_custom);
        user.put("purpose", profile.purpose);
        user.put("target_weight", profile.target_weight);
        user.put("loss_rate", profile.loss_rate);
        user.put("current_weight", profile.current_weight);
        user.put("bmi", profile.bmi);
        user.put("ticket_count", profile.ticket_count);
        user.put("star_count", profile.star_count);
        user.put("medal_count", profile.medal_count);

        user.update(callback);
        //  通信できないときのために、SharedPreferenceにも書いておく
        saveProfileToPreferences();
    }

    private void loadProfileFromBackend(User user) {
        profile.username = user.getUsername();
        profile.email = (String) user.get("email");
        profile.gender = (String) user.get("gender");
        profile.birthday = (String) user.get("birthday");
        profile.height = asDouble(user.get("height"));
        profile.initial_weight = asDouble(user.get("initial_weight"));
        profile.occupation = (String) user.get("occupation");
        profile.exercising_custom = (String) user.get("exercising_custom");
        profile.meal_custom = (String) user.get("meal_custom");
        profile.purpose = (String) user.get("purpose");
        profile.target_weight = asDouble(user.get("target_weight"));
        profile.loss_rate = asDouble(user.get("loss_rate"));
        profile.current_weight = asDouble(user.get("current_weight"));
        profile.bmi = asDouble(user.get("bmi"));
        profile.ticket_count = asInt(user.get("ticket_count"));
        profile.star_count = asInt(user.get("star_count"));
        profile.medal_count = asInt(user.get("medal_count"));
    }

    private void saveProfileToPreferences() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();

        editor.putString("username", profile.username);
        editor.putString("email", profile.email);
        editor.putString("gender", profile.gender);
        editor.putString("birthday", profile.birthday);
        editor.putFloat("height", (float) profile.height);
        editor.putFloat("initial_weight", (float) profile.initial_weight);
        editor.putString("occupation", profile.occupation);
        editor.putString("exercising_custom", profile.exercising_custom);
        editor.putString("meal_custom", profile.meal_custom);
        editor.putString("purpose", profile.purpose);
        editor.putFloat("target_weight", (float) profile.target_weight);
        editor.putFloat("loss_rate", (float) profile.loss_rate);
        editor.putFloat("current_weight", (float) profile.current_weight);
        editor.putFloat("bmi", (float) profile.bmi);
        editor.putInt("ticket_count", profile.ticket_count);
        editor.putInt("star_count", profile.star_count);
        editor.putInt("medal_count", profile.medal_count);

        editor.commit();
    }

    private void loadProfileFromPreferences() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        final String empty = null;

        try {
            profile.username = preferences.getString("username", empty);
            profile.email = preferences.getString("email", empty);
            profile.gender = preferences.getString("gender", empty);
            profile.birthday = preferences.getString("birthday", empty);
            profile.height = preferences.getFloat("height", 0);
            profile.initial_weight = preferences.getFloat("initial_weight", 0);
            profile.occupation = preferences.getString("occupation", empty);
            profile.exercising_custom = preferences.getString("exercising_custom", empty);
            profile.meal_custom = preferences.getString("meal_custom", empty);
            profile.purpose = preferences.getString("purpose", empty);
            profile.target_weight = preferences.getFloat("target_weight", 0);
            profile.loss_rate = preferences.getFloat("loss_rate", 0);
            profile.current_weight = preferences.getFloat("current_weight", 0);
            profile.bmi = preferences.getFloat("bmi", 0);
            profile.ticket_count = preferences.getInt("ticket_count", 0);
            profile.star_count = preferences.getInt("star_count", 0);
            profile.medal_count = preferences.getInt("medal_count", 0);
        } catch (Exception e) {
            //握りつぶす
        }
    }

    public Backend.Profile getProfile() {
        return profile;
    }

    public String getUserId() {
        return isLoggedIn() ? profile.id : null;
    }

    public String getUserName() {
        return isLoggedIn() ? profile.username : null;
    }

    private SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void loadActions() {
        SharedPreferences prefs = getPreferences();
        for (int i = 0; i < 3; i++) {
            int id = prefs.getInt(ACTION_GROUP_ID + i, 0);
            int level = prefs.getInt(ACTION_LEVEL + i, -1);
            if (id > 0 && level >= 0) {
                actions[i] = DietActions.sharedInstance().findAction(id, level);
            } else {
                actions[i] = null;
            }
        }
    }

    public boolean incActionLevel(int index) {
        SharedPreferences preferences = getPreferences();
        int level = preferences.getInt(ACTION_LEVEL + index, -1);
        if (0 <= level && level <= 9) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(ACTION_LEVEL + index, level + 1);
            editor.commit();
            loadActions();
            return true;
        } else {
            return false;
        }
    }

    public void removeAction(int index) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.remove(ACTION_LEVEL + index);
        editor.commit();
        loadActions();
    }

    private void saveActions() {
        SharedPreferences.Editor editor = getPreferences().edit();

        for (int i = 0; i < 3; i++) {
            if (actions[i] != null) {
                editor.putInt(ACTION_GROUP_ID + i, actions[i].getGroupId());
                editor.putInt(ACTION_LEVEL + i, actions[i].getLevel());
            } else {
                editor.remove(ACTION_GROUP_ID + i);
                editor.remove(ACTION_LEVEL + i);
            }
        }
        editor.commit();
    }

    public boolean hasAction() {
        for (DietActions.Item action : actions) {
            if (action != null) return true;
        }
        return false;
    }

    public void storeSelected(int index, int groupId, int level) {
        actions[index] = DietActions.sharedInstance().findAction(groupId, level);
        saveActions();
    }

    public DietActions.LeveledItem getAction(int index) {
        return actions[index];
    }

    public void decreaseTicket() {
        profile.ticket_count--;
        //todo;保存
    }

    public void resetPassword() {
        String userName = profile.username;
        String email = profile.email;
        if (email != null && email.length() > 0) {
            Backend.sharedInstance().resetPassword(userName, new KinveyUserManagementCallback() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(context, R.string.reset_mail_send, Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(Throwable throwable) {
                    Toast.makeText(context, R.string.cannot_reset, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(context, R.string.email_not_found, Toast.LENGTH_SHORT).show();
        }
    }
}
