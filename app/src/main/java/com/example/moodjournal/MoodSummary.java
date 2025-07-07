package com.example.moodjournal;

import android.widget.TextView;
import java.util.Map;
import java.util.HashMap;
import java.util.Calendar;
import java.util.List;

public class MoodSummary {
    private List<MoodEntry> moodEntries;
    private TextView weeklySummaryTextView;

    public MoodSummary(List<MoodEntry> moodEntries, TextView weeklySummaryTextView) {
        this.moodEntries = moodEntries;
        this.weeklySummaryTextView = weeklySummaryTextView;
    }

    private Map<String, Integer> getWeeklyMoodCounts() {
        Map<String, Integer> moodCounts = new HashMap<>();
        Calendar oneWeekAgo = Calendar.getInstance();
        oneWeekAgo.add(Calendar.DAY_OF_YEAR, -6);

        for (MoodEntry entry : moodEntries) {
            if (entry.getTimestamp() >= oneWeekAgo.getTimeInMillis()) {
                String mood = entry.getMood();
                moodCounts.put(mood, moodCounts.getOrDefault(mood, 0) + 1);
            }
        }
        return moodCounts;
    }

    public void updateWeeklySummary() {
        Map<String, Integer> weeklyCounts = getWeeklyMoodCounts();
        String topMood = null;
        int max = 0;
        for (Map.Entry<String, Integer> e : weeklyCounts.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
                topMood = e.getKey();
            }
        }
        String encouragement = (topMood != null)
                ? "Keep it up! Your most frequent mood this week: " + topMood
                : "No moods recorded this week.";
        weeklySummaryTextView.setText(encouragement + "\n" + weeklyCounts.toString());
    }
}