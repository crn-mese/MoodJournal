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

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    private static final String TAG = "HistoryActivity";

    private MoodAdapter adapter;
    private List<MoodEntry> moodEntries;
    private FirebaseFirestore firestore; // CHANGED: Renamed from db to firestore
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
        firestore = FirebaseFirestore.getInstance(); // CHANGED: Use firestore variable name
    }

    private void initializeViews() {
        RecyclerView recyclerView = findViewById(R.id.recyclerViewHistory);
        moodEntries = new ArrayList<>(); // CHANGED: Use moodEntries
        adapter = new MoodAdapter(moodEntries); // CHANGED: Remove context parameter

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadMoodHistory() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please sign in to view history.", Toast.LENGTH_SHORT).show();
            return;
        }

        @NonNull String userId = currentUser.getUid();  // ADD @NonNull

        firestore.collection("moods") // CHANGED: Use "moods" collection
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        moodEntries.clear(); // CHANGED: Use moodEntries
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            MoodEntry entry = document.toObject(MoodEntry.class); // CHANGED: Use MoodEntry
                            moodEntries.add(entry); // CHANGED: Use moodEntries
                        }
                        adapter.updateData(moodEntries); // CHANGED: Use updateData method
                        if (moodEntries.isEmpty()) {
                            Toast.makeText(HistoryActivity.this, "No mood entries found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w(TAG, "Error getting documents: ", task.getException());
                        Toast.makeText(HistoryActivity.this, "Error loading history.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}