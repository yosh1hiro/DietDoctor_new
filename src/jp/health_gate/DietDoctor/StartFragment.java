package jp.health_gate.DietDoctor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import jp.health_gate.DietDoctor.models.Achievements;
import jp.health_gate.DietDoctor.models.ActiveUser;
import jp.health_gate.DietDoctor.models.DietActions;
import jp.health_gate.DietDoctor.models.Weights;
import jp.health_gate.DietDoctor.mypage.MyPageActivity;
import jp.health_gate.DietDoctor.record.RecordActivity;

/**
 * スタート画面用フラグメント
 * 裏で同期とかいろいろやる（予定）。
 * <p/>
 * Created by kazhida on 2013/10/04.
 */
public class StartFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.start_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        getView().findViewById(R.id.bg_image).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                replaceToIntroduction();
//            }
//        });
    }

    private boolean replaced;
    private boolean started = false;
    private Calendar today = Calendar.getInstance();

    @Override
    public void onPause() {
        super.onPause();
        replaced = true;
    }

    private static final String BACKEND_LOADED_DATE = "BACKEND_LOADED_DATE";

    protected long dateSerial(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String dateString = dateFormat.format(date);
        try {
            return Long.parseLong(dateString);
        } catch (NumberFormatException e) {
            //来ないけどね
            return 0;
        }
    }

    private boolean isLoadedToday() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        long achievedDate = preferences.getLong(BACKEND_LOADED_DATE, 0);
        return achievedDate == dateSerial(today.getTime());
    }

    private void loaded() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        editor.putLong(BACKEND_LOADED_DATE, dateSerial(today.getTime()));
        editor.commit();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!isLoadedToday()) {
            DietActions.sharedInstance().loadActions(new DietActions.LoadNotify() {
                @Override
                public void onLoaded(final boolean success) {
                    if (!replaced && success) {
                        initialLoadAndStart();
                    }
                    replaced = false;
                }
            });
        } else if (!started) {
            if (ActiveUser.sharedInstance().isLoggedIn()) {
                DietActions.sharedInstance().loadActions(null);
                initialLoadAndStart();
            }
        } else {
            getActivity().finish();
        }
    }

    private void initialLoadAndStart() {
        final Handler handler = new Handler();

        ActiveUser.sharedInstance().loadActions();
        Achievements.sharedInstance().importFromBackend(new Runnable() {
            @Override
            public void run() {
                Weights.sharedInstance().importFromBackend(new Runnable() {
                    @Override
                    public void run() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                DietActions.sharedInstance().loadImages();
                                startFragment();
                                loaded();
                            }
                        });
                    }
                });
            }
        });
    }

    private void startFragment() {
        started = true;
        if (ActiveUser.sharedInstance().hasAction()) {
            Intent intent = new Intent(getActivity(), RecordActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        } else {
            Intent intent = new Intent(getActivity(), MyPageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(intent);
        }
    }
}
