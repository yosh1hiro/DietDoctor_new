package jp.health_gate.DietDoctor.models;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * ローカルDB管理用クラス
 * <p/>
 * Created by kazhida on 2013/11/27.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "dd.db";
    private static final int DB_VERSION = 3;

    private static DatabaseHelper shared;

    public static DatabaseHelper initInstance(Context context) {
        shared = new DatabaseHelper(context);
        return shared;
    }

    public static DatabaseHelper sharedInstance() {
        return shared;
    }

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DietActions.createTable());
        db.execSQL(ImageCache.createTable());
        db.execSQL(Weights.createTable());
        db.execSQL(Achievements.createTable());
        db.execSQL(DietNews.createTable());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL(DietNews.createTable());
        }
        if (oldVersion < 3) {
            for (String sql : DietActions.addColumn2to3()) {
                db.execSQL(sql);
            }
        }
    }

    @SuppressWarnings("unused")
    public static boolean getAsBoolean(Cursor cursor, String column) {
        return getAsInt(cursor, column) != 0;
    }

    @SuppressWarnings("unused")
    public static int getAsInt(Cursor cursor, String column) {
        int index = cursor.getColumnIndex(column);
        return cursor.getInt(index);
    }

    @SuppressWarnings("unused")
    public static long getAsLong(Cursor cursor, String column) {
        int index = cursor.getColumnIndex(column);
        return cursor.getLong(index);
    }

    @SuppressWarnings("unused")
    public static float getAsFloat(Cursor cursor, String column) {
        int index = cursor.getColumnIndex(column);
        return cursor.getFloat(index);
    }

    @SuppressWarnings("unused")
    public static String getAsString(Cursor cursor, String column) {
        int index = cursor.getColumnIndex(column);
        return cursor.getString(index);
    }
}
