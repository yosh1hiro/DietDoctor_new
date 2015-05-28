package jp.health_gate.DietDoctor;

import android.os.Handler;
import android.view.View;
import com.haarman.supertooltips.ToolTip;
import com.haarman.supertooltips.ToolTipRelativeLayout;
import com.haarman.supertooltips.ToolTipView;

import java.util.ArrayList;
import java.util.List;

/**
 * チュートリアルのシナリオ
 * Created by kazhida on 2014/01/31.
 */
public class Scenario {

    private Handler handler = new Handler();
    private ToolTipRelativeLayout layout;
    private int color;
    private final List<Step> steps = new ArrayList<Step>();
    private int counter = 0;
    private View parent;

    private class Step {
        long delay;
        Runnable step;
        ToolTipView view = null;

        void execute() {
//            handler.postDelayed(step, delay);
//            handler.post(step);
        }
    }

    public Scenario(ToolTipRelativeLayout layout, int color) {
        this.layout = layout;
        this.color = color;
    }

    public void setParentView(View parent) {
        this.parent = parent;
    }

    public void addStep(long delay, final int msgId, final int id) {
        final Step step = new Step();
        steps.add(step);
        final int next = steps.size();

        step.delay = delay;
        step.step = new Runnable() {
            @Override
            public void run() {
                if (parent != null) {
                    View target = parent.findViewById(id);
                    if (target != null && target.getVisibility() == View.VISIBLE) {
                        step.view = layout.showToolTipForView(toolTip(msgId), target);
                        step.view.setOnToolTipViewClickedListener(new ToolTipView.OnToolTipViewClickedListener() {
                            @Override
                            public void onToolTipViewClicked(ToolTipView toolTipView) {
                                Scenario.this.step(next);
                            }
                        });
                    }
                }
            }
        };
    }

    private ToolTip toolTip(int msgId) {
        return new ToolTip().withText(msgId).withColor(color).withAnimationType(ToolTip.ANIMATIONTYPE_FROMMASTERVIEW);
    }

    private void removeAll() {
        synchronized (steps) {
            for (Step step : steps) {
                if (step.view != null) {
                    step.view.setOnToolTipViewClickedListener(null);
                    step.view.remove();
                    step.view = null;
                }
            }
        }
    }

    public void step(int index) {
        removeAll();
        synchronized (steps) {
            if (counter <= index && index < steps.size()) {
                steps.get(index).execute();
                counter = index + 1;
            }
        }
    }

    public void end() {
        synchronized (steps) {
            parent = null;
            counter = steps.size();
            for (Step step : steps) {
                step.step = null;
            }
        }
        removeAll();
    }

    @Override
    public void finalize() throws Throwable {
        end();
        super.finalize();
    }
}
