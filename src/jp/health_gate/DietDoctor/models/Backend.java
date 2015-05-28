package jp.health_gate.DietDoctor.models;

import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.google.api.client.json.GenericJson;
import com.google.api.client.util.Key;
import com.kinvey.android.AsyncAppData;
import com.kinvey.android.AsyncCustomEndpoints;
import com.kinvey.android.AsyncUser;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.android.callback.KinveyUserManagementCallback;
import com.kinvey.java.Query;
import com.kinvey.java.core.DownloaderProgressListener;
import com.kinvey.java.core.KinveyClientCallback;
import com.kinvey.java.core.MediaHttpDownloader;
import com.kinvey.java.model.KinveyMetaData;
import com.kinvey.java.query.AbstractQuery;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * Backend(Kinvey)とのインターフェース・ラッパー
 * <p/>
 * Created by kazhida on 2013/10/20.
 */
public class Backend {

    interface DownLoadListener {
        void onLoaded(Bitmap bitmap);
    }

    public static final String ISO8601UTC = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static Backend shared = null;

    private Client client = null;
    private ThreadSafeCounter counter = new ThreadSafeCounter();

    private Backend(Context context) {
        this.client = new Client.Builder("kid_Te37TDNMq9", "49b0ed2c7a7d4f50b01c3261387b6597", context).build();
    }

    public static Backend initInstance(Context context) {
        shared = new Backend(context);
        return shared;
    }

    public static Backend sharedInstance() {
        return shared;
    }

    public void login(String userName, String password, KinveyUserCallback callback) {
        client.user().login(userName, password, callback);
    }

    public void logout() {
        client.user().logout().execute();
    }

    public void signUp(String userName, String password, KinveyUserCallback callback) {
        client.user().create(userName, password, callback);
    }

    public AsyncUser getUser() {
        return client.user();
    }

    public boolean isUserLoggedIn() {
        return client.user().isUserLoggedIn();
    }

    public void resetPassword(String userName, KinveyUserManagementCallback callback) {
        client.user().resetPassword(userName, callback);
    }

    //--------------------------------
    //  user-profile
    //--------------------------------

    public static class Profile extends GenericJson {
        @Key("_id")
        String id;
        @Key
        String username;
        @Key
        String email;
        @Key
        String gender;
        @Key
        String birthday;
        @Key
        double height;
        @Key
        double initial_weight;
        @Key
        String occupation;
        @Key
        String exercising_custom;
        @Key
        String meal_custom;
        @Key
        String purpose;
        @Key
        double target_weight;
        @Key
        double loss_rate;
        @Key
        double current_weight;
        @Key
        double bmi;
        @Key
        int ticket_count;
        @Key
        int star_count;
        @Key
        int medal_count;

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public String getGender() {
            return gender;
        }

        public String getBirthday() {
            return birthday;
        }

        public String getOccupation() {
            return occupation;
        }

        public String getExercisingCustom() {
            return exercising_custom;
        }

        public String getMealCustom() {
            return meal_custom;
        }

        public String getPurpose() {
            return purpose;
        }

        public void setPurpose(String purpose) {
            this.purpose = purpose;
        }

        public float getHeight() {
            return (float) height;
        }

        public float getInitialWeight() {
            return (float) initial_weight;
        }

        public float getTargetWeight() {
            return (float) target_weight;
        }

        public void setTargetWeight(String weight) {
            try {
                target_weight = Double.parseDouble(weight);
            } catch (NumberFormatException e) {
                //無視
            }
        }

        public int getTicketCount() {
            return ticket_count;
        }

        public int getStarCount() {
            return star_count;
        }

        public int getMedalCount() {
            return medal_count;
        }

        public void setRegisterInfo(
                String email,
                String gender,
                String birthday,
                String height,
                String initial_weight,
                String occupation,
                String exercising_custom,
                String meal_custom) {

            this.email = email;
            this.gender = gender;
            this.birthday = birthday;
            this.height = Double.parseDouble(height);
            this.initial_weight = Double.parseDouble(initial_weight);
            this.occupation = occupation;
            this.exercising_custom = exercising_custom;
            this.meal_custom = meal_custom;
        }

        public void setAchievementInfo(int star, int medal) {
            star_count = star;
            medal_count = medal;
        }

        public Profile() {
        }
    }


    //--------------------------------
    //  diet-actions
    //--------------------------------

    public static class DietActionRecord extends GenericJson implements Comparable<DietActionRecord> {
        @Key("_id")
        String serverId;
        @Key
        int groupId;
        @Key
        int level;        //groupItemでは0,leveledItemでは>0
        @Key
        String caption;
        @Key
        String description;
        @Key
        String description_1;
        @Key
        String description_2;
        @Key
        String description_3;
        @Key
        String description_4;
        @Key
        String tips;
        @Key
        String comment;
        @Key
        String category;
        @Key
        int param_0;
        @Key
        int param_1;
        @Key
        int param_2;
        @Key("_kmd")
        KinveyMetaData meta;

        public DietActionRecord() {
        }

        @Override
        public int compareTo(DietActionRecord another) {
            int compare = this.groupId - another.groupId;
            if (compare == 0) {
                compare = this.level - another.level;
            }
            return compare;
        }
    }

    public void loadDietActions(final KinveyListCallback<DietActionRecord> callback) {
        AsyncAppData<DietActionRecord> appData = client.appData("diet-actions", DietActionRecord.class);
        appData.get(new KinveyListCallback<DietActionRecord>() {
            @Override
            public void onSuccess(DietActionRecord[] dietActions) {
                Log.d("DietDoctor", "loadDietActions: success");
                //  Kinveyでのソートがいまいちなので、自前でソートする
                List<DietActionRecord> list = new ArrayList<DietActionRecord>();
                if (dietActions != null) {
                    Collections.addAll(list, dietActions);
                }
                Collections.sort(list);
                DietActionRecord[] sortedActions = new DietActionRecord[list.size()];
                list.toArray(sortedActions);
                Log.d("DietDoctor", "loadDietActions: sorted");
                callback.onSuccess(sortedActions);
            }

            @Override
            public void onFailure(Throwable throwable) {
                callback.onFailure(throwable);
            }
        });
    }

    //--------------------------------
    //  achievements
    //--------------------------------

    public static class AchievementRecord extends GenericJson {
        @Key("user_id")
        String userId;
        @Key
        int year;
        @Key
        int month;
        @Key
        int date;
        @Key
        String time;
        @Key
        int groupId;
        @Key
        int level;
        @Key
        int star;
        @Key
        int medal;

        public AchievementRecord() {
        }
    }

    public void insertAchievement(Calendar date, int groupId, int level, int star, int medal,
                                  KinveyClientCallback<AchievementRecord> callback) {
        AchievementRecord record = new AchievementRecord();

        record.userId = ActiveUser.sharedInstance().getUserId();
        record.year = date.get(Calendar.YEAR);
        record.month = date.get(Calendar.MONTH) + 1;
        record.date = date.get(Calendar.DAY_OF_MONTH);
        record.time = new SimpleDateFormat(ISO8601UTC).format(date.getTime());
        record.groupId = groupId;
        record.level = level;
        record.star = star;
        record.medal = medal;

        AsyncAppData<AchievementRecord> appData = client.appData("achievements", AchievementRecord.class);
        appData.save(record, callback);
    }

    public void loadAchievements(Calendar date, KinveyListCallback<AchievementRecord> callback) {
        String time = new SimpleDateFormat(ISO8601UTC).format(date.getTime());

        Query query = client.query();
        query.greaterThan("time", time);
        query.equals("user_id", ActiveUser.sharedInstance().getUserId());
        AsyncAppData<AchievementRecord> appData = client.appData("achievements", AchievementRecord.class);

        appData.get(query, callback);
    }


    //--------------------------------
    //  weights
    //--------------------------------

    public static class WeightRecord extends GenericJson {
        @Key("user_id")
        String userId;
        @Key
        int year;
        @Key
        int month;
        @Key
        int date;
        @Key
        String time;
        @Key
        float weight;
        @Key
        float rate;
        @Key
        String memo;

        public WeightRecord() {
        }
    }

    public void insertWeight(Calendar date, float weight, float rate, String memo,
                             KinveyClientCallback<WeightRecord> callback) {
        WeightRecord record = new WeightRecord();

        record.userId = ActiveUser.sharedInstance().getUserId();
        record.year = date.get(Calendar.YEAR);
        record.month = date.get(Calendar.MONTH) + 1;
        record.date = date.get(Calendar.DAY_OF_MONTH);
        record.time = new SimpleDateFormat(ISO8601UTC).format(date.getTime());
        record.rate = rate;
        record.weight = weight;
        record.memo = memo;

        AsyncAppData<WeightRecord> appData = client.appData("weights", WeightRecord.class);
        appData.save(record, callback);
    }

    public void loadWeights(Calendar date, KinveyListCallback<WeightRecord> callback) {
        String time = new SimpleDateFormat(ISO8601UTC).format(date.getTime());

        Query query = client.query();
        query.greaterThan("time", time);
        query.equals("user_id", ActiveUser.sharedInstance().getUserId());
        AsyncAppData<WeightRecord> appData = client.appData("weights", WeightRecord.class);

        appData.get(query, callback);
    }

    //--------------------------------
    //  rank-points
    //--------------------------------

    public static class RankPointRecord extends GenericJson {
        @Key("loss_rate")
        double lossRate;
        @Key
        double bmi;
        @Key("star_count")
        int starCount;
        @Key("medal_count")
        int medalCount;
        @Key("loss_rate_rank")
        int lossRateRank;
        @Key("bmi_rank")
        int bmiRank;
        @Key("star_count_rank")
        int starCountRank;
        @Key("medal_count_rank")
        int medalCountRank;

        public RankPointRecord() {
        }
    }

    public void loadRanking(final KinveyListCallback<RankPointRecord> callback) {
        AsyncCustomEndpoints<RankPointRecord, RankPointRecord> endpoints = client.customEndpoints(RankPointRecord.class);
        endpoints.callEndpoint("rank-points", new RankPointRecord(), callback);
//        endpoints.callEndpoint("rank-history", new RankPointRecord(), callback);
    }

    //--------------------------------
    //  counsels
    //--------------------------------

    public static class CommentRecord extends GenericJson {
        @Key("user_id")
        String userId;

        @Key("username")
        String userName;

        @Key
        String time;

        @Key
        String comment;

        public CommentRecord() {
        }
    }

    public static class CounselRecord extends GenericJson {
        @Key("_id")
        String id;
        @Key("user_id")
        String userId;
        @Key
        Profile user;
        @Key
        String time;
        //  問診票の内容
        @Key
        String title;
        @Key
        String work;
        @Key
        String meal;
        @Key
        String exercise;
        @Key
        String snack;
        @Key
        String drink;
        @Key
        String[] illness;
        //  定型回答
        @Key("by_profile")
        String byProfile;
        @Key("by_custom")
        String byCustom;
        @Key("by_action")
        String byAction;
        @Key("by_illness")
        String byIllness;
        @Key("recommended_action")
        String[] recommendedAction;
        //  相談のやりとり
        @Key
        CommentRecord[] comments;

        public CounselRecord() {
        }
    }

    public void insertCounsel(CounselRecord record, KinveyClientCallback<CounselRecord> callback) {
        record.user = ActiveUser.sharedInstance().getProfile();
        AsyncAppData<CounselRecord> appData = client.appData("counsels", CounselRecord.class);
        appData.save(record, callback);
    }

    public void loadCounsels(KinveyListCallback<CounselRecord> callback) {
        Query query = client.query();
        String userId = ActiveUser.sharedInstance().getUserId();
        query.equals("user_id", userId).addSort("time", AbstractQuery.SortOrder.DESC);
        AsyncAppData<CounselRecord> appData = client.appData("counsels", CounselRecord.class);
        appData.get(query, callback);
    }

    //--------------------------------
    //  ThreadSafeCounter
    //--------------------------------

    public static class ThreadSafeCounter {
        int count;

        public synchronized void inc() {
            count++;
        }

        public synchronized void dec() {
            count--;
        }

        public synchronized int getCount() {
            return count;
        }
    }

    //--------------------------------
    //  Files
    //--------------------------------

    public void loadFile(final String filename, final DownLoadListener listener) {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();

        Query query = client.query().equals("_filename", filename);
        counter.inc();
        client.file().download(query, stream, new DownloaderProgressListener() {
            @Override
            public void progressChanged(MediaHttpDownloader mediaHttpDownloader) throws IOException {
                if (isAborted()) {
                    throw new IOException("aborted by user.");
                }
            }

            @Override
            public void onSuccess(Void aVoid) {
                if (isAborted()) {
                    listener.onLoaded(null);
                    Log.d("DietDoctor", "backend aborted: " + filename + "(" + counter.getCount() + ")");
                } else {
                    byte[] buffer = stream.toByteArray();
                    Bitmap bitmap = BitmapFactory.decodeByteArray(buffer, 0, buffer.length);
                    listener.onLoaded(bitmap);
                    if (!bitmap.isRecycled()) bitmap.recycle();
                    Log.d("DietDoctor", "backend loaded: " + filename + "(" + counter.getCount() + ")");
                }
                counter.dec();
            }

            @Override
            public void onFailure(Throwable throwable) {
                listener.onLoaded(null);
                counter.dec();
                Log.d("DietDoctor", "backend failed: " + filename + "(" + counter.getCount() + ")");
            }
        });
    }

    public int getLoadingCounter() {
        return counter.getCount();
    }

    private boolean abortLoad = false;

    public synchronized void clearAbortLoad() {
        abortLoad = false;
    }

    public synchronized void abortLoading() {
        abortLoad = true;
    }

    public synchronized boolean isAborted() {
        return abortLoad;
    }
}
