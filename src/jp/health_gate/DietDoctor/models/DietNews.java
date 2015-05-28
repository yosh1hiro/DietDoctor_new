package jp.health_gate.DietDoctor.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * ダイエット通信
 * <p/>
 * Created by kazhida on 2013/12/17.
 */
public class DietNews {

    public static final String TIPS_TITLE = "TIPS_TITLE";
    public static final String TIPS_CONTENT = "TIPS_CONTENT";

    private static final String DIET_NEWS = "DIET_NEWS";

    private static final String ID = "ID";
    private static final String SERVER_ID = "SERVER_ID";
    private static final String GROUP_ID = "GROUP_ID";
    private static final String LEVEL = "LEVEL";
    private static final String CLOSED = "CLOSED";
    private static final String OPENED = "OPENED";
    private static final String TITLE = "TITLE";
    private static final String CONTENT = "CONTENT";
    private static final String UPDATED_AT = "UPDATED_AT";

//    public static int NON_CATEGORY_GROUP_ID = 0;

    public static String createTable() {
        return "create table " + DIET_NEWS + " (" +
                "  " + ID + " integer primary key," +
                "  " + SERVER_ID + " text," +
                "  " + GROUP_ID + " integer," +
                "  " + LEVEL + " integer," +
                "  " + CLOSED + " integer," +
                "  " + OPENED + " integer," +
                "  " + TITLE + " text," +
                "  " + CONTENT + " text," +
                "  " + UPDATED_AT + " text," +
                "  unique(" + SERVER_ID + "));";
    }

    public static class Item {
        private long id;
        private int groupId;
        private int level;
        private boolean opened;
        private String title;
        private String content;
        private Calendar updatedAt = Calendar.getInstance();

        public int getGroupId() {
            return groupId;
        }

        public int getLevel() {
            return level;
        }

        public boolean isOpened() {
            return opened;
        }

        public void setRead() {
            if (!opened) {
                ContentValues cv = new ContentValues();
                cv.put(OPENED, true);
                DatabaseHelper.sharedInstance().getWritableDatabase().update(DIET_NEWS, cv, ID + "=" + id, null);

                opened = true;
            }
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public Calendar getDate() {
            return updatedAt;
        }

        public boolean match(CharSequence constraint) {
            return constraint == null || constraint.length() > 0 || title.contains(constraint) || content.contains(constraint);
        }
    }

    private static SimpleDateFormat dateFormat = new SimpleDateFormat(Backend.ISO8601UTC);

    public static void addNews(SQLiteDatabase db, Backend.DietActionRecord action) {
        String[] columns = new String[]{
                SERVER_ID
        };
        String selection = SERVER_ID + "='" + action.serverId + "'";

        ContentValues cv = new ContentValues();

        cv.put(TITLE, "Tips: " + action.caption);
        cv.put(CONTENT, action.tips);
        cv.put(UPDATED_AT, action.meta.getLastModifiedTime());

        Cursor cursor = db.query(DIET_NEWS, columns, selection, null, null, null, null);
        if (cursor.moveToFirst()) {
            db.update(DIET_NEWS, cv, selection, null);
        } else {
            cv.put(SERVER_ID, action.serverId);
            cv.put(GROUP_ID, action.groupId);
            cv.put(LEVEL, action.level);
            cv.put(CLOSED, 1);
            cv.put(OPENED, 0);
            db.insert(DIET_NEWS, null, cv);
        }
        cursor.close();
    }

    static boolean openLevelTips(int groupId, int level) {
        SQLiteDatabase db = DatabaseHelper.sharedInstance().getWritableDatabase();

        String selection = GROUP_ID + "=" + groupId + " AND " + LEVEL + "=" + level;

        ContentValues cv = new ContentValues();
        cv.put(CLOSED, 0);

        return db.update(DIET_NEWS, cv, selection, null) > 0;
    }

    public static Item getLevelTips(int groupId, int level) {
        SQLiteDatabase db = DatabaseHelper.sharedInstance().getReadableDatabase();

        String[] columns = new String[]{
                ID,
                GROUP_ID,
                LEVEL,
                OPENED,
                TITLE,
                CONTENT,
                UPDATED_AT
        };
        String selection = GROUP_ID + "=" + groupId + " AND " + LEVEL + "=" + level;

        Cursor cursor = db.query(DIET_NEWS, columns, selection, null, null, null, null);
        if (cursor.moveToFirst()) {
            Item item = new Item();
            item.id = cursor.getLong(0);
            item.groupId = cursor.getInt(1);
            item.level = cursor.getInt(2);
            item.opened = cursor.getInt(3) > 0;
            item.title = cursor.getString(4);
            item.content = cursor.getString(5);
            try {
                item.updatedAt.setTime(dateFormat.parse(cursor.getString(6)));
            } catch (ParseException e) {
                //無視
                //  なので、updatedAtはあまり信用しない（ソートに使うくらい）
            }
            return item;
        } else {
            return null;
        }
    }

    private List<Item> items = new ArrayList<Item>();

    public DietNews() {
        SQLiteDatabase db = DatabaseHelper.sharedInstance().getReadableDatabase();

        String[] columns = new String[]{
                ID,
                GROUP_ID,
                LEVEL,
                OPENED,
                TITLE,
                CONTENT,
                UPDATED_AT
        };
        String selection = CLOSED + "=0";
        String order = OPENED + " desc," + UPDATED_AT;

        Cursor cursor = db.query(DIET_NEWS, columns, selection, null, null, null, order);
        if (cursor.moveToFirst()) {
            do {
                Item item = new Item();
                item.id = cursor.getLong(0);
                item.groupId = cursor.getInt(1);
                item.level = cursor.getInt(2);
                item.opened = cursor.getInt(3) > 0;
                item.title = cursor.getString(4);
                item.content = cursor.getString(5);
                try {
                    item.updatedAt.setTime(dateFormat.parse(cursor.getString(6)));
                } catch (ParseException e) {
                    //無視
                    //  なので、updatedAtはあまり信用しない（ソートに使うくらい）
                }
                items.add(item);
            } while (cursor.moveToNext());
        }
    }

    public List<Item> getItems() {
        return items;
    }

    public Item find(int groupId) {
        for (Item item : items) {
            if (item.getGroupId() == groupId) {
                return item;
            }
        }
        return null;
    }

    public Item findUnread(int groupId) {
        for (Item item : items) {
            if (item.getGroupId() == groupId && (!item.isOpened())) {
                return item;
            }
        }
        return null;
    }
}
