package com.example.moodjournal;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


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
import com.example.moodjournal.MoodSummary;

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
        adapter = new MoodAdapter(this, moodEntries);
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
        getMenuInflater().inflate(R.menu.menu_history, menu);
        MenuItem toggleItem = menu.findItem(R.id.action_toggle_view);
        toggleItem.setIcon(isCalendarView ? R.drawable.ic_list : R.drawable.ic_calendar);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_toggle_view) {
            toggleView();
            return true;
        } else if (item.getItemId() == R.id.action_monthly_insights) {
            showMonthlyInsights();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showMonthlyInsights() {
        if (moodEntries == null || moodEntries.isEmpty()) {
            Toast.makeText(this, "No mood entries for this month.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Integer> moodCounts = new HashMap<>();
        int total = 0;
        Calendar now = Calendar.getInstance();
        int month = now.get(Calendar.MONTH);
        int year = now.get(Calendar.YEAR);

        String topMood = null;
        int max = 0;
        long topMoodTimestamp = 0;

        for (MoodEntry entry : moodEntries) {
            Calendar entryCal = Calendar.getInstance();
            entryCal.setTimeInMillis(entry.getTimestamp());
            if (entryCal.get(Calendar.MONTH) == month && entryCal.get(Calendar.YEAR) == year) {
                String mood = entry.getMood();
                moodCounts.put(mood, moodCounts.getOrDefault(mood, 0) + 1);
                total++;
                if (moodCounts.get(mood) > max) {
                    max = moodCounts.get(mood);
                    topMood = mood;
                    topMoodTimestamp = entry.getTimestamp();
                }
            }
        }

        if (total == 0) {
            Toast.makeText(this, "No mood entries for this month.", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder stats = new StringBuilder();
        for (Map.Entry<String, Integer> e : moodCounts.entrySet()) {
            int percent = (int) ((e.getValue() * 100.0f) / total);
            stats.append(e.getKey()).append(": ").append(percent).append("%\n");
        }

        String topMoodDate = "";
        if (topMoodTimestamp != 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
            topMoodDate = sdf.format(new java.util.Date(topMoodTimestamp));
        }

        String suggestion = (topMood != null)
                ? "Most common mood: " + topMood + " (on " + topMoodDate + "). Keep tracking your moods!"
                : "No moods recorded this month.";

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Monthly Mood Insights")
                .setMessage(stats.toString() + "\n" + suggestion)
                .setPositiveButton("OK", null)
                .show();
    }
    private void showWeeklyMoodSummary(List<MoodEntry> allEntries) {
        // Filter entries for the current week
        Calendar now = Calendar.getInstance();
        int week = now.get(Calendar.WEEK_OF_YEAR);
        int year = now.get(Calendar.YEAR);

        List<MoodEntry> weeklyEntries = new ArrayList<>();
        for (MoodEntry entry : allEntries) {
            Calendar entryCal = Calendar.getInstance();
            entryCal.setTimeInMillis(entry.getTimestamp()); // FIXED
            if (entryCal.get(Calendar.WEEK_OF_YEAR) == week && entryCal.get(Calendar.YEAR) == year) {
                weeklyEntries.add(entry);
            }
        }

        // Use MoodSummary to get counts and encouragement
        MoodSummary summary = new MoodSummary(weeklyEntries);
        Map<String, Integer> moodCounts = summary.getMoodCounts();
        String encouragement = summary.getEncouragementMessage();

        // Build summary text
        // In EnhancedHistoryActivity.java, inside showWeeklyMoodSummary()
        StringBuilder sb = new StringBuilder();
        sb.append("This Week's Mood Summary:\n");
        for (Map.Entry<String, Integer> entry : moodCounts.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(" ");
            // Add a simple bar: one '|' per mood count
            for (int i = 0; i < entry.getValue(); i++) {
                sb.append("|");
            }
            sb.append("\n");
        }
        sb.append("\n").append(encouragement);

        // Set to TextView
        android.widget.TextView summaryText = findViewById(R.id.weeklySummaryText); // FIXED
        summaryText.setText(sb.toString());
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
