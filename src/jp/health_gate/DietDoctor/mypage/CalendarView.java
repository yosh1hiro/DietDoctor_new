package jp.health_gate.DietDoctor.mypage;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import java.util.Calendar;
import java.util.List;

import jp.health_gate.DietDoctor.CalendarUtils;
import jp.health_gate.DietDoctor.R;
import jp.health_gate.DietDoctor.models.Achievements;
import jp.health_gate.DietDoctor.models.Weights;

/**
 * Created by yoshihiro on 2014/10/27.
 */
public class CalendarView extends SurfaceView implements SurfaceHolder.Callback {

    public interface Callback {
        void onPrevMonth(Calendar month);

        void onNextMonth(Calendar month);

        void onPickDate(Calendar date);
    }

    @SuppressWarnings("unused")
    public CalendarView(Context context) {
        super(context);
        init();
    }

    @SuppressWarnings("unused")
    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @SuppressWarnings("unused")
    public CalendarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downState = drawer.position(event.getX(), event.getY());
                redraw();
                return true;
            case MotionEvent.ACTION_UP:
                if (downState != IGNORE && callback != null) {
                    if (downState == drawer.position(event.getX(), event.getY())) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(drawer.year, drawer.month, 1);
                        switch (downState) {
                            case PREV_BUTTON:
                                callback.onPrevMonth(calendar);
                                drawer.setMonth(calendar);
                                break;
                            case NEXT_BUTTON:
                                callback.onNextMonth(calendar);
                                drawer.setMonth(calendar);
                                break;
                            default:
                                drawer.setPositionDate(calendar, downState);
                                callback.onPickDate(calendar);
                                redraw();
                                break;
                        }
                    }
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    private Drawer drawer;
    private List<Weights.Item> weights;
    private List<Achievements.Item> achievements;
    private int downState;
    private static final int PREV_BUTTON = 1;
    private static final int NEXT_BUTTON = 2;
    private static final int IGNORE = -1;
    private Callback callback;

    private void init() {
        getHolder().addCallback(this);
        setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        setFocusable(true);
        drawer = new Drawer();
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setSource(Weights weights, Achievements achievements) {
        this.weights = weights.load();
        this.achievements = achievements.load();
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

    public int getYear() {
        return drawer.year;
    }

    public int getMonth() {
        return drawer.month;
    }

    public void setMonth(Calendar month) {
        drawer.setMonth(month);
    }

    private class Drawer {
        float width;
        float height;
        float rowHeight;
        float colWidth;
        Calendar origin = Calendar.getInstance();
        Calendar work = Calendar.getInstance();
        int year;
        int month;
        long firstDay;
        Paint mainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint grayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint gatePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint pickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        float density = getResources().getDisplayMetrics().densityDpi / 160.0f;

        final float TEXT_OFFSET = 4.0f * density;

        Drawer() {
            final float FONT_SIZE = 18.0f * density;

            setMonth(Calendar.getInstance());

            mainPaint.setColor(getResources().getColor(R.color.caption_text));
            mainPaint.setTextSize(FONT_SIZE);
            mainPaint.setStyle(Paint.Style.FILL);
            mainPaint.setTextAlign(Paint.Align.CENTER);

            arrowPaint.setColor(getResources().getColor(R.color.bg_caption));
            arrowPaint.setStyle(Paint.Style.FILL);

            grayPaint.setColor(Color.LTGRAY);
            grayPaint.setTextSize(FONT_SIZE);
            grayPaint.setStyle(Paint.Style.FILL);
            grayPaint.setTextAlign(Paint.Align.CENTER);

            gatePaint.setColor(Color.GRAY);
            gatePaint.setStyle(Paint.Style.STROKE);
            gatePaint.setStrokeWidth(3.0f);

            pickPaint.setColor(getResources().getColor(R.color.bg_picked));
            pickPaint.setStyle(Paint.Style.FILL);
        }

        void setSize(int width, int height) {
            this.width = width;
            this.height = height;
            this.colWidth = width / 7;
            this.rowHeight = height / 7;    //タイトル1行と6週分
            redraw();
        }

        void setMonth(Calendar month) {
            origin.setTimeInMillis(month.getTimeInMillis());
            origin.set(Calendar.DAY_OF_MONTH, 1);
            firstDay = julianDay(month);
            int offset = (int) ((firstDay + 1) % 7);    //  1日の曜日(SUNDAY=0始まり)
            origin.add(Calendar.DATE, -offset);         //  カレンダー表示の最初の日
            this.year = month.get(Calendar.YEAR);       //  カレンダーの年
            this.month = month.get(Calendar.MONTH);     //  カレンダーの月
            redraw();
        }

        void drawTitle(Canvas canvas) {
            float x0 = 0;
            float x1 = rowHeight;
            float x2 = width - rowHeight;
//            float x3 = width;
            float y0 = 0;
            float y1 = rowHeight;
//            float dx = rowHeight * 3 / 4;
//            float dy = rowHeight / 6;

            final float offset = -mainPaint.getFontMetrics().ascent / 2;

            String[] months = getResources().getStringArray(R.array.month_list);
            String title = months[month] + " " + year;

//            Path arrowL = new Path();
//            arrowL.moveTo(x0, (y0 + y1) / 2);
//            arrowL.lineTo(x0 + dx, y0 + dy);
//            arrowL.lineTo(x0 + dx, y1 - dy);
//            arrowL.close();
//            canvas.drawPath(arrowL, arrowPaint);
//
//            Path arrowR = new Path();
//            arrowR.moveTo(x3, (y0 + y1) / 2);
//            arrowR.lineTo(x3 - dx, y0 + dy);
//            arrowR.lineTo(x3 - dx, y1 - dy);
//            arrowR.close();
//            canvas.drawPath(arrowR, arrowPaint);

            canvas.drawRect(x0, y0, x0 + colWidth * 7, y0 + rowHeight, gatePaint);

            canvas.drawText(title, (x1 + x2) / 2, (y0 + y1) / 2 + offset, mainPaint);
        }

        void drawDate(Canvas canvas) {
            work.setTimeInMillis(origin.getTimeInMillis());

            for (int r = 1; r < 7; r++) {
                for (int c = 0; c < 7; c++) {
                    float x0 = colWidth * c;
                    float y0 = rowHeight * r;
                    int m = work.get(Calendar.MONTH);
                    int d = work.get(Calendar.DAY_OF_MONTH);

                    if (r * 7 + c == downState || (downState == 0 && CalendarUtils.isToday(work))) {
                        canvas.drawRect(x0, y0, x0 + colWidth, y0 + rowHeight, pickPaint);
                    }
                    canvas.drawRect(x0, y0, x0 + colWidth, y0 + rowHeight, gatePaint);

                    if (m != month) {
                        canvas.drawText("" + d, x0 + colWidth / 2, y0 + rowHeight / 2 + TEXT_OFFSET, grayPaint);
                    } else if (CalendarUtils.isToday(work)) {
                        mainPaint.setColor(getResources().getColor(R.color.today_caption));
                        canvas.drawText("" + d, x0 + colWidth / 2, y0 + rowHeight / 2 + TEXT_OFFSET, mainPaint);
                    } else if (work.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                        mainPaint.setColor(getResources().getColor(R.color.sunday_caption));
                        canvas.drawText("" + d, x0 + colWidth / 2, y0 + rowHeight / 2 + TEXT_OFFSET, mainPaint);
                    } else if (work.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
                        mainPaint.setColor(getResources().getColor(R.color.saturday_caption));
                        canvas.drawText("" + d, x0 + colWidth / 2, y0 + rowHeight / 2 + TEXT_OFFSET, mainPaint);
                    } else {
                        canvas.drawText("" + d, x0 + colWidth / 2, y0 + rowHeight / 2 + TEXT_OFFSET, mainPaint);
                    }
                    mainPaint.setColor(getResources().getColor(R.color.caption_text));

                    work.add(Calendar.DATE, 1);
                }
            }
        }

        int position(float x, float y) {
            int ix = (int) Math.floor(x / colWidth);
            int iy = (int) Math.floor(y / rowHeight);
            if (iy > 0) {
                return iy * 7 + ix;
            } else if (ix == 0) {
                return PREV_BUTTON;
            } else if (ix == 6) {
                return NEXT_BUTTON;
            } else {
                return IGNORE;
            }
        }

        void drawWeightDots(Canvas canvas) {
            long first = julianDay(origin);
            final float r = 6.0f;

            mainPaint.setColor(getResources().getColor(R.color.weight_mark));
            for (Weights.Item item : weights) {
                long date = julianDay(item.getDate());
                if (first <= date && date < first + 6 * 7) {
                    int offset = (int) (date - first);
                    float x = colWidth * (offset % 7);
                    float y = rowHeight * (offset / 7 + 1);
                    x += colWidth * 2 / 6;
                    y += rowHeight * 4 / 5;

                    canvas.drawRect(x - r, y - r, x + r, y + r, mainPaint);
                }
            }
            mainPaint.setColor(getResources().getColor(R.color.caption_text));
        }

        void drawAchievementDots(Canvas canvas) {
            long first = julianDay(origin);
            final float r = 8.0f;

            mainPaint.setColor(getResources().getColor(R.color.star_mark));
            for (Achievements.Item item : achievements) {
                long date = julianDay(item.getDate());
                if (first <= date && date < first + 6 * 7) {
                    int offset = (int) (date - first);
                    float x = colWidth * (offset % 7);
                    float y = rowHeight * (offset / 7 + 1);
                    x += colWidth * 3 / 6;
                    y += rowHeight * 4 / 5;

                    canvas.drawCircle(x, y, r, mainPaint);
                }
            }
            mainPaint.setColor(getResources().getColor(R.color.caption_text));
        }

        void drawMemoDots(Canvas canvas) {
            long first = julianDay(origin);
            final float r = 6.0f;

            mainPaint.setColor(getResources().getColor(R.color.memo_mark));
            for (Weights.Item item : weights) {
                long date = julianDay(item.getDate());
                if (first <= date && date < first + 6 * 7 && item.hasMemo()) {
                    int offset = (int) (date - first);
                    float x = colWidth * (offset % 7);
                    float y = rowHeight * (offset / 7 + 1);
                    x += colWidth * 4 / 6;
                    y += rowHeight * 4 / 5;

                    canvas.drawRect(x - r, y - r, x + r, y + r, mainPaint);
                }
            }
            mainPaint.setColor(getResources().getColor(R.color.caption_text));
        }

        void setPositionDate(Calendar calendar, int position) {
            calendar.setTimeInMillis(origin.getTimeInMillis());
            calendar.add(Calendar.DATE, position - 7);
        }

        void draw(Canvas canvas) {
            canvas.drawColor(Color.WHITE);
            drawTitle(canvas);
            drawDate(canvas);
            drawWeightDots(canvas);
            drawAchievementDots(canvas);
            drawMemoDots(canvas);
        }
    }
}