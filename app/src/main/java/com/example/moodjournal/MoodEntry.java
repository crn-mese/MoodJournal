package com.example.moodjournal;

public class MoodEntry {
    private String mood;
    private String emoji;
    private String note;
    private String date;
    private long timestamp;

    public MoodEntry() {}

    public MoodEntry(String mood, String emoji, String note, String date, long timestamp) {
        this.mood = mood;
        this.emoji = emoji;
        this.note = note;
        this.date = date;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}