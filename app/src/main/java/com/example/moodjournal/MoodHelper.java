package com.example.moodjournal;

import java.util.HashMap;
import java.util.Map;

public class MoodHelper {

    public static String getMoodEmoji(String mood) {
        switch (mood.toLowerCase()) {
            case "happy": return "😄";
            case "sad": return "😢";
            case "angry": return "😠";
            case "anxious": return "😰";
            case "calm": return "😌";
            default: return "😐";
        }
    }

    public static String getMoodDescription(String mood) {
        switch (mood.toLowerCase()) {
            case "happy": return "Feeling joyful and cheerful";
            case "sad": return "Feeling down or unhappy";
            case "angry": return "Frustrated or mad";
            case "anxious": return "Nervous or uneasy";
            case "calm": return "Relaxed and at peace";
            default: return "Unknown mood";
        }
    }

    public static String getMoodColor(String mood) {
        switch (mood.toLowerCase()) {
            case "happy": return "#FFF1B6";
            case "sad": return "#D4E2FC";
            case "angry": return "#F5B6B6";
            case "anxious": return "#E8DFF5";
            case "calm": return "#B8E2C8";
            default: return "#FFFFFF";
        }
    }
}