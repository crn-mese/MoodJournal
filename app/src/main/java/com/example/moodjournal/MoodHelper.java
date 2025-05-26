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
            case "tired": return "😴";
            case "calm": return "😌";
            case "stressed": return "😩";
            case "excited": return "🤩";
            case "lonely": return "😔";
            case "neutral": return "😐";
            default: return "😐";
        }
    }

    public static String getMoodDescription(String mood) {
        switch (mood.toLowerCase()) {
            case "happy": return "Feeling joyful and cheerful";
            case "sad": return "Feeling down or unhappy";
            case "angry": return "Frustrated or mad";
            case "anxious": return "Nervous or uneasy";
            case "tired": return "Physically or mentally drained";
            case "calm": return "Relaxed and at peace";
            case "stressed": return "Overwhelmed or pressured";
            case "excited": return "Energetic and thrilled";
            case "lonely": return "Isolated or disconnected";
            case "neutral": return "Neither good nor bad";
            default: return "Unknown mood";
        }
    }

    public static String getMoodColor(String mood) {
        switch (mood.toLowerCase()) {
            case "happy": return "#FFF1B6";
            case "sad": return "#D4E2FC";
            case "angry": return "#F5B6B6";
            case "anxious": return "#E8DFF5";
            case "tired": return "#DADADA";
            case "calm": return "#B8E2C8";
            case "stressed": return "#F5B6B6";
            case "excited": return "#FFC8A2";
            case "lonely": return "#C3C9E9";
            case "neutral": return "#F1F1F1";
            default: return "#FFFFFF";
        }
    }
}