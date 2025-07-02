package com.example.moodjournal;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EnhancedHistoryActivity extends AppCompatActivity
        implements CalendarMoodView.OnDateClickListener, CalendarMoodView.OnMonthChangeListener {

    private static final String TAG = "EnhancedHistoryActivity";

    private MoodAdapter adapter;
    private List<MoodEntry> moodEntries;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;

    // Calendar view components
    private RecyclerView recyclerView;
    private CalendarMoodView calendarView;
    private MoodLegendView legendView;
    private boolean isCalendarView = false;
    private Map<String, List<MoodEntry>> calendarMoodData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enhanced_history);

        initializeFirebase();
        initializeViews();
        loadMoodHistory();
    }

    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        calendarMoodData = new HashMap<>();
    }

    private void initializeViews() {
        // Initialize RecyclerView (existing functionality)
        recyclerView = findViewById(R.id.recyclerViewHistory);
        moodEntries = new ArrayList<>();
        adapter = new MoodAdapter(moodEntries);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Initialize Calendar View (new functionality)
        calendarView = findViewById(R.id.calendarView);
        legendView = findViewById(R.id.legendView);

        calendarView.setOnDateClickListener(this);
        calendarView.setOnMonthChangeListener(this);

        // Initially show list view
        showListView();
    }

    private void loadMoodHistory() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please sign in to view history.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        // Load last 6 months of data for calendar view
        Calendar sixMonthsAgo = Calendar.getInstance();
        sixMonthsAgo.add(Calendar.MONTH, -6);

        firestore.collection("journal_entries")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        moodEntries.clear();
                        calendarMoodData.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            JournalEntry journalEntry = document.toObject(JournalEntry.class);
                            MoodEntry moodEntry = convertToMoodEntry(journalEntry);
                            moodEntries.add(moodEntry);

                            // Group by date for calendar view
                            String dateKey = getDateKey(moodEntry.getTimestamp());
                            calendarMoodData.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(moodEntry);
                        }

                        // Update both views
                        adapter.updateData(moodEntries);
                        calendarView.setMoodData(calendarMoodData);

                        if (moodEntries.isEmpty()) {
                            Toast.makeText(EnhancedHistoryActivity.this, "No mood entries found.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w(TAG, "Error getting documents: ", task.getException());
                        Toast.makeText(EnhancedHistoryActivity.this, "Error loading history.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private MoodEntry convertToMoodEntry(JournalEntry journalEntry) {
        String emoji = MoodHelper.getMoodEmoji(journalEntry.getMood());
        String formattedDateTimeString = "";

        if (journalEntry.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            formattedDateTimeString = sdf.format(journalEntry.getTimestamp());
        }

        long timestamp = journalEntry.getTimestamp() != null ?
                journalEntry.getTimestamp().getTime() : System.currentTimeMillis();

        return new MoodEntry(
                journalEntry.getMood(),
                emoji,
                journalEntry.getContent(),
                formattedDateTimeString,
                timestamp
        );
    }

    private String getDateKey(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        return String.format(Locale.getDefault(), "%04d-%02d-%02d",
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.history_menu, menu);
        MenuItem toggleItem = menu.findItem(R.id.action_toggle_view);
        toggleItem.setIcon(isCalendarView ? R.drawable.ic_list : R.drawable.ic_calendar);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_toggle_view) {
            toggleView();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleView() {
        if (isCalendarView) {
            showListView();
        } else {
            showCalendarView();
        }
        invalidateOptionsMenu(); // Update menu icon
    }

    private void showListView() {
        isCalendarView = false;
        recyclerView.setVisibility(View.VISIBLE);
        calendarView.setVisibility(View.GONE);
        legendView.setVisibility(View.GONE);
    }

    private void showCalendarView() {
        isCalendarView = true;
        recyclerView.setVisibility(View.GONE);
        calendarView.setVisibility(View.VISIBLE);
        legendView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDateClick(Calendar date, List<MoodEntry> moods) {
        if (moods != null && !moods.isEmpty()) {
            MoodDetailsDialog dialog = new MoodDetailsDialog(this, date, moods);
            dialog.show();
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String dateStr = sdf.format(date.getTime());
            Toast.makeText(this, "No moods recorded for " + dateStr, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMonthChange(Calendar month) {
        // Optional: Load additional data for the new month if needed
        Log.d(TAG, "Month changed to: " + month.getTime());
    }
}
