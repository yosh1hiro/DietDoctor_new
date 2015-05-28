package jp.health_gate.DietDoctor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

/**
 * 文字サイズを自動で小さくするTextView
 * <p/>
 * Created by kazhida on 2014/01/16.
 */
public class AutoSizeTextView extends TextView {

    private float originalTextSize;
    private Rect workRect = new Rect();
    private float density = getResources().getDisplayMetrics().density;

    @SuppressWarnings("unused")
    public AutoSizeTextView(Context context) {
        super(context);
        originalTextSize = getTextSize();
    }

    @SuppressWarnings("unused")
    public AutoSizeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        originalTextSize = getTextSize();
    }

    @SuppressWarnings("unused")
    public AutoSizeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        originalTextSize = getTextSize();
    }

    @Override
    public void setTextSize(float size) {
        super.setTextSize(size);
        originalTextSize = getTextSize();
    }

    @Override
    public void setTextSize(int unit, float size) {
        super.setTextSize(unit, size);
        originalTextSize = getTextSize();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        TextPaint paint = getPaint();

        float textSize = originalTextSize;
        String text = getText().toString();
        float paddingLeft = getPaddingLeft();
        float paddingRight = getPaddingRight();
        float width;

        do {
            super.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            paint.getTextBounds(text, 0, text.length(), workRect);
            width = getWidth();
            width -= paddingLeft + paddingRight + 2.0f;   //ちょっとだけ狭くする
            textSize -= density;
        } while (workRect.right - workRect.left > width && textSize > 4.0f);

        super.onDraw(canvas);
    }
}
