package jp.health_gate.DietDoctor.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.kinvey.android.callback.KinveyListCallback;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;
import com.kinvey.java.core.KinveyClientCallback;
import jp.health_gate.DietDoctor.CalendarUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 項目達成を管理するクラス
 * <p/>
 * Created by kazhida on 2013/12/06.
 */
public class Achievements {
    private static final String ACHIEVEMENTS = "ACHIEVEMENTS";

    private static final String ID = "ID";
    private static final String YEAR = "YEAR";
    private static final String MONTH = "MONTH";
    private static final String DATE = "DATE";
    private static final String TIME = "TIME";
    private static final String GROUP_ID = "GROUP_ID";
    private static final String LEVEL = "LEVEL";
    private static final String STAGE = "STAGE";
    private static final String STAR = "STAR";
    private static final String MEDAL = "MEDAL";
    private static final String EXPORTED = "EXPORTED";

    public static String createTable() {
        return "create table " + ACHIEVEMENTS + " (" +
                "  " + ID + " integer primary key," +
                "  " + YEAR + " integer," +
                "  " + MONTH + " integer," +
                "  " + DATE + " integer," +
                "  " + TIME + " integer," +
                "  " + GROUP_ID + " integer," +
                "  " + LEVEL + " integer," +
                "  " + STAGE + " integer," +
                "  " + STAR + " integer," +
                "  " + MEDAL + " integer," +
                "  " + EXPORTED + " integer);";
    }

    private static Achievements shared;

    private Exporter exporter = new Exporter();

    private Achievements() {
    }

    /**
     * シングルトン・インスタンスの取得
     *
     * @return シングルトン・インスタンス
     */
    public static Achievements sharedInstance() {
        if (shared == null) {
            shared = new Achievements();
        }
        return shared;
    }

    /**
     * 達成レコードの追加
     * <p/>
     * DietActions.LeveledItemのincAchievement()からだけ呼ばれるようにするため
     * パッケージ・スコープになっている
     *
     * @param date   日付
     * @param action 項目
     */
    void addAchievement(Calendar date, DietActions.LeveledItem action) {
        ContentValues cv = new ContentValues();

        int y = date.get(Calendar.YEAR);
        int m = date.get(Calendar.MONTH) + 1;
        int d = date.get(Calendar.DAY_OF_MONTH);

        int medal = 0;
        int star = action.getStar();
        int bonus = 0;
        int rank = action.getStage() == 0 ? 1 : action.getStar() / action.getStage();

        if (action.getAchievement() == action.getAchievementMax()) {
            medal++;
            switch (rank) {
                case 1:
                    bonus = 5 * action.getStage();
                    break;
                case 2:
                    bonus = 10 * action.getStage();
                    break;
                case 3:
                    bonus = 20 * action.getStage();
                    break;
            }
        }

        if (action.getAchievement() == 0) {
            //  最初の選択時なので、獲得はしない
            star = 0;
            bonus = 0;
            medal = 0;
        }

        cv.put(YEAR, y);
        cv.put(MONTH, m);
        cv.put(DATE, d);
        cv.put(TIME, date.getTimeInMillis());
        cv.put(GROUP_ID, action.getGroupId());
        cv.put(LEVEL, action.getLevel());
        cv.put(STAGE, action.getStage());
        cv.put(STAR, star + bonus);
        cv.put(MEDAL, medal);
        cv.put(EXPORTED, 0);

        Log.d("DietDoctor", "inc achievement: [" + action.getGroupId() + "-" + action.getLevel() + "] s:" + star + " b:" + bonus + " m:" + medal);

        SQLiteDatabase db = DatabaseHelper.sharedInstance().getWritableDatabase();
        try {
            db.insert(ACHIEVEMENTS, null, cv);
            if (action.getAchievement() == action.getAchievementMax()) {
                checkAchievementAll(db, action.groupId, cv, rank);
            }
            exporter.export();
        } finally {
            db.close();
        }
    }

    private void addAchievement(SQLiteDatabase db, Calendar date, int groupId, int level, int star, int medal, long exported) {
        ContentValues cv = new ContentValues();

        int stage;
        if (level < DietActions.LeveledItem.SILVER_STAGE) {
            stage = 1;
        } else if (level < DietActions.LeveledItem.GOLD_STAGE) {
            stage = 2;
        } else if (level < DietActions.LeveledItem.MASTER_STAGE) {
            stage = 3;
        } else {
            stage = 4;
        }

        cv.put(YEAR, date.get(Calendar.YEAR));
        cv.put(MONTH, date.get(Calendar.MONTH) + 1);
        cv.put(DATE, date.get(Calendar.DAY_OF_MONTH));
        cv.put(TIME, date.getTimeInMillis());
        cv.put(GROUP_ID, groupId);
        cv.put(LEVEL, level);
        cv.put(STAGE, stage);
        cv.put(STAR, star);
        cv.put(MEDAL, medal);
        cv.put(EXPORTED, exported);

        db.insert(ACHIEVEMENTS, null, cv);
    }

    private void checkAchievementAll(SQLiteDatabase db, int groupId, ContentValues cv, int rank) {
        String[] columns = new String[]{
                LEVEL
        };
        String selection = GROUP_ID + "=" + groupId;
        String order = LEVEL;

        Cursor cursor = db.query(true, ACHIEVEMENTS, columns, selection, null, null, null, order, null);
        int count = 10;
        if (cursor.moveToFirst()) {
            do {
                if (cursor.getInt(0) == 0) break;
                count--;
            } while (cursor.moveToNext());
        }
        cursor.close();

        if (count == 0) {
            //  全レベル制覇
            cv.put(LEVEL, 0);
            cv.put(STAGE, 1);
            switch (rank) {
                case 1:
                    cv.put(STAR, 50);
                    break;
                case 2:
                    cv.put(STAR, 100);
                    break;
                case 3:
                    cv.put(STAR, 200);
                    break;
            }
            db.insert(ACHIEVEMENTS, null, cv);
        }
    }


    /**
     * 獲得したメダルの数の取得
     *
     * @return メダルの数
     */
    public int getMedalCount() {
        int result = 0;

        String[] columns = new String[]{
                "sum(" + MEDAL + ")"
        };

        SQLiteDatabase db = DatabaseHelper.sharedInstance().getReadableDatabase();
        Cursor cursor = db.query(ACHIEVEMENTS, columns, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            result = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return result;
    }

    /**
     * 獲得したスターの数の取得
     *
     * @return スターの数
     */
    public int getStarCount() {
        int result = 0;

        String[] columns = new String[]{
                "sum(" + STAR + ")"
        };

        SQLiteDatabase db = DatabaseHelper.sharedInstance().getReadableDatabase();
        Cursor cursor = db.query(ACHIEVEMENTS, columns, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            result = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return result;
    }

    public class Item {
        private long id;
        private Calendar date;
        private int groupId;
        private int level;
        private int stage;
        private int star;
        private int medal;
        private boolean exported;

        public Calendar getDate() {
            return date;
        }

        public int getGroupId() {
            return groupId;
        }

        public int getLevel() {
            return level;
        }

        @SuppressWarnings("unused")
        public int getStage() {
            return stage;
        }

        public int getStar() {
            return star;
        }

        public int getMedal() {
            return medal;
        }
    }

    public List<Item> getItemsForMedals() {
        List<Item> items = new ArrayList<Item>();

        String[] columns = new String[]{
                GROUP_ID,
                "MAX(" + LEVEL + ") AS " + LEVEL,
                STAGE,
                "COUNT(" + MEDAL + ") AS " + MEDAL
        };
        String selection = MEDAL + ">0";
        String group = GROUP_ID + "," + STAGE;
        String order = GROUP_ID + "," + STAGE;

        SQLiteDatabase db = DatabaseHelper.sharedInstance().getReadableDatabase();
        Cursor cursor = db.query(ACHIEVEMENTS, columns, selection, null, group, null, order);
        if (cursor.moveToFirst()) {
            do {
                Item item = new Item();
                item.groupId = cursor.getInt(0);
                item.level = cursor.getInt(1);
                item.stage = cursor.getInt(2);
                item.medal = cursor.getInt(3);
                items.add(item);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return items;
    }

    public List<Item> getAchievements(int groupId, int level) {
        List<Item> items = new ArrayList<Item>();

        String[] columns = new String[]{
                GROUP_ID,   //0
                LEVEL,      //1
                STAGE,      //2
                STAR,       //3
                MEDAL,      //4
                TIME        //5
        };
        String selection = GROUP_ID + "=" + groupId + " AND " + LEVEL + "=" + level;
        String order = TIME;

        SQLiteDatabase db = DatabaseHelper.sharedInstance().getReadableDatabase();
        Cursor cursor = db.query(true, ACHIEVEMENTS, columns, selection, null, null, null, order, null);
        if (cursor.moveToFirst()) {
            do {
                Item item = new Item();
                item.groupId = cursor.getInt(0);
                item.level = cursor.getInt(1);
                item.stage = cursor.getInt(2);
                item.star = cursor.getInt(3);
                item.medal = cursor.getInt(4);
                item.date = Calendar.getInstance();
                item.date.setTimeInMillis(cursor.getLong(5));
                items.add(item);
            } while (cursor.moveToNext());

        }
        cursor.close();
        db.close();

        return items;
    }

    public List<Item> load() {
        List<Item> items = new ArrayList<Item>();

        String[] columns = new String[]{
                GROUP_ID,   //0
                LEVEL,      //1
                STAGE,      //2
                STAR,       //3
                MEDAL,      //4
                TIME        //5
        };
        String order = TIME + " desc";

        SQLiteDatabase db = DatabaseHelper.sharedInstance().getReadableDatabase();
        Cursor cursor = db.query(ACHIEVEMENTS, columns, null, null, null, null, order);
        if (cursor.moveToFirst()) {
            do {
                Item item = new Item();
                item.groupId = cursor.getInt(0);
                item.level = cursor.getInt(1);
                item.stage = cursor.getInt(2);
                item.star = cursor.getInt(3);
                item.medal = cursor.getInt(4);
                item.date = Calendar.getInstance();
                item.date.setTimeInMillis(cursor.getLong(5));
                items.add(item);
            } while (cursor.moveToNext());

        }
        cursor.close();
        db.close();

        return items;
    }


    public List<Item> load(Calendar date) {
        List<Item> items = new ArrayList<Item>();

        int y = date.get(Calendar.YEAR);
        int m = date.get(Calendar.MONTH) + 1;
        int d = date.get(Calendar.DAY_OF_MONTH);

        String[] columns = new String[]{
                GROUP_ID,   //0
                LEVEL,      //1
                STAGE,      //2
                STAR,       //3
                MEDAL,      //4
                TIME,       //5
                YEAR,
                MONTH,
                DATE
        };
        String selection = YEAR + "=" + y + " AND " + MONTH + "=" + m + " AND " + DATE + "=" + d;
        String order = TIME;

        SQLiteDatabase db = DatabaseHelper.sharedInstance().getReadableDatabase();
        Cursor cursor = db.query(true, ACHIEVEMENTS, columns, selection, null, null, null, order, null);
        if (cursor.moveToFirst()) {
            do {
                Item item = new Item();
                item.groupId = cursor.getInt(0);
                item.level = cursor.getInt(1);
                item.stage = cursor.getInt(2);
                item.star = cursor.getInt(3);
                item.medal = cursor.getInt(4);
                item.date = Calendar.getInstance();
                item.date.setTimeInMillis(cursor.getLong(5));
                items.add(item);
            } while (cursor.moveToNext());

        }
        cursor.close();
        db.close();

        return items;
    }

    private class Exporter {

        private int busy;
        private boolean reload;

        private List<Item> load() {
            List<Item> items = new ArrayList<Item>();

            String[] columns = new String[]{
                    ID,         //0
                    GROUP_ID,   //1
                    LEVEL,      //2
                    STAGE,      //3
                    STAR,       //4
                    MEDAL,      //5
                    TIME,       //6
                    EXPORTED
            };
            String selection = EXPORTED + "=0";
            String order = TIME;

            SQLiteDatabase db = DatabaseHelper.sharedInstance().getReadableDatabase();
            Cursor cursor = db.query(ACHIEVEMENTS, columns, selection, null, null, null, order);
            if (cursor.moveToFirst()) {
                do {
                    Item item = new Item();
                    item.id = cursor.getLong(0);
                    item.groupId = cursor.getInt(1);
                    item.level = cursor.getInt(2);
                    item.stage = cursor.getInt(3);
                    item.star = cursor.getInt(4);
                    item.medal = cursor.getInt(5);
                    item.date = Calendar.getInstance();
                    item.date.setTimeInMillis(cursor.getLong(6));
                    items.add(item);
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();

            return items;
        }

        private synchronized void done() {
            if (--busy == 0 && reload) {
                reload = false;
                export();
            }
        }

        public synchronized void export() {
            final long now = Calendar.getInstance().getTimeInMillis();

            if (busy == 0) {
                List<Item> items = load();
                busy = items.size();

                final Backend.Profile profile = ActiveUser.sharedInstance().getProfile();

                for (final Item item : items) {
                    if (!item.exported) {
                        Backend.sharedInstance().insertAchievement(item.getDate(),
                                item.getGroupId(), item.getLevel(), item.getStar(), item.getMedal(),
                                new KinveyClientCallback<Backend.AchievementRecord>() {
                                    @Override
                                    public void onSuccess(Backend.AchievementRecord achievementRecord) {
                                        SQLiteDatabase db = DatabaseHelper.sharedInstance().getWritableDatabase();
                                        ContentValues cv = new ContentValues();

                                        String selection = ID + "=" + item.id;
                                        cv.put(EXPORTED, now);

                                        db.update(ACHIEVEMENTS, cv, selection, null);
                                        item.exported = true;

                                        Log.e("DietDoctor", "Achievement export and updated");

                                        profile.star_count += item.getStar();
                                        profile.medal_count += item.getMedal();

                                        ActiveUser.sharedInstance().updateProfile(profile, new KinveyUserCallback() {
                                            @Override
                                            public void onSuccess(User user) {
                                                Log.e("DietDoctor", "Achievement updated profile");
                                            }

                                            @Override
                                            public void onFailure(Throwable throwable) {
                                                Log.e("DietDoctor", "Achievement update profile error :" + (throwable == null ? "null" : throwable.getLocalizedMessage()));
                                            }
                                        });

                                        done();
                                    }

                                    @Override
                                    public void onFailure(Throwable throwable) {
                                        Log.e("DietDoctor", "Achievement export error :" + (throwable == null ? "null" : throwable.getLocalizedMessage()));
                                        done();
                                    }
                                });
                    }
                }
            } else {
                reload = true;
            }
        }
    }

    private class Importer {
        Calendar lastImport = Calendar.getInstance();

        Importer() {
            SQLiteDatabase db = DatabaseHelper.sharedInstance().getReadableDatabase();
            String[] columns = new String[]{
                    EXPORTED
            };
            Cursor cursor = db.query(ACHIEVEMENTS, columns, null, null, null, null, EXPORTED + " desc");
            if (cursor.moveToFirst()) {
                lastImport.setTimeInMillis(cursor.getLong(0));
            } else {
                lastImport.setTimeInMillis(0);
            }
            cursor.close();
            db.close();
        }

        void importFromBackend(final Runnable postProcess) {
            final long now = Calendar.getInstance().getTimeInMillis();

            Backend.sharedInstance().loadAchievements(lastImport, new KinveyListCallback<Backend.AchievementRecord>() {
                @Override
                public void onSuccess(Backend.AchievementRecord[] records) {
                    SQLiteDatabase db = DatabaseHelper.sharedInstance().getWritableDatabase();
                    db.beginTransaction();
                    try {
                        Calendar date = CalendarUtils.dateCalendar();
                        for (Backend.AchievementRecord record : records) {
                            date.set(record.year, record.month - 1, record.date);
                            addAchievement(db, date, record.groupId, record.level, record.star, record.medal, now);
                        }
                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                        db.close();
                    }

                    if (postProcess != null) postProcess.run();
                }

                @Override
                public void onFailure(Throwable throwable) {
                    Log.e("DietDoctor", "Achievement importer error :" + (throwable == null ? "null" : throwable.getLocalizedMessage()));
                    if (postProcess != null) postProcess.run();
                }
            });
        }
    }

    public void deleteAll() {
        SQLiteDatabase db = DatabaseHelper.sharedInstance().getWritableDatabase();
        db.delete(ACHIEVEMENTS, null, null);
        db.close();
    }

    public void importFromBackend(Runnable postProcess) {
        new Importer().importFromBackend(postProcess);
    }
}
