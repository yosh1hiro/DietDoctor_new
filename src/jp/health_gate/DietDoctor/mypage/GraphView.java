package jp.health_gate.DietDoctor.mypage;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jp.health_gate.DietDoctor.R;
import jp.health_gate.DietDoctor.models.Achievements;
import jp.health_gate.DietDoctor.models.ActiveUser;
import jp.health_gate.DietDoctor.models.Weights;

/**
 * Created by yoshihiro on 2014/10/27.
 */
public class GraphView extends SurfaceView implements SurfaceHolder.Callback {

    public enum Term {
        W1,
        M1,
        M3
    }

    @SuppressWarnings("unused")
    public GraphView(Context context) {
        super(context);
        init();
    }

    @SuppressWarnings("unused")
    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @SuppressWarnings("unused")
    public GraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private Drawer drawer;
    private List<Weights.Item> weightsAll;
    private List<Achievements.Item> achievementsAll;

    private void init() {
        getHolder().addCallback(this);
        setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        setFocusable(true);
        drawer = new Drawer();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        drawer.width = width;
        drawer.height = height;
        redraw();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    public void setSource(Weights weights, Achievements achievements) {
        weightsAll = weights.load();
        achievementsAll = achievements.load();
        drawer.targetWeight = ActiveUser.sharedInstance().getProfile().getTargetWeight();
        redraw();
    }

    private void redraw() {
        Canvas canvas = getHolder().lockCanvas();
        if (canvas != null) {
            try {
                drawer.draw(canvas);
            } finally {
                getHolder().unlockCanvasAndPost(canvas);
            }
        }
    }

    public void setTerm(Term term) {
        drawer.setTerm(term);
    }

    private static long julianDay(Calendar calendar) {
        int y = calendar.get(Calendar.YEAR);
        int m = calendar.get(Calendar.MONTH);
        int d = calendar.get(Calendar.DAY_OF_MONTH);

        if (m <= 1) {
            y -= 1;
            m += 12;
        }

        y = (int) Math.floor(365.25 * y) + y / 400 - y / 100;
        m = (int) Math.floor(30.59 * (m - 1));
        d = d - 678912;

        return y + m + d + 2400001;
    }

    private class Drawer {
        float width;
        float height;
        Term term;
        Calendar today = Calendar.getInstance();
        Calendar origin = Calendar.getInstance();
        float minWeight;
        float maxWeight;
        int[] stars = null;
        int maxStars;
        Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint weightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint targetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint achievementPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        float targetWeight;
        List<Weights.Item> weights = new ArrayList<Weights.Item>();
        List<Achievements.Item> achievements = new ArrayList<Achievements.Item>();
        float density = getResources().getDisplayMetrics().density;


        //  正直言って、お世辞にもきれいに書けてないコードなので、
        //  お恥ずかしい。(*>_<*);

        static final float TOP_MARGIN = 60.0f;
        static final float BOTTOM_MARGIN = 20.0f;
        static final float LEFT_MARGIN = 120.0f;
        static final float RIGHT_MARGIN = 120.0f;
        static final float DOT_RADIUS = 8.0f;

        Drawer() {
            axisPaint.setStyle(Paint.Style.STROKE);
            axisPaint.setColor(Color.GRAY);
            axisPaint.setStrokeWidth(2.0f);
            weightPaint.setStyle(Paint.Style.STROKE);
            weightPaint.setStrokeWidth(3.0f);
            weightPaint.setColor(getResources().getColor(R.color.chart_weight));
            achievementPaint.setStyle(Paint.Style.FILL);
            achievementPaint.setColor(getResources().getColor(R.color.chart_stars));
            targetPaint.setStyle(Paint.Style.STROKE);
            targetPaint.setColor(getResources().getColor(R.color.chart_target));
            targetPaint.setStrokeWidth(3.0f);
            setTerm(Term.W1);
        }

        PointF point(long date, long first, long last, float value, float min, float max) {
            float w = width - (LEFT_MARGIN + RIGHT_MARGIN);
            float h = height - (TOP_MARGIN + BOTTOM_MARGIN);
            float x = w / (last - first + 1) * (date - first) + LEFT_MARGIN;
            float y = (height - BOTTOM_MARGIN) - (h / (max - min) * (value - min));
            x += w / (last - first + 1) / 2;
            return new PointF(x, y);
        }

        void setTerm(Term term) {
            this.term = term;
            weights.clear();
            achievements.clear();

            origin.setTimeInMillis(today.getTimeInMillis());
            switch (term) {
                case W1:
                    origin.add(Calendar.DATE, -6);
                    prepare();
                    break;
                case M1:
                    origin.add(Calendar.MONDAY, -1);
                    origin.add(Calendar.DATE, 1);
                    prepare();
                    break;
                case M3:
                    origin.add(Calendar.MONDAY, -3);
                    origin.add(Calendar.DATE, 1);
                    prepare();
                    break;
            }
            redraw();
        }

        float adjustMin(float w) {
            if (w < 20) w = 20;
            return (float) (Math.floor(w / 2) * 2);
        }

        float adjustMax(float w) {
            if (w > 300) w = 300;
            return (float) (Math.ceil(w / 2) * 2);
        }

        int adjustStar(int n) {
            if (n < 10) n = 10;
            return n / 10 * 10;
        }

        void prepare() {
            long first = julianDay(origin);
            long last = julianDay(today);

            minWeight = targetWeight;
            maxWeight = targetWeight;

            if (weightsAll != null && weights.isEmpty()) {
                for (Weights.Item item : weightsAll) {
                    long date = julianDay(item.getDate());
                    if (first <= date && date <= last) {
                        if (minWeight > item.getWeight()) minWeight = item.getWeight();
                        if (maxWeight < item.getWeight()) maxWeight = item.getWeight();
                        weights.add(item);
                    }
                }
            }
            minWeight = adjustMin(minWeight);
            maxWeight = adjustMax(maxWeight);
            if (maxWeight < minWeight + 2) maxWeight = minWeight + 2;


            if (achievementsAll != null && achievements.isEmpty()) {
                stars = new int[(int) (last - first + 1)];
                for (int i = 0; i < stars.length; i++) stars[i] = 0;
                maxStars = 10;

                for (Achievements.Item item : achievementsAll) {
                    long date = julianDay(item.getDate());
                    if (first <= date && date <= last) {
                        if (item.getMedal() > 0 || item.getStar() > 0) {
                            achievements.add(item);
                            int i = (int) (date - first);
                            stars[i] += item.getStar();
                            if (maxStars < stars[i]) maxStars = stars[i];
                        }
                    }
                }
                if (term == Term.M3) {
                    //  週毎に再集計
                    int[] sums = new int[(stars.length + 6) / 7];
                    maxStars = 10;
                    for (int i = 0; i < sums.length; i++) {
                        sums[i] = 0;
                    }
                    for (int i = 0; i < stars.length; i++) {
                        sums[i / 7] += stars[i];
                        if (maxStars < sums[i / 7]) maxStars = sums[i / 7];
                    }
                    stars = sums;
                }

                maxStars = adjustStar(maxStars);
            }
        }

        void draw1(Canvas canvas) {
            long first = julianDay(origin);
            long last = julianDay(today);

            float dx = width / (stars.length * 4);

            for (int i = 0; i < stars.length; i++) {
                PointF p = point(first + i, first, last, stars[i], 0, maxStars);
                canvas.drawRect(p.x - dx, p.y, p.x + dx, height - BOTTOM_MARGIN, achievementPaint);
            }

            weightPaint.setStyle(Paint.Style.STROKE);
            for (int i = 1; i < weights.size(); i++) {
                Weights.Item item0 = weights.get(i - 1);
                Weights.Item item1 = weights.get(i);
                PointF p0 = point(julianDay(item0.getDate()), first, last, item0.getWeight(), minWeight, maxWeight);
                PointF p1 = point(julianDay(item1.getDate()), first, last, item1.getWeight(), minWeight, maxWeight);
                canvas.drawLine(p0.x, p0.y, p1.x, p1.y, weightPaint);
                Log.d("DietDoctor", "plot (" + p0.x + ", " + p0.y + ")-(" + p1.x + ", " + p1.y + ")");
                Log.d("DietDoctor", "plot[" + i + "] (" + p0.x + ", " + p0.y + ")-(" + p1.x + ", " + p1.y + ")");
            }

            weightPaint.setStyle(Paint.Style.FILL);
            for (Weights.Item item : weights) {
                PointF p = point(julianDay(item.getDate()), first, last, item.getWeight(), minWeight, maxWeight);
                canvas.drawCircle(p.x, p.y, DOT_RADIUS, weightPaint);
            }
        }

        void drawM3(Canvas canvas) {
            long first = julianDay(origin);
            long last = julianDay(today);

            float dx = width / (stars.length * 4);

            for (int i = 0; i < stars.length; i++) {
                PointF p = point(first + i * 7, first, last, stars[i], 0, maxStars);
                canvas.drawRect(p.x - dx, p.y, p.x + dx, height - BOTTOM_MARGIN, achievementPaint);
            }

            for (int i = 1; i < weights.size(); i++) {
                Weights.Item item0 = weights.get(i - 1);
                Weights.Item item1 = weights.get(i);
                PointF p0 = point(julianDay(item0.getDate()), first, last, item0.getWeight(), minWeight, maxWeight);
                PointF p1 = point(julianDay(item1.getDate()), first, last, item1.getWeight(), minWeight, maxWeight);
                canvas.drawLine(p0.x, p0.y, p1.x, p1.y, weightPaint);
                Log.d("DietDoctor", "plot[" + i + "] (" + p0.x + ", " + p0.y + ")-(" + p1.x + ", " + p1.y + ")");
            }
        }

        void drawAxis(Canvas canvas, PointF p, float value, Paint paint) {
            String text = String.format("%.1f", value);

            p.x -= 20.0f;
            p.y += 18.0f;

            paint.setTextSize(8.0f * density);
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(text, p.x, p.y, paint);
        }

        void drawAxis(Canvas canvas, PointF p, int value, Paint paint) {
            String text = String.format("%d", value);

            p.x += 7.0f * density;
            p.y += 6.0f * density;

            paint.setTextSize(8.0f * density);
            paint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(text, p.x, p.y, paint);
        }

//        void drawAxis(Canvas canvas, PointF p, Calendar calendar, Paint paint) {
//            int m = calendar.get(Calendar.MONTH) + 1;
//            int d = calendar.get(Calendar.DAY_OF_MONTH);
//            String text = String.format("%02d/%02d", m, d);
//
//            paint.setTextSize(8.0f * density);
//            paint.setTextAlign(Paint.Align.CENTER);
//
//            float[] cw = new float[text.length()];
//            paint.getTextWidths(text, cw);
//            float w = 0;
//            for (float w1 : cw) w += w1;
//
//            p.y += w / 2 + 3.0f * density;
//            p.x += 6.0f * density;
//
//            canvas.save();
//            canvas.rotate(-90, p.x, p.y);
//            canvas.drawText(text, p.x, p.y, paint);
//            canvas.restore();
//        }

        float dWeight(float diff) {
            if (diff <= 2.0f) {
                return 0.5f;
            } else if (diff <= 5.0f) {
                return 1.0f;
            } else if (diff <= 10.0f) {
                return 2.0f;
            } else if (diff <= 20.0f) {
                return 5.0f;
            } else {
                return 10.0f;
            }
        }

        int dStar(int diff) {
            if (diff <= 10) {
                return 2;
            } else if (diff <= 50) {
                return 10;
            } else if (diff <= 100) {
                return 20;
            } else if (diff <= 200) {
                return 50;
            } else if (diff <= 500) {
                return 100;
            } else if (diff <= 1000) {
                return 200;
            } else if (diff <= 2000) {
                return 500;
            } else {
                return 1000;
            }
        }

//        int dDay(long diff) {
//            if (diff <= 7) {
//                return 1;
//            } else {
//                return 7;
//            }
//        }

        void drawGate(Canvas canvas) {
            long first = julianDay(origin);
            long last = julianDay(today);

            //  目標体重
            {
                PointF p0 = point(first, first, last, targetWeight, minWeight, maxWeight);
                PointF p1 = point(last, first, last, targetWeight, minWeight, maxWeight);
                p0.x = LEFT_MARGIN;
                p1.x = width - RIGHT_MARGIN;
                canvas.drawLine(p0.x, p0.y, p1.x, p1.y, targetPaint);
            }

            //  枠
            axisPaint.setStyle(Paint.Style.STROKE);
            {
                axisPaint.setStrokeWidth(2.0f);
                PointF p0 = new PointF(LEFT_MARGIN, TOP_MARGIN);
                PointF p1 = new PointF(LEFT_MARGIN, height - BOTTOM_MARGIN);
                PointF p2 = new PointF(width - RIGHT_MARGIN, height - BOTTOM_MARGIN);
                PointF p3 = new PointF(width - RIGHT_MARGIN, TOP_MARGIN);
                canvas.drawLine(p0.x, p0.y, p1.x, p1.y, axisPaint);
                canvas.drawLine(p1.x, p1.y, p2.x, p2.y, axisPaint);
                canvas.drawLine(p2.x, p2.y, p3.x, p3.y, axisPaint);
                Log.d("DietDoctor", "GATE (" + LEFT_MARGIN + ", " + TOP_MARGIN + ")-(" + (width - RIGHT_MARGIN) + ", " + (height - BOTTOM_MARGIN) + ")");
            }

            axisPaint.setStyle(Paint.Style.FILL);

            //  左目盛り
            float dw = dWeight(maxWeight - minWeight);
            float w;
            for (w = minWeight; w <= maxWeight; w += dw) {
                PointF p = point(0, 0, 1, w, minWeight, maxWeight);
                p.x = LEFT_MARGIN;
                drawAxis(canvas, p, w, axisPaint);
            }

            //  右目盛り
            int dn = dStar(maxStars);
            for (int i = 0; i <= maxStars; i += dn) {
                PointF p = point(1, 0, 1, i, 0, maxStars);
                p.x = width - RIGHT_MARGIN;
                drawAxis(canvas, p, i, axisPaint);
            }
            {
                PointF p = new PointF();
                p.y = 12.0f * density;

                axisPaint.setTextSize(12.0f * density);

                p.x = 8.0f * density;
                axisPaint.setTextAlign(Paint.Align.LEFT);
                canvas.drawText(getResources().getString(R.string.weight), p.x, p.y, axisPaint);

                p.x = width - 8.0f * density;
                axisPaint.setTextAlign(Paint.Align.RIGHT);
                canvas.drawText(getResources().getString(R.string.achievement_star_caption), p.x, p.y, axisPaint);
            }

            //  下目盛り
//            int dd = dDay(last - first);
//            Calendar calendar = Calendar.getInstance();
//            for (long day = first; day <= last; day += dd) {
//                PointF p = point(day, first, last, minWeight, minWeight, maxWeight);
//                calendar.setTimeInMillis(origin.getTimeInMillis());
//                calendar.add(Calendar.DATE, (int) (day - first));
//                drawAxis(canvas, p, calendar, axisPaint);
//            }
        }

        void draw(Canvas canvas) {
            canvas.drawColor(Color.WHITE);

            if (weightsAll != null && achievementsAll != null) {
                if (stars == null) {
                    prepare();
                }
                switch (term) {
                    case W1:
                    case M1:
                        draw1(canvas);
                        break;
                    case M3:
                        drawM3(canvas);
                        break;
                }
                drawGate(canvas);
            }
        }
    }
}

