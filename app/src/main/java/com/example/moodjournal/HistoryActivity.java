package com.example.moodjournal;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
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
        recyclerView = findViewById(R.id.recyclerViewHistory);
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
        String userId = firebaseAuth.getCurrentUser().getUid();

        databaseReference.child(userId)
                .orderByChild("timestamp")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        moodEntries.clear();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            MoodEntry entry = snapshot.getValue(MoodEntry.class);
                            if (entry != null) {
                                moodEntries.add(entry);
                            }
                        }

                        Collections.sort(moodEntries, (a, b) ->
                                Long.compare(b.getTimestamp(), a.getTimestamp()));

                        adapter.updateData(moodEntries);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }
}