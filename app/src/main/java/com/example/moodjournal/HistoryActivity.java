package com.example.moodjournal;

import android.os.Bundle;
import android.util.Log; // For logging
import android.widget.Toast; // For user feedback

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore; // Import Firestore
import com.google.firebase.firestore.Query; // Import Query
import com.google.firebase.firestore.QueryDocumentSnapshot; // For iterating documents

import java.util.ArrayList;
import java.util.List;
// import java.util.Collections; // If you still need to sort client-side for some reason

public class HistoryActivity extends AppCompatActivity {
    private static final String TAG = "HistoryActivity"; // For logging

    private MoodAdapter adapter;
    private List<JournalEntry> journalEntries; // Use JournalEntry
    private FirebaseFirestore db; // Firestore instance
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initializeFirebase(); // Initialize Firebase first
        initializeViews();    // Then views
        loadMoodHistory();
    }

    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance(); // Get Firestore instance
    }

    private void initializeViews() {
        RecyclerView recyclerView = findViewById(R.id.recyclerViewHistory);
        journalEntries = new ArrayList<>();
        // You'll need to adjust MoodAdapter to take List<JournalEntry>
        // or create a new adapter specifically for JournalEntry.
        // For now, let's assume MoodAdapter can be adapted or you'll create JournalEntryAdapter.
        adapter = new MoodAdapter(this, journalEntries); // Pass context if needed by adapter

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadMoodHistory() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please sign in to view history.", Toast.LENGTH_SHORT).show();
            // Optionally, navigate to SignInActivity
            // finish();
            return;
        }

        String userId = currentUser.getUid();

        db.collection("journal_entries") // Collection name from MainActivity
                .whereEqualTo("userId", userId) // Filter by the current user's ID
                .orderBy("timestamp", Query.Direction.DESCENDING) // Order by timestamp, newest first
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        journalEntries.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            JournalEntry entry = document.toObject(JournalEntry.class);
                            // The @DocumentId field in JournalEntry will be automatically populated.
                            journalEntries.add(entry);
                        }
                        adapter.updateEntries(journalEntries); // Assuming MoodAdapter has this method
                        if (journalEntries.isEmpty()) {
                            Toast.makeText(HistoryActivity.this, "No entries found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w(TAG, "Error getting documents: ", task.getException());
                        Toast.makeText(HistoryActivity.this, "Error loading history.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}