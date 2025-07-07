package com.example.moodjournal;

public class MoodEntry {
    private long id;
    private String date;
    private String emotion;
    private String note;
    private String photoPath;
    private String mood;
    private String emoji;
    private long timestamp;

    public MoodEntry() {}

    public MoodEntry(String date, String emotion, String note, String photoPath) {
        this.date = date;
        this.emotion = emotion;
        this.note = note;
        this.photoPath = photoPath;
    }

    public MoodEntry(String mood, String emoji, String note, String date, long timestamp) {
        this.mood = mood;
        this.emoji = emoji;
        this.note = note;
        this.date = date;
        this.timestamp = timestamp;
        this.photoPath = "";
    }

    public MoodEntry(String mood, String emoji, String note, String date, long timestamp, String photoPath) {
        this.mood = mood;
        this.emoji = emoji;
        this.note = note;
        this.date = date;
        this.timestamp = timestamp;
        this.photoPath = photoPath != null ? photoPath : "";
    }

    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getEmotion() { return emotion; }
    public void setEmotion(String emotion) { this.emotion = emotion; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath != null ? photoPath : ""; }

    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean hasPhoto() {
        return photoPath != null && !photoPath.trim().isEmpty();
    }
}
