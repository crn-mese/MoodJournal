// MoodSummary.java
package com.example.moodjournal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoodSummary {
    private List<MoodEntry> entries;

    public MoodSummary(List<MoodEntry> entries) {
        this.entries = entries;
    }

    public Map<String, Integer> getMoodCounts() {
        Map<String, Integer> counts = new HashMap<>();
        for (MoodEntry entry : entries) {
            String mood = entry.getMood();
            counts.put(mood, counts.getOrDefault(mood, 0) + 1);
        }
        return counts;
    }

    public String getEncouragementMessage() {
        if (entries == null || entries.isEmpty()) {
            return "No moods recorded this week. Keep tracking!";
        }
        // Example: encourage if happy is most common
        Map<String, Integer> counts = getMoodCounts();
        String topMood = null;
        int max = 0;
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
                topMood = e.getKey();
            }
        }
        if ("happy".equalsIgnoreCase(topMood)) {
            return "Great job staying positive!";
        } else {
            return "Keep going! Every mood is valid.";
        }
    }
}