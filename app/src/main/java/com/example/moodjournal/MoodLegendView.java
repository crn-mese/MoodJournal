package com.example.moodjournal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class MoodLegendView extends View {
    private static final String[] MOOD_TYPES = {"Happy", "Sad", "Angry", "Anxious", "Calm"};
    private static final String[] MOOD_COLORS = {"#FFD700", "#4169E1", "#DC143C", "#9370DB", "#32CD32"};

    private Paint circlePaint, textPaint;

    public MoodLegendView(Context context) {
        super(context);
        init();
    }

    public MoodLegendView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(6f);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(32f);
        textPaint.setColor(Color.parseColor("#333333"));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float itemWidth = getWidth() / (float) MOOD_TYPES.length;
        float centerY = getHeight() / 2f;
        float circleRadius = 15f;

        for (int i = 0; i < MOOD_TYPES.length; i++) {
            float centerX = (i + 0.5f) * itemWidth;

            // Draw colored circle
            circlePaint.setColor(Color.parseColor(MOOD_COLORS[i]));
            canvas.drawCircle(centerX, centerY - 20f, circleRadius, circlePaint);

            // Draw mood label
            canvas.drawText(MOOD_TYPES[i], centerX, centerY + 15f, textPaint);
        }
    }
}
