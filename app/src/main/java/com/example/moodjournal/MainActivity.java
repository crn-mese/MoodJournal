package com.example.moodjournal;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat; // For using ContextCompat.getColorStateList

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity"; // For logging

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String selectedMood = "";
    private EditText noteEditText;
    private TextView dateTextView;
    private Button[] moodButtons;

    // Define color resource IDs for mood buttons (ensure these are in colors.xml)
    // Example: <color name="mood_happy_default">#FFEB3B</color>
    private final int[] moodDefaultColors = {
            R.color.mood_happy, R.color.mood_sad, R.color.mood_angry,
            R.color.mood_anxious, R.color.mood_calm
    };
    private final int selectedMoodColor = R.color.selected_mood; // Example: <color name="selected_mood">#00BCD4</color>


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please sign in to continue.", Toast.LENGTH_LONG).show();
            // Redirect to SignInActivity
            Intent signInIntent = new Intent(this, SignInActivity.class);
            startActivity(signInIntent);
            finish(); // Prevent coming back to MainActivity without signing in
            return; // Stop further execution in onCreate if not signed in
        }

        initializeViews();
        setupDateDisplay();
        setupMoodButtons();
        setupSaveButton();
        setupNavigationButtons();
    }

    private void initializeViews() {
        dateTextView = findViewById(R.id.dateTextView);
        noteEditText = findViewById(R.id.noteEditText);
        moodButtons = new Button[]{
                findViewById(R.id.happyButton),
                findViewById(R.id.sadButton),
                findViewById(R.id.angryButton),
                findViewById(R.id.anxiousButton),
                findViewById(R.id.calmButton)
        };
    }

    private void setupDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault());
        dateTextView.setText(sdf.format(new Date()));
    }

    private void setupMoodButtons() {
        String[] moods = {"Happy", "Sad", "Angry", "Anxious", "Calm"};

        for (int i = 0; i < moodButtons.length; i++) {
            final String mood = moods[i];
            final Button button = moodButtons[i];
            final int defaultColorResId = moodDefaultColors[i];

            // Set initial background tint
            button.setBackgroundTintList(ContextCompat.getColorStateList(this, defaultColorResId));
            button.setOnClickListener(v -> selectMood(mood, button));
        }
    }

    private void selectMood(String mood, Button selectedButton) {
        resetMoodButtonsUI();
        selectedButton.setBackgroundTintList(ContextCompat.getColorStateList(this, selectedMoodColor));
        selectedButton.setElevation(8f); // Add some elevation for visual feedback
        selectedMood = mood;
        Toast.makeText(this, "Selected: " + mood, Toast.LENGTH_SHORT).show();
    }

    private void resetMoodButtonsUI() {
        for (int i = 0; i < moodButtons.length; i++) {
            moodButtons[i].setBackgroundTintList(ContextCompat.getColorStateList(this, moodDefaultColors[i]));
            moodButtons[i].setElevation(4f); // Reset elevation
        }
    }

    private void setupSaveButton() {
        Button saveBtn = findViewById(R.id.saveButton);
        saveBtn.setOnClickListener(v -> attemptSaveMoodEntry());
    }

    private void attemptSaveMoodEntry() {
        FirebaseUser currentUser = auth.getCurrentUser();
        // This check is good, but onCreate already redirects if null,
        // so currentUser should ideally not be null here unless there's a race condition
        // or the user gets signed out in the background.
        if (currentUser == null) {
            Toast.makeText(this, "Session expired. Please sign in again.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        if (selectedMood.isEmpty()) {
            Toast.makeText(this, "Please select a mood.", Toast.LENGTH_SHORT).show();
            return;
        }

        String noteContent = noteEditText.getText().toString().trim();
        if (noteContent.isEmpty()) {
            Toast.makeText(this, "Please write a note for your entry.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String entryTitle = "Mood: " + selectedMood + " (" +
                new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date()) + ")";

        JournalEntry newEntry = new JournalEntry(userId, entryTitle, noteContent, selectedMood);

        Toast.makeText(this, "Saving entry...", Toast.LENGTH_SHORT).show();
        // You might want to disable the save button here to prevent double clicks
        // findViewById(R.id.saveButton).setEnabled(false);


        db.collection("journal_entries")
                .add(newEntry)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                    Toast.makeText(MainActivity.this, "Entry saved successfully!", Toast.LENGTH_LONG).show();
                    clearInputFields();
                    // findViewById(R.id.saveButton).setEnabled(true); // Re-enable save button
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error adding document", e);
                    Toast.makeText(MainActivity.this, "Error saving entry: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    // findViewById(R.id.saveButton).setEnabled(true); // Re-enable save button
                });
    }

    private void clearInputFields() {
        selectedMood = "";
        noteEditText.setText("");
        resetMoodButtonsUI();
    }

    private void setupNavigationButtons() {
        Button historyBtn = findViewById(R.id.historyButton);
        Button settingsBtn = findViewById(R.id.settingsButton);
        Button logoutBtn = findViewById(R.id.logoutButton);

        historyBtn.setOnClickListener(v -> {
            // TODO: Implement HistoryActivity and navigate to it
             Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
             startActivity(intent);
//            Toast.makeText(this, "History feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        settingsBtn.setOnClickListener(v -> {
            // TODO: Implement SettingsActivity and navigate to it
             Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
             startActivity(intent);
//            Toast.makeText(this, "Settings feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        logoutBtn.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(MainActivity.this, "Logged out successfully.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
            startActivity(intent);
            finish();
        });
    }
}