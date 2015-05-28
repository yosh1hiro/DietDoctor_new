package jp.health_gate.DietDoctor.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import jp.health_gate.DietDoctor.R;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * メダルの画像を管理するクラス
 * <p/>
 * Created by kazhida on 2013/12/06.
 */
class ImageCache {

    private static final String IMAGE_CACHE = "IMAGE_CACHE";

    private static final String KEY_NAME = "KEY_NAME";
    private static final String IMAGE = "IMAGE";

    private static final String MEDAL_PREFIX = "medal_";
    private static final String SCENE_PREFIX = "scene_";
    private static final String MEDAL_PRO = "pro_";

    public static String createTable() {
        return "create table " + IMAGE_CACHE + " (" +
                "  " + KEY_NAME + " text primary key," +
                "  " + IMAGE + " blob," +
                "  unique(" + KEY_NAME + "));";
    }

    private Context context;
    private Map<String, Drawable> cache = new HashMap<String, Drawable>();

    ImageCache(Context context) {
        this.context = context;
    }

    //------------------
    //  メダル関連
    //

    private String medalKey(int groupId, int stage) {
        return MEDAL_PREFIX + groupId + "-" + stage;
    }

    private String medalPro(int groupId, int stage) {
        return MEDAL_PREFIX + MEDAL_PRO + groupId + "-" + stage;
    }

    public Drawable getMedal(int groupId, int stage) {
        return getImage(medalKey(groupId, stage), true, R.drawable.ic_loading_action);
    }

    public void stockMedal(SQLiteDatabase db, int groupId, int stageMin, int stageMax) {
        for (int stage = stageMin; stage <= stageMax; stage++) {
            storeImage(db, medalKey(groupId, stage), null);
            storeImage(db, medalPro(groupId, stage), null);
        }
    }

    //------------------
    //  レベル関連
    //

    private String sceneKey(int groupId, int level) {
        return SCENE_PREFIX + groupId + "-" + level;
    }

    public Drawable getScene(int groupId, int level) {
        return getImage(sceneKey(groupId, level), false, R.drawable.dummy_scene);
    }

    public void storeScene(SQLiteDatabase db, int groupId, int level, Bitmap bitmap) {
        storeImage(db, sceneKey(groupId, level), bitmap);
    }

    //------------------
    //  ローカルDBから取り出し
    //

    private Drawable getImage(String key, boolean needCache, int dummyId) {
        Drawable result = cache.get(key);

        if (result == null) {
            SQLiteDatabase db = DatabaseHelper.sharedInstance().getReadableDatabase();

            String[] columns = new String[]{IMAGE};
            String selection = KEY_NAME + "='" + key + "'";

            Cursor cursor = db.query(IMAGE_CACHE, columns, selection, null, null, null, null);

            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    result = new BitmapDrawable(context.getResources(), bitmap);
                }
                if (result == null) {
                    result = context.getResources().getDrawable(dummyId);
                } else if (needCache) {
                    cache.put(key, result);
                }
            } else {
                result = context.getResources().getDrawable(dummyId);
            }
            cursor.close();
            db.close();
        }

        return result;
    }

    //------------------
    //  ローカルDBに保存
    //

    private void storeImage(SQLiteDatabase db, String key, Bitmap bitmap) {
        String[] columns = new String[]{KEY_NAME};
        String selection = KEY_NAME + "='" + key + "'";

        ContentValues cv = new ContentValues();

        if (bitmap != null) {
            Bitmap.CompressFormat format = Bitmap.CompressFormat.PNG;
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(format, 100, stream);
            bitmap.recycle();
            cv.put(IMAGE, stream.toByteArray());
        }

        Cursor cursor = db.query(IMAGE_CACHE, columns, selection, null, null, null, null);
        if (!cursor.moveToFirst()) {
            //  レコードがなければ作る
            cv.put(KEY_NAME, key);
            db.insert(IMAGE_CACHE, null, cv);
        } else if (bitmap != null) {
            //  画像の更新
            db.update(IMAGE_CACHE, cv, selection, null);
        }
        cursor.close();
    }

    private class ImageNameMatcher {

        private Matcher matcher;

        ImageNameMatcher(String filename, String pattern) {
            matcher = Pattern.compile(pattern).matcher(filename);
            Log.d("DietDoctor", "filename=" + filename + " pattern=" + pattern);
        }

        public String prefix = "";
        public String pro = "";
        public String groupId = "";

        private String getGroup(int i) {
            String group = matcher.group(i);
            if (group == null) group = "";
            return group;
        }

        boolean matches() {
            if (matcher.matches()) {
                prefix = getGroup(1);
                pro = getGroup(2);
                groupId = getGroup(3);
                return true;
            } else {
                return false;
            }
        }

        public String fileBody() {
            return prefix + pro + groupId;
        }
    }

    private void storeImages(SQLiteDatabase db, String filename, Bitmap bitmaps) {
        ImageNameMatcher matcher = new ImageNameMatcher(filename, "([a-zA-Z]+_)(" + MEDAL_PRO + ")?([0-9]+)\\.png");

        Log.d("DietDoctor", "storeImages: " + filename);

        if (matcher.matches()) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            Log.d("DietDoctor", "storeImages: prefix=" + matcher.prefix + " groupId=" + matcher.groupId + " pro=" + matcher.pro);

            if (MEDAL_PREFIX.equals(matcher.prefix)) {
                //  横に4つ並んでいる
                int w = bitmaps.getWidth() / 4;
                int h = bitmaps.getHeight();
                Rect src = new Rect(0, 0, w, h);
                Rect dst = new Rect(0, 0, w, h);

                for (int i = 1; i <= 4; i++) {
                    Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawBitmap(bitmaps, src, dst, paint);
                    storeImage(db, matcher.fileBody() + "-" + i, bitmap);
                    src.left += w;
                    src.right += w;
                }
            }
            if (SCENE_PREFIX.equals(matcher.prefix)) {
                //  縦に10個並んでいる
                int w = bitmaps.getWidth();
                int h = bitmaps.getHeight() / 10;
                Rect src = new Rect(0, 0, w, h);
                Rect dst = new Rect(0, 0, w, h);

                for (int i = 1; i <= 10; i++) {
                    Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    canvas.drawBitmap(bitmaps, src, dst, paint);
                    storeImage(db, matcher.fileBody() + "-" + i, bitmap);
                    src.top += h;
                    src.bottom += h;
                }
            }
        }
    }

    /**
     * まだ読み込んでいない画像を読み込む処理
     *
     * @param handler 通知時に使用するハンドラ
     */
    public void loadEmptyImages(final Handler handler) {
        Backend.sharedInstance().clearAbortLoad();
        SQLiteDatabase db = DatabaseHelper.sharedInstance().getReadableDatabase();
        List<String> files = new ArrayList<String>();

        String[] columns = new String[]{KEY_NAME};
        String selection = IMAGE + " is null";
        String order = KEY_NAME;

        //  空のレコードを探す
        try {
            Cursor cursor = db.query(IMAGE_CACHE, columns, selection, null, null, null, order);
            if (cursor.moveToFirst()) {
                do {
                    //  同一のチャレンジ項目の画像をまとめて一枚にしたので、
                    //  ややこしいことになっている
                    ImageNameMatcher matcher = new ImageNameMatcher(cursor.getString(0), "([a-zA-Z]+_)(" + MEDAL_PRO + ")?([0-9]+)-[0-9]*");
                    if (matcher.matches()) {
                        String filename = matcher.fileBody() + ".png";
                        if (!files.contains(filename)) {
                            files.add(filename);
                        }
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            db.close();
        }

        final Backend.ThreadSafeCounter counter = new Backend.ThreadSafeCounter();

        for (final String filename : files) {
            Log.d("DietDoctor", "loading :" + filename);
            //  Backendから取り出す
            counter.inc();
            Backend.sharedInstance().loadFile(filename, new Backend.DownLoadListener() {
                @Override
                public void onLoaded(Bitmap bitmap) {
                    counter.dec();
                    if (bitmap != null) {
                        Log.d("DietDoctor", "loaded  :" + filename);
                        SQLiteDatabase db = DatabaseHelper.sharedInstance().getWritableDatabase();
                        try {
                            storeImages(db, filename, bitmap);
                            Log.d("DietDoctor", "stored :" + filename);
                        } finally {
                            db.close();
                        }
                    } else {
                        Log.d("DietDoctor", "error  :" + filename);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (!Backend.sharedInstance().isAborted()) {
                                    Toast.makeText(context, filename + "の読み込みができませんでした。", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            });
            while (counter.getCount() > 8) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    //nop
                }
            }
        }
    }
}
