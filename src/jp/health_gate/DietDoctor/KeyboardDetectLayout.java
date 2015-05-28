package jp.health_gate.DietDoctor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * ソフトキーボードの出現を検知するFrameLayout
 * <p/>
 * Created by kazhida on 2013/06/20.
 */
public class KeyboardDetectLayout extends RelativeLayout {

    public interface OnKeyboardDetectListener {
        void onKeyboardShown();

        void onKeyboardHidden();
    }

    private static final int threshold = 180; //dp単位：ソフトキーボードはこれより大きいはず
    private OnKeyboardDetectListener listener;
    private Handler handler = new Handler();

    @SuppressWarnings("unused")
    public KeyboardDetectLayout(Context context) {
        super(context);
    }

    @SuppressWarnings("unused")
    public KeyboardDetectLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressWarnings("unused")
    public KeyboardDetectLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnKeyboardDetectListener(OnKeyboardDetectListener keyboardListener) {
        this.listener = keyboardListener;
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (listener != null) {
            int proposedHeight = MeasureSpec.getSize(heightMeasureSpec);
            int actualHeight = getHeight();
            final int difference = actualHeight - proposedHeight;

            float limit = threshold * getContext().getResources().getDisplayMetrics().density;

            if (Math.abs(difference) > limit) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (difference > 0) {
                            listener.onKeyboardShown();
                        } else {
                            listener.onKeyboardHidden();
                        }
                    }
                });
            }
        }
    }
}
