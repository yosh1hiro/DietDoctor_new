package jp.health_gate.DietDoctor.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import com.kinvey.android.callback.KinveyListCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;


/**
 * 減量行動を管理するクラス
 * <p/>
 * Created by kazhida on 2013/10/08.
 */
public class DietActions {

    public interface LoadNotify {
        void onLoaded(boolean success);
    }

    //  サーバ側には、
    //    stage
    //    star
    //    achievement
    //    max of achievementはない

    private static final long UNDECIDED_ID = -1;

    private static final String ACTIONS = "ACTIONS";

    private static final String ID = "ID";
    private static final String GROUP_ID = "GROUP_ID";
    private static final String LEVEL = "LEVEL";
    private static final String STAGE = "STAGE";
    private static final String CAPTION = "CAPTION";
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String DESCRIPTION_1 = "DESCRIPTION_1";    //  bronze medal description
    private static final String DESCRIPTION_2 = "DESCRIPTION_2";    //  silver medal description
    private static final String DESCRIPTION_3 = "DESCRIPTION_3";    //  gold medal description
    private static final String DESCRIPTION_4 = "DESCRIPTION_4";    //  master medal description
    private static final String COMMENT = "COMMENT";
    private static final String CATEGORY = "CATEGORY";         //  食事、間食、運動
    private static final String PARAM_0 = "PARAM_0"; //chart[0] or star
    private static final String PARAM_1 = "PARAM_1"; //chart[1] or achievement
    private static final String PARAM_2 = "PARAM_2"; //chart[2] or max of achievement

    public static String createTable() {
        return "create table " + ACTIONS + " (" +
                "  " + ID + " integer primary key," +
                "  " + GROUP_ID + " integer," +
                "  " + LEVEL + " integer," +
                "  " + STAGE + " integer," +
                "  " + CAPTION + " text," +
                "  " + DESCRIPTION + " text," +
                "  " + DESCRIPTION_1 + " text," +
                "  " + DESCRIPTION_2 + " text," +
                "  " + DESCRIPTION_3 + " text," +
                "  " + DESCRIPTION_4 + " text," +
                "  " + COMMENT + " text," +
                "  " + CATEGORY + " text," +
                "  " + PARAM_0 + " integer," +
                "  " + PARAM_1 + " integer," +
                "  " + PARAM_2 + " integer," +
                "  unique(" + GROUP_ID + "," + LEVEL + "));";
    }

    public static String[] addColumn2to3() {
        return new String[]{
                "alter table " + ACTIONS + " add column " + DESCRIPTION_1 + " text;",
                "alter table " + ACTIONS + " add column " + DESCRIPTION_2 + " text;",
                "alter table " + ACTIONS + " add column " + DESCRIPTION_3 + " text;",
                "alter table " + ACTIONS + " add column " + DESCRIPTION_4 + " text;",
                "alter table " + ACTIONS + " add column " + CATEGORY + " text;"
        };
    }

    private static DietActions shared;

    public static DietActions initInstance(Context context) {
        shared = new DietActions(context);
        return shared;
    }

    public static DietActions sharedInstance() {
        return shared;
    }

    private List<ChallengeItem> actions = new ArrayList<ChallengeItem>();
    private ImageCache images;
    private LoadNotify notify;

    private DietActions(Context context) {
        images = new ImageCache(context);
    }

    public class Item implements Comparable<Item> {
        protected long id = UNDECIDED_ID;
        protected int groupId;
        protected int level;        //groupItemでは0,leveledItemでは>0
        protected int stage;        //groupItemでは0,leveledItemでは、levelに連動
        protected String caption;
        protected String[] descriptions = new String[5];
        protected String comment;
        protected String category;
        protected int[] params = new int[3];

        private Item(int groupId, int level) {
            this.groupId = groupId;
            this.level = level;
        }

        private Item(Backend.DietActionRecord action) {
            id = UNDECIDED_ID;
            groupId = action.groupId;
            level = action.level;
            stage = 0;
            caption = action.caption;
            descriptions[0] = action.description;
            descriptions[1] = action.description_1;
            descriptions[2] = action.description_2;
            descriptions[3] = action.description_3;
            descriptions[4] = action.description_4;
            comment = action.comment;
            category = action.category;
            params[0] = action.param_0;
            params[1] = action.param_1;
            params[2] = action.param_2;
        }

        private Item(Cursor cursor) {
            id = cursor.getLong(0);
            groupId = cursor.getInt(1);
            level = cursor.getInt(2);
            stage = cursor.getInt(3);
            caption = cursor.getString(4);
            descriptions[0] = cursor.getString(5);
            comment = cursor.getString(6);
            params[0] = cursor.getInt(7);
            params[1] = cursor.getInt(8);
            params[2] = cursor.getInt(9);
            descriptions[1] = cursor.getString(10);
            descriptions[2] = cursor.getString(11);
            descriptions[3] = cursor.getString(12);
            descriptions[4] = cursor.getString(13);
            category = cursor.getString(14);
        }

        public long getId() {
            return id;
        }

        public int getGroupId() {
            return groupId;
        }

        protected void update(SQLiteDatabase db) {
            ContentValues cv = new ContentValues();

            cv.put(GROUP_ID, groupId);
            cv.put(LEVEL, level);
            cv.put(STAGE, stage);
            cv.put(CAPTION, caption);
            cv.put(DESCRIPTION, descriptions[0]);
            cv.put(DESCRIPTION_1, descriptions[1]);
            cv.put(DESCRIPTION_2, descriptions[2]);
            cv.put(DESCRIPTION_3, descriptions[3]);
            cv.put(DESCRIPTION_4, descriptions[4]);
            cv.put(COMMENT, comment);
            cv.put(CATEGORY, category);
            cv.put(PARAM_0, params[0]);
            cv.put(PARAM_1, params[1]);
            cv.put(PARAM_2, params[2]);

            if (getId() == UNDECIDED_ID) {
                id = db.insert(ACTIONS, null, cv);
            } else {
                db.update(ACTIONS, cv, ID + "=" + getId(), null);
            }
        }

        protected void update() {
            SQLiteDatabase db = DatabaseHelper.sharedInstance().getWritableDatabase();
            try {
                update(db);
            } finally {
                db.close();
            }
        }

        public void update(SQLiteDatabase db, Backend.DietActionRecord action) {
            groupId = action.groupId;
            level = action.level;
            caption = action.caption;
            descriptions[0] = action.description;
            descriptions[1] = action.description_1;
            descriptions[2] = action.description_2;
            descriptions[3] = action.description_3;
            descriptions[4] = action.description_4;
            comment = action.comment;
            category = action.category;
            params[0] = action.param_0;
            params[1] = action.param_1;
            params[2] = action.param_2;
            update(db);
        }

        @Override
        public int compareTo(Item another) {
            int compare = this.groupId - another.groupId;

            if (compare == 0) {
                compare = this.level - another.level;
            }

            return compare;
        }

        public Drawable getIcon() {
            return images.getMedal(groupId, stage);
        }
    }

    public class ChallengeItem extends Item {
        private List<LeveledItem> levels = new ArrayList<LeveledItem>();

        ChallengeItem(Backend.DietActionRecord action) {
            super(action);

            for (int i = 0; i < 10; i++) {
                levels.add(new LeveledItem(groupId, i + 1));
            }
        }

        ChallengeItem(Cursor cursor) {
            super(cursor);
        }

        public String getCaption() {
            return caption;
        }

        public String getDescription() {
            return descriptions[0];
        }

        public String getDescription(int stage) {
            return descriptions[stage];
        }

        public String getCategory() {
            return category;
        }

        public String getTargetUser() {
            return comment;
        }

        public int getChartValue(int i) {
            return params[i];
        }

        public List<LeveledItem> getLevels() {
            return levels;
        }

        @Override
        public Drawable getIcon() {
            return images.getMedal(groupId, 1);
        }
    }

    public class LeveledItem extends Item {
        public static final int BRONZE_STAGE = 1;
        public static final int SILVER_STAGE = 4;
        public static final int GOLD_STAGE = 7;
        public static final int MASTER_STAGE = 10;

        private LeveledItem(int groupId, int level) {
            super(groupId, level);
            if (level < SILVER_STAGE) {
                stage = 1;
            } else if (level < GOLD_STAGE) {
                stage = 2;
            } else if (level < MASTER_STAGE) {
                stage = 3;
            } else {
                stage = 4;
            }
        }

        private LeveledItem(Cursor cursor) {
            super(cursor);
        }

        public String getTitle() {
            return caption;
        }

        public String getDescription() {
            return descriptions[0];
        }

        public int getLevel() {
            return level;
        }

        public int getStage() {
            return stage;
        }

        public int getStar() {
            return params[0];
        }

        public int getAchievement() {
            return params[1];
        }

        public int getAchievementMax() {
            return params[2];
        }

        public String getAchievementComment() {
            return comment;
        }

        public void incAchievement(Calendar date) {
            params[1]++;
            update();
            Achievements.sharedInstance().addAchievement(date, this);
        }

        public boolean initAchievement(Calendar date) {
            if (params[1] == 0) {
                Achievements.sharedInstance().addAchievement(date, this);
                return DietNews.openLevelTips(groupId, level);
            } else {
                return false;
            }
        }

        public Drawable getScene() {
            return images.getScene(groupId, level);
        }
    }

    public List<ChallengeItem> getActions() {
        return actions;
    }

    public ChallengeItem findAction(int groupId) {
        if (groupId > 0) {
            for (ChallengeItem action : actions) {
                if (groupId == action.groupId) return action;
            }
        }
        return null;
    }

    public LeveledItem findAction(int groupId, int level) {
        ChallengeItem parent = findAction(groupId);
        for (LeveledItem action : parent.levels) {
            if (action.getLevel() == level) return action;
        }
        return null;
    }

    public void loadActions(LoadNotify notify) {
        actions.clear();
        this.notify = notify;

        //debug: DBを空にしてからやる
//        SQLiteDatabase db = DatabaseHelper.sharedInstance().getWritableDatabase();
//        db.delete(ACTIONS, null, null);
//        db.delete(MEDALS, null, null);

        loadFromDB();
        if (notify != null) {
            loadFromBackend();
        }
    }

    private void loadFromDB() {
        SQLiteDatabase db = DatabaseHelper.sharedInstance().getReadableDatabase();

        String[] columns = new String[]{
                ID,
                GROUP_ID,
                LEVEL,
                STAGE,
                CAPTION,
                DESCRIPTION,
                COMMENT,
                PARAM_0,
                PARAM_1,
                PARAM_2,
                DESCRIPTION_1,
                DESCRIPTION_2,
                DESCRIPTION_3,
                DESCRIPTION_4,
                CATEGORY
        };
        String order = GROUP_ID + "," + LEVEL;

        Cursor groupCursor = db.query(ACTIONS, columns, LEVEL + "=0", null, null, null, order);
        if (groupCursor.moveToFirst()) {
            do {
                actions.add(new ChallengeItem(groupCursor));
            } while (groupCursor.moveToNext());
        }
        groupCursor.close();

        Cursor leveledCursor = db.query(ACTIONS, columns, LEVEL + ">0", null, null, null, order);
        if (leveledCursor.moveToFirst()) {
            do {
                LeveledItem action = new LeveledItem(leveledCursor);
                ChallengeItem parent = findAction(action.groupId);
                if (parent != null) {
                    parent.levels.add(action);
                }
            } while (leveledCursor.moveToNext());
        }
        leveledCursor.close();

        db.close();
    }

    private void loadFromBackend() {
        Log.d("DietDoctor", "loadFromBackend");
        final Handler handler = new Handler();
        Backend.sharedInstance().loadDietActions(new KinveyListCallback<Backend.DietActionRecord>() {
            @Override
            public void onSuccess(final Backend.DietActionRecord[] dietActions) {
                Log.d("DietDoctor", "loadFromBackend: success");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SQLiteDatabase db = DatabaseHelper.sharedInstance().getWritableDatabase();
                        db.beginTransaction();
                        boolean success = true;
                        try {
                            for (Backend.DietActionRecord action : dietActions) {
                                Log.d("DietDoctor", "loadFromBackend: " + action.caption + "(" + action.groupId + "," + action.level + ")");
                                if (action.level == 0) {
                                    ChallengeItem item = find(action.groupId);
                                    if (item != null) {
                                        item.update(db, action);
                                    } else {
                                        item = new ChallengeItem(action);
                                        item.update(db);
                                        actions.add(item);
                                        images.stockMedal(db, action.groupId, 1, 4);
                                    }
                                    Collections.sort(actions);
                                } else {
                                    LeveledItem item = find(action.groupId, action.level);
                                    if (item != null) {
                                        item.update(db, action);
                                        images.storeScene(db, action.groupId, action.level, null);
                                    }
                                    DietNews.addNews(db, action);
                                }
                            }
                            db.setTransactionSuccessful();
//                            while (Backend.sharedInstance().getLoadingCounter() > 0) {
//                                Thread.sleep(50);
//                            }
//                        } catch (InterruptedException e) {
//                            Log.e("DietDoctor", "wait thread interrupted");
                        } catch (Exception e) {
                            success = false;
                        }
                        db.endTransaction();
                        db.close();
                        final boolean result = success;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                notify.onLoaded(result);
                            }
                        });
                    }
                }).start();
            }

            @Override
            public void onFailure(Throwable throwable) {
                if (throwable != null && throwable.getLocalizedMessage() != null) {
                    Log.e("DietDoctor", throwable.getLocalizedMessage());
                }
                notify.onLoaded(false);
            }
        });
    }

    private ChallengeItem find(int groupId) {
        if (groupId == 0) {
            return null;
        } else {
            for (ChallengeItem item : actions) {
                if (groupId == item.groupId) return item;
            }
            return null;
        }
    }

    private LeveledItem find(int groupId, int level) {
        ChallengeItem parent = find(groupId);
        if (parent == null) {
            return null;
        } else {
            for (LeveledItem item : parent.levels) {
                if (item.level == level) return item;
            }
            return null;
        }
    }

    public void loadImages() {
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                images.loadEmptyImages(handler);
            }
        }).start();
    }

    public void abortLoadImages() {
        Backend.sharedInstance().abortLoading();
    }
}
