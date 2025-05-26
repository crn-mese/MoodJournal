package com.example.moodjournal;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    private MoodAdapter adapter;
    private List<MoodEntry> moodEntries;
    private DatabaseReference databaseReference;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        initializeViews();
        setupFirebase();
        loadMoodHistory();
    }

    private void initializeViews() {
        RecyclerView recyclerView = findViewById(R.id.recyclerViewHistory);
        moodEntries = new ArrayList<>();
        adapter = new MoodAdapter(moodEntries);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("moods");
    }

    private void loadMoodHistory() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        String userId = currentUser.getUid();

        databaseReference.child(userId)
                .orderByChild("timestamp")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        moodEntries.clear();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            MoodEntry entry = snapshot.getValue(MoodEntry.class);
                            if (entry != null) {
                                moodEntries.add(entry);
                            }
                        }

                        // Sort by timestamp (newest first)
                        moodEntries.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));

                        adapter.updateData(moodEntries);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle error if needed
                    }
                });
    }
}