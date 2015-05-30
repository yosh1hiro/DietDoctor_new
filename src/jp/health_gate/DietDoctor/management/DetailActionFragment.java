package jp.health_gate.DietDoctor.management;

import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.haarman.supertooltips.ToolTipRelativeLayout;
import jp.health_gate.DietDoctor.CustomFragment;
import jp.health_gate.DietDoctor.Scenario;
import jp.health_gate.DietDoctor.models.DietActions;
import jp.health_gate.DietDoctor.R;

/**
 * 減量行動の詳細表示用フラグメント
 * <p/>
 * Created by kazhida on 2013/10/09.
 */
class DetailActionFragment extends CustomFragment {

    private static final String INDEX = "INDEX";
    private static final String ID = "ID";
    private DietActions.ChallengeItem action;
    private int goalIndex;
    private Scenario scenario;

    static DetailActionFragment newInstance(int index, int id) {
        DetailActionFragment fragment = new DetailActionFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(INDEX, index);
        bundle.putInt(ID, id);

        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        goalIndex = getArguments().getInt(INDEX);
        action = DietActions.sharedInstance().findAction(getArguments().getInt(ID));
        return inflater.inflate(R.layout.detail_action_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitleText(action.getCaption());

        setText(R.id.action_title, action.getCaption());
        setIcon(R.id.action_icon, action.getIcon());
        setText(R.id.chart_value_0_text, "" + action.getChartValue(0));
        setText(R.id.chart_value_1_text, "" + action.getChartValue(1));
        setText(R.id.chart_value_2_text, "" + action.getChartValue(2));

        setText(R.id.action_description, "向いている方\n" + action.getTargetUser() + "\n\n" + action.getDescription());

        getView().findViewById(R.id.select_action_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceFragment(SelectLevelFragment.newInstance(goalIndex, action.getGroupId()));
            }
        });

        ImageView imageView = (ImageView) getView().findViewById(R.id.chart_image);
        imageView.setImageDrawable(radarChart());

        ToolTipRelativeLayout layout = (ToolTipRelativeLayout) getActivity().findViewById(R.id.tooltip_layout);
        int color = getResources().getColor(R.color.bg_tutorial);
        scenario = new Scenario(layout, color);
        scenario.setParentView(getView());
        scenario.addStep(1000, R.string.tutorial_decide_action, R.id.select_action_button);
        scenario.step(0);
    }

    @Override
    public void onPause() {
        if (scenario != null) scenario.end();
        super.onPause();
    }

    private Drawable radarChart() {
        float density = getResources().getDisplayMetrics().density;
        int h = (int) (120 * density);
        int w = (int) (150 * density);
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        ChartDrawer drawer = new ChartDrawer(w, h);
        drawer.draw(new Canvas(bitmap));
        return new BitmapDrawable(getResources(), bitmap);
    }

    private class ChartDrawer {
        private PointF center;
        private float radius;
        private Paint paint;

        ChartDrawer(float w, float h) {
            center = new PointF(w / 2, 2 * h / 3);
            radius = 2 * h / 3;
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStrokeWidth(1.0f);
        }

        void drawTriangle(Canvas canvas, float r) {
            float w = (float) (r * Math.sqrt(3.0) / 2);
            canvas.drawLine(center.x, center.y - r, center.x + w, center.y + r / 2, paint);
            canvas.drawLine(center.x + w, center.y + r / 2, center.x - w, center.y + r / 2, paint);
            canvas.drawLine(center.x - w, center.y + r / 2, center.x, center.y - r, paint);
        }

        void drawTriangle(Canvas canvas, int v0, int v1, int v2) {
            float w = (float) (radius * Math.sqrt(3.0) / 2);
            float h = radius / 2;
            Path path = new Path();
            path.moveTo(center.x, center.y - radius * v0 / 5);
            path.lineTo(center.x - w * v1 / 5, center.y + h * v1 / 5);
            path.lineTo(center.x + w * v2 / 5, center.y + h * v2 / 5);
            path.close();
            canvas.drawPath(path, paint);
        }

        void draw(Canvas canvas) {
            paint.setColor(Color.LTGRAY);
            paint.setStyle(Paint.Style.STROKE);
            for (int i = 0; i <= 5; i++) {
                drawTriangle(canvas, radius * i / 5);
            }
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(getResources().getColor(R.color.chart_fill));
            drawTriangle(canvas, action.getChartValue(0), action.getChartValue(1), action.getChartValue(2));
        }
    }
}
