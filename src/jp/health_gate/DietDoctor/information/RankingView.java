package jp.health_gate.DietDoctor.information;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import jp.health_gate.DietDoctor.CalendarUtils;
import jp.health_gate.DietDoctor.R;
import jp.health_gate.DietDoctor.models.Achievements;
import jp.health_gate.DietDoctor.models.ActiveUser;
import jp.health_gate.DietDoctor.models.RankPoints;
import jp.health_gate.DietDoctor.models.Weights;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * ランキング（のヒストグラム）表示用のビュー
 * <p/>
 * Created by kazhida on 2013/12/16.
 */
public class RankingView extends SurfaceView implements SurfaceHolder.Callback {

    public interface Callback {
        void onPrepared();
    }

    public enum Target {
        STAR,
        RATE,
        BMI
    }

    @SuppressWarnings("unused")
    public RankingView(Context context) {
        super(context);
        init();
    }

    @SuppressWarnings("unused")
    public RankingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @SuppressWarnings("unused")
    public RankingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private Drawer drawer;
    private List<RankPoints.Item> items;

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
        drawer.setSize(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
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

    public void setTarget(Target target, Callback callback) {
        drawer.setTarget(target);
        callback.onPrepared();
    }

    public Target getTarget() {
        return drawer.target;
    }

    public void setRankPoints(RankPoints rankPoints) {
        this.items = rankPoints.getItems();
        redraw();
    }

    public int getRank() {
        return drawer.currentRank;
    }

    public int getPopulation() {
        return items.size();
    }

    public float getRate() {
        return drawer.currentRate;
    }

    public float getBMI() {
        return drawer.currentBMI;
    }

    private class Drawer {
        float width;
        float height;
        private Target target = Target.STAR;
        int currentIndex;
        int currentRank;
        int[] frequencies;
        int maxFrequency;
        int divFrequency;
        float minValue;
        float maxValue;
        Paint axisPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        float density = getResources().getDisplayMetrics().density;
        float currentRate;
        float currentBMI;

        Drawer() {
            textPaint.setColor(getResources().getColor(R.color.caption_text));
            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.setTextSize(14.0f * density);
            axisPaint.setStyle(Paint.Style.STROKE);
            axisPaint.setColor(Color.GRAY);
            axisPaint.setStrokeWidth(2.0f);
            barPaint.setStyle(Paint.Style.FILL);
        }

        void setSize(float width, float height) {
            this.width = width;
            this.height = height;
            redraw();
        }

        float calcRate() {
            Weights.Item recent0 = Weights.sharedInstance().getRecentWeight(Calendar.getInstance());
            if (recent0 == null) return 0;

            Calendar limit = CalendarUtils.dateCalendar(recent0.getDate(), -1);
            Weights.Item recent1 = Weights.sharedInstance().getRecentWeight(limit);
            if (recent1 == null) return 0;

            float dw = recent1.getWeight() - recent0.getWeight();
            float dt = recent1.getDate().getTimeInMillis() - recent0.getDate().getTimeInMillis();
            dt /= 24L * 60 * 60 * 1000;

            return dw / dt;
        }

        float calcBMI() {
            final float DEF = 35.0f;
            try {
                float h = ActiveUser.sharedInstance().getProfile().getHeight() / 100;
                Weights.Item recent0 = Weights.sharedInstance().getRecentWeight(Calendar.getInstance());
                if (recent0 != null) {
                    return recent0.getWeight() / (h * h);
                } else {
                    return DEF;
                }
            } catch (NumberFormatException e) {
                return DEF;
            }
        }

        void prepareStar() {

            Collections.sort(items, new Comparator<RankPoints.Item>() {
                @Override
                public int compare(RankPoints.Item lhs, RankPoints.Item rhs) {
                    return lhs.getStarCount() - rhs.getStarCount();
                }
            });

            int max = 0;
            currentRank = 1;
            int star = Achievements.sharedInstance().getStarCount();
            for (RankPoints.Item item : items) {
                if (max < item.getStarCount()) max = item.getStarCount();
                if (star < item.getStarCount()) currentRank++;
            }
            if (currentRank > items.size()) currentRank = items.size();

            max++;
            if (max < 100) max = 100;
            int div;
            if (max <= 100) {
                div = 10;
            } else if (max <= 200) {
                div = 20;
            } else if (max <= 500) {
                div = 50;
            } else if (max <= 1000) {
                div = 100;
            } else if (max <= 2000) {
                div = 100;
            } else if (max <= 5000) {
                div = 500;
            } else {
                div = 1000;
            }
            max /= div;
            max *= div;
            max += div;

            frequencies = new int[max / div];
            for (RankPoints.Item item : items) {
                int i = item.getStarCount() / div;
                frequencies[i]++;
            }
            currentIndex = Achievements.sharedInstance().getStarCount() / div;
            minValue = 0;
            maxValue = max;
        }

        void prepareRate() {

            Collections.sort(items, new Comparator<RankPoints.Item>() {
                @Override
                public int compare(RankPoints.Item lhs, RankPoints.Item rhs) {
                    if (lhs.getLossRate() > rhs.getLossRate()) {
                        return -1;
                    } else if (lhs.getLossRate() < rhs.getLossRate()) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });

            currentRate = -calcRate();

            minValue = Float.MIN_VALUE;
            maxValue = Float.MIN_VALUE;
            currentRank = 1;
            for (RankPoints.Item item : items) {
                if (minValue > -item.getLossRate()) minValue = -item.getLossRate();
                if (maxValue < -item.getLossRate()) maxValue = -item.getLossRate();
                if (currentRate < -item.getLossRate()) currentRank++;
            }
            if (currentRank > items.size()) currentRank = items.size();

            float div = (maxValue - minValue) / 10;
            if (div <= 0.05) {
                div = 0.05f;
            } else if (div <= 0.1) {
                div = 0.1f;
            } else if (div <= 0.2) {
                div = 0.2f;
            } else if (div <= 0.5) {
                div = 0.5f;
            } else if (div <= 1.0) {
                div = 1.0f;
            } else if (div <= 2.0) {
                div = 2.0f;
            } else {
                div = 5.0f;
            }

            minValue = (float) Math.floor(minValue / div) * div;
            maxValue = (float) Math.ceil(maxValue / div) * div;
            int n = (int) ((maxValue - minValue) / div);
            frequencies = new int[n];

            for (RankPoints.Item item : items) {
                for (int i = 0; i < n; i++) {
                    if (-item.getLossRate() < div * (i + 1) + minValue) {
                        frequencies[i]++;
                        break;
                    } else if (i == n - 1) {
                        frequencies[i]++;
                    }
                }
            }

            for (int i = 0; i < n; i++) {
                if (currentRate < div * (i + 1) + minValue) {
                    currentIndex = i;
                    break;
                } else if (i == n - 1) {
                    currentIndex = i;
                }
            }
        }

        void prepareBMI() {
            final float IDEAL_BMI = 22.0f;

            Collections.sort(items, new Comparator<RankPoints.Item>() {
                @Override
                public int compare(RankPoints.Item lhs, RankPoints.Item rhs) {
                    float l_diff = Math.abs(lhs.getLossRate() - IDEAL_BMI);
                    float r_diff = Math.abs(rhs.getLossRate() - IDEAL_BMI);
                    float diff = l_diff - r_diff;
                    if (diff < 0) {
                        return -1;
                    } else if (diff > 0) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });

            currentBMI = calcBMI();
            float diffBMI = Math.abs(currentBMI - IDEAL_BMI);
            currentRank = 1;
            minValue = 0;
            maxValue = 0;
            for (RankPoints.Item item : items) {
                float diff = Math.abs(item.getBMI() - IDEAL_BMI);
                if (maxValue < diff) maxValue = diff;
                if (diffBMI > diff) currentRank++;
            }
            if (currentRank > items.size()) currentRank = items.size();

            float div = maxValue / 10;
            if (div <= 0.1) {
                div = 0.1f;
            } else if (div <= 0.2) {
                div = 0.2f;
            } else if (div <= 0.5) {
                div = 0.5f;
            } else if (div <= 1.0) {
                div = 1.0f;
            } else if (div <= 2.0) {
                div = 2.0f;
            } else {
                div = 5.0f;
            }

            maxValue = (float) Math.floor(maxValue / div) * div;
            int n = (int) (maxValue / div);
            frequencies = new int[n];

            for (RankPoints.Item item : items) {
                for (int i = 0; i < n; i++) {
                    float diff = Math.abs(item.getBMI() - IDEAL_BMI);
                    if (diff > div * (n - i)) {
                        frequencies[i]++;
                        break;
                    } else if (i == n - 1) {
                        frequencies[i]++;
                    }
                }
            }

            for (int i = 0; i < n; i++) {
                if (diffBMI > div * (n - i)) {
                    currentIndex = i;
                    break;
                } else if (i == n - 1) {
                    currentIndex = i;
                }
            }
        }

        void prepare() {
            switch (target) {
                case STAR:
                    prepareStar();
                    break;
                case RATE:
                    prepareRate();
                    break;
                case BMI:
                    prepareBMI();
                    break;
            }
            maxFrequency = 0;
            for (int f : frequencies) {
                if (f > maxFrequency) maxFrequency = f;
            }
            divFrequency = maxFrequency / 4;
            if (divFrequency <= 1) {
                divFrequency = 1;
            } else if (divFrequency <= 5) {
                divFrequency = 5;
            } else if (divFrequency <= 25) {
                divFrequency = 25;
            } else if (divFrequency < 50) {
                divFrequency = 50;
            } else if (divFrequency < 125) {
                divFrequency = 125;
            } else if (divFrequency < 250) {
                divFrequency = 250;
            } else if (divFrequency < 500) {
                divFrequency = 500;
            } else if (divFrequency < 1250) {
                divFrequency = 1250;
            } else if (divFrequency < 2500) {
                divFrequency = 2500;
            } else if (divFrequency < 5000) {
                divFrequency = 5000;
            } else if (divFrequency < 12500) {
                divFrequency = 12500;
            } else if (divFrequency < 25000) {
                divFrequency = 25000;
            } else if (divFrequency < 50000) {
                divFrequency = 50000;
            } else if (divFrequency < 125000) {
                divFrequency = 125000;
            } else if (divFrequency < 250000) {
                divFrequency = 250000;
            } else {
                //  100万人を超えてたらすごいよね
                divFrequency += 250000 - 1;
                divFrequency /= 250000;
                divFrequency *= 250000;
            }
            maxFrequency = divFrequency * 4;
        }

        void setTarget(Target target) {
            this.target = target;
            prepare();
            redraw();
        }

        static final float TOP_MARGIN = 40.0f;
        static final float BOTTOM_MARGIN = 20.0f;
        static final float LEFT_MARGIN = 160.0f;
        static final float RIGHT_MARGIN = 160.0f;

        PointF point(long date, long first, long last, float value, float min, float max) {
            float w = width - (LEFT_MARGIN + RIGHT_MARGIN);
            float h = height - (TOP_MARGIN + BOTTOM_MARGIN);
            float x = w / (last - first + 1) * (date - first) + LEFT_MARGIN;
            float y = (height - BOTTOM_MARGIN) - (h / (max - min) * (value - min));
            x += w / (last - first + 1) / 2;
            return new PointF(x, y);
        }

        void drawHistogram(Canvas canvas) {
            float dw = width / frequencies.length;

            for (int i = 0; i < frequencies.length; i++) {
                PointF p = point(i, 0, frequencies.length, frequencies[i], 0, maxFrequency);
                float x0 = p.x + dw / 4;
                float x1 = x0 + dw / 2;
                if (i == currentIndex) {
                    barPaint.setColor(getResources().getColor(R.color.bg_pressed));
                } else {
                    barPaint.setColor(getResources().getColor(R.color.chart_fill));
                }
                canvas.drawRect(x0, p.y, x1, height - BOTTOM_MARGIN, barPaint);
            }
        }

        void drawGate(Canvas canvas) {
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
                canvas.drawLine(p3.x, p3.y, p0.x, p0.y, axisPaint);
            }

            axisPaint.setStyle(Paint.Style.FILL);

            //  右目盛り
            for (int i = 0; i <= maxFrequency; i += divFrequency) {
                PointF p = point(1, 0, 1, i, 0, maxFrequency);
                p.x = width - RIGHT_MARGIN;
                String text = String.format("%d", i * divFrequency);
                p.x += 7.0f * density;
                p.y += 6.0f * density;
                canvas.drawText(text, p.x, p.y, textPaint);
            }
        }

        void draw(Canvas canvas) {
            canvas.drawColor(Color.WHITE);

            if (items != null) {
                if (frequencies == null) {
                    prepare();
                }
                drawHistogram(canvas);
                drawGate(canvas);
            }
        }
    }
}
