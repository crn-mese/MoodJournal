package com.example.moodjournal;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {
    private static final String TAG = "HistoryActivity";

    private MoodAdapter adapter;
    private List<MoodEntry> moodEntries;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initializeFirebase();
        initializeViews();
        loadMoodHistory();
    }

    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }

    private void initializeViews() {
        RecyclerView recyclerView = findViewById(R.id.recyclerViewHistory);
        moodEntries = new ArrayList<>();
        adapter = new MoodAdapter(moodEntries);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadMoodHistory() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please sign in to view history.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        // FIXED: Changed to use "journal_entries" collection (same as MainActivity)
        firestore.collection("journal_entries")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        moodEntries.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Convert JournalEntry to MoodEntry for display
                            JournalEntry journalEntry = document.toObject(JournalEntry.class);
                            MoodEntry moodEntry = convertToMoodEntry(journalEntry);
                            moodEntries.add(moodEntry);
                        }
                        adapter.updateData(moodEntries);
                        if (moodEntries.isEmpty()) {
                            Toast.makeText(HistoryActivity.this, "No mood entries found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w(TAG, "Error getting documents: ", task.getException());
                        Toast.makeText(HistoryActivity.this, "Error loading history.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Helper method to convert JournalEntry to MoodEntry
    private MoodEntry convertToMoodEntry(JournalEntry journalEntry) {
        String emoji = MoodHelper.getMoodEmoji(journalEntry.getMood());
        String dateString = "";

        if (journalEntry.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            dateString = sdf.format(journalEntry.getTimestamp());
        }

        long timestamp = journalEntry.getTimestamp() != null ?
                journalEntry.getTimestamp().getTime() : System.currentTimeMillis();

        return new MoodEntry(
                journalEntry.getMood(),
                emoji,
                journalEntry.getContent(),
                dateString,
                timestamp
        );
    }
}