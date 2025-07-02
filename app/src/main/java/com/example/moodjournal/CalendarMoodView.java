package com.example.moodjournal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.*;

public class CalendarMoodView extends View {
    private static final int MONTHS_TO_SHOW = 3;
    private static final int DAYS_IN_WEEK = 7;
    private static final String[] MONTH_NAMES = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
    };
    private static final String[] DAY_NAMES = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    private Paint textPaint, circlePaint, backgroundPaint, headerPaint;
    private Calendar currentCalendar;
    private Map<String, List<MoodEntry>> moodData;
    private GestureDetector gestureDetector;
    private OnDateClickListener dateClickListener;
    private OnMonthChangeListener monthChangeListener;

    private float cellWidth, cellHeight;
    private int currentMonthOffset = 0;
    private List<DateCell> dateCells; // For click detection

    public interface OnDateClickListener {
        void onDateClick(Calendar date, List<MoodEntry> moods);
    }

    public interface OnMonthChangeListener {
        void onMonthChange(Calendar month);
    }

    private static class DateCell {
        RectF bounds;
        Calendar date;
        List<MoodEntry> moods;

        DateCell(RectF bounds, Calendar date, List<MoodEntry> moods) {
            this.bounds = new RectF(bounds);
            this.date = (Calendar) date.clone();
            this.moods = moods;
        }
    }

    public CalendarMoodView(Context context) {
        super(context);
        init();
    }

    public CalendarMoodView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        currentCalendar = Calendar.getInstance();
        moodData = new HashMap<>();
        dateCells = new ArrayList<>();

        // Initialize paints
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.parseColor("#333333"));

        headerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerPaint.setTextAlign(Paint.Align.CENTER);
        headerPaint.setColor(Color.parseColor("#333333"));
        headerPaint.setFakeBoldText(true);

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(8f);

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(Color.WHITE);

        // Setup gesture detector
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(velocityY) && Math.abs(diffX) > 100) {
                    if (diffX > 0) {
                        navigateMonth(-1); // Swipe right - previous month
                    } else {
                        navigateMonth(1);  // Swipe left - next month
                    }
                    return true;
                }
                return false;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                handleDateClick(e.getX(), e.getY());
                return true;
            }
        });
    }

    public void setMoodData(Map<String, List<MoodEntry>> data) {
        this.moodData = data;
        invalidate();
    }

    public void setOnDateClickListener(OnDateClickListener listener) {
        this.dateClickListener = listener;
    }

    public void setOnMonthChangeListener(OnMonthChangeListener listener) {
        this.monthChangeListener = listener;
    }

    private void navigateMonth(int direction) {
        currentMonthOffset += direction;
        if (monthChangeListener != null) {
            Calendar month = Calendar.getInstance();
            month.add(Calendar.MONTH, currentMonthOffset);
            monthChangeListener.onMonthChange(month);
        }
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateDimensions();
    }

    private void calculateDimensions() {
        int width = getWidth() - getPaddingLeft() - getPaddingRight();
        int height = getHeight() - getPaddingTop() - getPaddingBottom();

        cellWidth = width / (float) DAYS_IN_WEEK;
        cellHeight = height / (float) (MONTHS_TO_SHOW * 8 + 2); // 3 months * 8 rows + spacing

        textPaint.setTextSize(cellHeight * 0.25f);
        headerPaint.setTextSize(cellHeight * 0.35f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (cellWidth == 0 || cellHeight == 0) {
            calculateDimensions();
        }

        dateCells.clear();
        float startY = getPaddingTop();

        // Draw 3 months
        for (int monthIndex = -1; monthIndex <= 1; monthIndex++) {
            Calendar monthCalendar = Calendar.getInstance();
            monthCalendar.add(Calendar.MONTH, currentMonthOffset + monthIndex);

            startY = drawMonth(canvas, monthCalendar, startY);
            startY += cellHeight * 0.5f; // Add spacing between months
        }
    }

    private float drawMonth(Canvas canvas, Calendar monthCalendar, float startY) {
        float currentY = startY;

        // Draw month header
        String monthYear = MONTH_NAMES[monthCalendar.get(Calendar.MONTH)] + " " +
                monthCalendar.get(Calendar.YEAR);

        canvas.drawText(monthYear, getWidth() / 2f, currentY + cellHeight * 0.4f, headerPaint);
        currentY += cellHeight * 0.6f;

        // Draw day headers
        for (int i = 0; i < DAYS_IN_WEEK; i++) {
            float x = getPaddingLeft() + (i + 0.5f) * cellWidth;
            canvas.drawText(DAY_NAMES[i], x, currentY + cellHeight * 0.3f, textPaint);
        }
        currentY += cellHeight * 0.5f;

        // Calculate first day of month and number of days
        Calendar firstDay = (Calendar) monthCalendar.clone();
        firstDay.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = firstDay.get(Calendar.DAY_OF_WEEK) - 1; // 0-based
        int daysInMonth = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Draw calendar days
        int dayCounter = 1;
        for (int week = 0; week < 6 && dayCounter <= daysInMonth; week++) {
            for (int day = 0; day < DAYS_IN_WEEK && dayCounter <= daysInMonth; day++) {
                if (week == 0 && day < firstDayOfWeek) {
                    continue; // Skip days before month starts
                }

                float x = getPaddingLeft() + (day + 0.5f) * cellWidth;
                float y = currentY + cellHeight * 0.4f;

                // Create date for this cell
                Calendar cellDate = (Calendar) monthCalendar.clone();
                cellDate.set(Calendar.DAY_OF_MONTH, dayCounter);

                // Get mood data for this date
                String dateKey = getDateKey(cellDate);
                List<MoodEntry> dayMoods = moodData.get(dateKey);

                // Create clickable area
                RectF cellBounds = new RectF(
                        getPaddingLeft() + day * cellWidth,
                        currentY,
                        getPaddingLeft() + (day + 1) * cellWidth,
                        currentY + cellHeight
                );
                dateCells.add(new DateCell(cellBounds, cellDate, dayMoods));

                // Draw mood indicator circle if there are moods
                if (dayMoods != null && !dayMoods.isEmpty()) {
                    MoodEntry dominantMood = getDominantMood(dayMoods);
                    circlePaint.setColor(getMoodColor(dominantMood.getMood()));

                    float radius = Math.min(cellWidth, cellHeight) * 0.25f;
                    canvas.drawCircle(x, y, radius, circlePaint);
                }

                // Draw day number
                canvas.drawText(String.valueOf(dayCounter), x, y + textPaint.getTextSize() * 0.3f, textPaint);

                dayCounter++;
            }
            currentY += cellHeight;
        }

        return currentY;
    }

    private MoodEntry getDominantMood(List<MoodEntry> moods) {
        // Count mood types and return the most frequent one
        Map<String, Integer> moodCounts = new HashMap<>();
        for (MoodEntry mood : moods) {
            moodCounts.put(mood.getMood(), moodCounts.getOrDefault(mood.getMood(), 0) + 1);
        }

        String dominantType = null;
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : moodCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                dominantType = entry.getKey();
            }
        }

        // Return first mood of dominant type
        for (MoodEntry mood : moods) {
            if (mood.getMood().equals(dominantType)) {
                return mood;
            }
        }

        return moods.get(0); // Fallback
    }

    private int getMoodColor(String mood) {
        // Use the same colors as your MoodAdapter
        switch (mood.toLowerCase()) {
            case "happy": return Color.parseColor("#FFD700");      // Gold
            case "sad": return Color.parseColor("#4169E1");        // Royal Blue
            case "angry": return Color.parseColor("#DC143C");      // Crimson
            case "anxious": return Color.parseColor("#9370DB");    // Medium Purple
            case "tired": return Color.parseColor("#696969");      // Dim Gray
            case "calm": return Color.parseColor("#32CD32");       // Lime Green
            case "stressed": return Color.parseColor("#FF8C00");   // Dark Orange
            case "excited": return Color.parseColor("#FF1493");    // Deep Pink
            case "lonely": return Color.parseColor("#6495ED");     // Cornflower Blue
            case "neutral": return Color.parseColor("#A9A9A9");    // Dark Gray
            default: return Color.parseColor("#808080");           // Gray default
        }
    }

    private String getDateKey(Calendar date) {
        return String.format(Locale.getDefault(), "%04d-%02d-%02d",
                date.get(Calendar.YEAR),
                date.get(Calendar.MONTH) + 1,
                date.get(Calendar.DAY_OF_MONTH));
    }

    private void handleDateClick(float x, float y) {
        for (DateCell cell : dateCells) {
            if (cell.bounds.contains(x, y)) {
                if (dateClickListener != null) {
                    dateClickListener.onDateClick(cell.date, cell.moods);
                }
                break;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }
}
