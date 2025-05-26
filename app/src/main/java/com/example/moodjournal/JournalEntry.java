package com.example.moodjournal; // Ensure this matches your package structure

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class JournalEntry {

    @DocumentId // Firestore will use this field to store the document's ID
    private String id;

    private String userId;      // To link the entry to a specific Firebase User
    private String title;       // A title for the journal entry
    private String content;     // The main text/note of the journal entry
    private String mood;        // The selected mood (e.g., "Happy", "Sad")

    @ServerTimestamp // Firestore will automatically populate this with the server's timestamp
    private Date timestamp;     // When the entry was created/saved

    // --- Constructors ---

    /**
     * Public no-argument constructor is REQUIRED for Firestore to deserialize
     * documents back into JournalEntry objects.
     */
    public JournalEntry() {
        // Firestore needs this empty constructor
    }

    /**
     * Constructor to create a new JournalEntry.
     * The 'id' and 'timestamp' are typically set by Firestore.
     *
     * @param userId The ID of the user creating the entry.
     * @param title The title of the journal entry.
     * @param content The main content or note of the entry.
     * @param mood The mood selected for this entry.
     */
    public JournalEntry(String userId, String title, String content, String mood) {
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.mood = mood;
        // 'timestamp' will be set by @ServerTimestamp annotation by Firestore upon saving
        // 'id' will be set by Firestore or can be set manually if you retrieve a document
    }

    // --- Getters ---
    // Getters are REQUIRED for Firestore to serialize the object into a document.

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getMood() {
        return mood;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    // --- Setters ---
    // Setters are often needed if you plan to modify these objects after creation,
    // and Firestore also uses them for deserialization in some cases (though public fields
    // or a constructor with all args can also work). It's good practice to include them.

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    // You could add an @Override toString() method for easier debugging if needed
    @Override
    public String toString() {
        return "JournalEntry{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", mood='" + mood + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}