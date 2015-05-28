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
 * 体重管理用クラス
 * <p/>
 * Created by kazhida on 2013/12/02.
 */
public class Weights {

    private static final String WEIGHTS = "WEIGHTS";

    private static final String ID = "ID";
    private static final String YEAR = "YEAR";
    private static final String MONTH = "MONTH";
    private static final String DATE = "DATE";
    private static final String TIME = "TIME";
    private static final String WEIGHT = "WEIGHT";
    private static final String MEMO = "MEMO";
    private static final String EXPORTED = "EXPORTED";

    public static String createTable() {
        return "create table " + WEIGHTS + " (" +
                "  " + ID + " integer primary key," +
                "  " + YEAR + " integer," +
                "  " + MONTH + " integer," +
                "  " + DATE + " integer," +
                "  " + TIME + " integer," +
                "  " + WEIGHT + " real," +
                "  " + MEMO + " text," +
                "  " + EXPORTED + " integer," +
                "  unique(" + YEAR + "," + MONTH + "," + DATE + "));";
    }

    private static final String[] columns = new String[]{
            ID,
            YEAR,
            MONTH,
            DATE,
            TIME,
            WEIGHT,
            MEMO,
            EXPORTED
    };

    public class Item {
        private long id;
        private Calendar date = Calendar.getInstance();
        private float weight;
        private float rate;     //  export時のみ使用
        private String memo;
        private boolean exported;

        public Calendar getDate() {
            return date;
        }

        public float getWeight() {
            return weight;
        }

        public String getMemo() {
            return memo;
        }

        public boolean hasMemo() {
            return memo != null && memo.length() > 0;
        }

        public boolean isSameDay(Calendar date) {
            return this.date.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                    this.date.get(Calendar.MONTH) == date.get(Calendar.MONTH) &&
                    this.date.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH);
        }
    }

    private static Weights shared;

    private Exporter exporter = new Exporter();

    private Weights() {
    }

    public static Weights sharedInstance() {
        if (shared == null) {
            shared = new Weights();
        }
        return shared;
    }

    private void addWeight(SQLiteDatabase db0, Calendar date, float weight, String memo, long exported) {
        if (weight > 0) {
            ContentValues cv = new ContentValues();

            int y = date.get(Calendar.YEAR);
            int m = date.get(Calendar.MONTH) + 1;
            int d = date.get(Calendar.DAY_OF_MONTH);

            cv.put(WEIGHT, weight);
            cv.put(MEMO, memo);
            cv.put(EXPORTED, exported);

            SQLiteDatabase db = db0 != null ? db0 : DatabaseHelper.sharedInstance().getWritableDatabase();
            try {
                String selection = YEAR + "=? AND " + MONTH + "=? AND " + DATE + "=?";
                String[] args = new String[]{
                        "" + y,
                        "" + m,
                        "" + d
                };
                Cursor cursor = db.query(WEIGHTS, columns, selection, args, null, null, TIME + " desc");
                if (cursor.moveToFirst()) {
                    db.update(WEIGHTS, cv, selection, args);
                } else {
                    cv.put(YEAR, y);
                    cv.put(MONTH, m);
                    cv.put(DATE, d);
                    cv.put(TIME, date.getTimeInMillis());
                    db.insert(WEIGHTS, null, cv);
                }
                if (exported == 0) {
                    exporter.export();
                }
            } finally {
                if (db != db0) db.close();
            }
        }
    }

    public synchronized void addWeight(Calendar date, float weight, String memo) {
        addWeight(null, date, weight, memo, 0);
    }

    public synchronized Item getRecentWeight(Calendar limit) {
        Item item = null;

        SQLiteDatabase db = DatabaseHelper.sharedInstance().getReadableDatabase();
        try {
            Calendar calendar = CalendarUtils.dateCalendar(limit, 1);

            String where = limit == null ? null : TIME + "<" + calendar.getTimeInMillis();
            Cursor cursor = db.query(WEIGHTS, columns, where, null, null, null, TIME + " desc");
            if (cursor.moveToFirst()) {
                item = new Item();
                item.id = DatabaseHelper.getAsLong(cursor, ID);
                item.date.setTimeInMillis(DatabaseHelper.getAsLong(cursor, TIME));
                item.weight = DatabaseHelper.getAsFloat(cursor, WEIGHT);
                item.memo = DatabaseHelper.getAsString(cursor, MEMO);
            }
        } finally {
            db.close();
        }
        return item;
    }

    public float getRecentWeight() {
        Item item = getRecentWeight(Calendar.getInstance());
        return item == null ? 0 : item.weight;
    }

    public float getLastWeekWeight() {
        Calendar lastWeek = Calendar.getInstance();
        lastWeek.add(Calendar.DATE, -7);
        Item item = getRecentWeight(lastWeek);
        return item == null ? 0 : item.weight;
    }

    public synchronized Item getWeight(Calendar calendar) {
        Item item = null;
        SQLiteDatabase db = DatabaseHelper.sharedInstance().getReadableDatabase();
        try {
            String selection = YEAR + "=? AND " + MONTH + "=? AND " + DATE + "=?";
            String[] args = new String[]{
                    "" + calendar.get(Calendar.YEAR),
                    "" + (calendar.get(Calendar.MONTH) + 1),
                    "" + calendar.get(Calendar.DAY_OF_MONTH)
            };
            Cursor cursor = db.query(WEIGHTS, columns, selection, args, null, null, TIME + " desc");
            if (cursor.moveToFirst()) {
                item = new Item();
                item.id = DatabaseHelper.getAsLong(cursor, ID);
                item.date.setTimeInMillis(DatabaseHelper.getAsLong(cursor, TIME));
                item.weight = DatabaseHelper.getAsFloat(cursor, WEIGHT);
                item.memo = DatabaseHelper.getAsString(cursor, MEMO);
            }
            cursor.close();
        } finally {
            db.close();
        }
        return item;
    }

    public List<Item> load() {
        List<Item> items = new ArrayList<Item>();

        SQLiteDatabase db = DatabaseHelper.sharedInstance().getReadableDatabase();
        try {
            Cursor cursor = db.query(WEIGHTS, columns, null, null, null, null, TIME + " desc");
            if (cursor.moveToFirst()) {
                do {
                    Item item = new Item();
                    item.id = DatabaseHelper.getAsLong(cursor, ID);
                    item.date.setTimeInMillis(DatabaseHelper.getAsLong(cursor, TIME));
                    item.weight = DatabaseHelper.getAsFloat(cursor, WEIGHT);
                    item.memo = DatabaseHelper.getAsString(cursor, MEMO);
                    items.add(item);
                } while (cursor.moveToNext());
            }
            cursor.close();
        } finally {
            db.close();
        }

        return items;
    }

    public static float parseWeight(String weight) {
        if (weight == null) {
            return 0.0f;
        } else {
            try {
                return Float.parseFloat(weight);
            } catch (NumberFormatException e) {
                return 0.0f;
            }
        }
    }

    private class Exporter {

        private int busy;
        private boolean reload;

        private List<Item> load() {
            List<Item> items = new ArrayList<Item>();

            String order = TIME;

            float lastWeight = 0;
            long lastTime = 0;

            SQLiteDatabase db = DatabaseHelper.sharedInstance().getReadableDatabase();
            try {
                Cursor cursor = db.query(WEIGHTS, columns, null, null, null, null, order);
                if (cursor.moveToFirst()) {
                    do {
                        float weight = DatabaseHelper.getAsFloat(cursor, WEIGHT);
                        long time = DatabaseHelper.getAsLong(cursor, TIME);
                        float rate = 0;
                        if (lastWeight > 0 && lastTime > 0) {
                            float dw = weight - lastWeight;
                            long dt = time - lastTime;
                            if (dt > 0) {
                                rate = dw / (dt / (24L * 60 * 60 * 1000));
                            }
                        }
                        lastWeight = weight;
                        lastTime = time;
                        if (!DatabaseHelper.getAsBoolean(cursor, EXPORTED)) {
                            Item item = new Item();
                            item.id = DatabaseHelper.getAsLong(cursor, ID);
                            item.date.setTimeInMillis(time);
                            item.weight = weight;
                            item.rate = rate;
                            item.memo = DatabaseHelper.getAsString(cursor, MEMO);
                            items.add(item);
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            } finally {
                db.close();
            }
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

                for (final Item item : items) {
                    if (!item.exported) {
                        Backend.sharedInstance().insertWeight(item.getDate(),
                                item.getWeight(), item.rate, item.getMemo(),
                                new KinveyClientCallback<Backend.WeightRecord>() {
                                    @Override
                                    public void onSuccess(Backend.WeightRecord weightRecord) {
                                        SQLiteDatabase db = DatabaseHelper.sharedInstance().getWritableDatabase();
                                        ContentValues cv = new ContentValues();

                                        String selection = ID + "=" + item.id;
                                        cv.put(EXPORTED, now);
                                        db.update(WEIGHTS, cv, selection, null);
                                        item.exported = true;
                                        Log.d("DietDoctor", "Weight export and updated");

                                        final Backend.Profile profile = ActiveUser.sharedInstance().getProfile();

                                        double h = profile.height / 100;
                                        profile.bmi = item.weight / (h * h);
                                        profile.loss_rate = item.rate;
                                        profile.current_weight = item.weight;

                                        ActiveUser.sharedInstance().updateProfile(profile, new KinveyUserCallback() {
                                            @Override
                                            public void onSuccess(User user) {
                                                Log.d("DietDoctor", "Weights updated profile");
                                            }

                                            @Override
                                            public void onFailure(Throwable throwable) {
                                                Log.e("DietDoctor", "Weights update profile error :" + (throwable == null ? "null" : throwable.getLocalizedMessage()));
                                            }
                                        });

                                        done();
                                    }

                                    @Override
                                    public void onFailure(Throwable throwable) {
                                        Log.e("DietDoctor", "Weight export error :" + (throwable == null ? "null" : throwable.getLocalizedMessage()));
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
            Cursor cursor = db.query(WEIGHTS, columns, null, null, null, null, EXPORTED + " desc");
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

            Backend.sharedInstance().loadWeights(lastImport, new KinveyListCallback<Backend.WeightRecord>() {
                @Override
                public void onSuccess(Backend.WeightRecord[] weightRecords) {
                    SQLiteDatabase db = DatabaseHelper.sharedInstance().getWritableDatabase();
                    db.beginTransaction();
                    try {
                        Calendar date = CalendarUtils.dateCalendar();
                        for (Backend.WeightRecord record : weightRecords) {
                            date.set(record.year, record.month - 1, record.date);
                            addWeight(db, date, record.weight, record.memo, now);
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
                    Log.e("DietDoctor", "Weight importer error :" + (throwable == null ? "null" : throwable.getLocalizedMessage()));
                    if (postProcess != null) postProcess.run();
                }
            });
        }
    }

    public void deleteAll() {
        SQLiteDatabase db = DatabaseHelper.sharedInstance().getWritableDatabase();
        db.delete(WEIGHTS, null, null);
        db.close();
    }

    public void importFromBackend(Runnable postProcess) {
        new Importer().importFromBackend(postProcess);
    }
}
