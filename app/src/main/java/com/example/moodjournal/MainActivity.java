package com.example.moodjournal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private String selectedMood = "";
    private EditText noteEditText;
    private TextView dateTextView;
    private Button[] moodButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupDateDisplay();
        setupMoodButtons();
        setupSaveButton();
        setupNavigationButtons();
    }

    private void initializeViews() {
        dateTextView = findViewById(R.id.dateTextView);
        noteEditText = findViewById(R.id.noteEditText);

        // Initialize mood buttons array for easy management
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
        // Happy button
        moodButtons[0].setOnClickListener(v -> selectMood("Happy", moodButtons[0]));

        // Sad button
        moodButtons[1].setOnClickListener(v -> selectMood("Sad", moodButtons[1]));

        // Angry button
        moodButtons[2].setOnClickListener(v -> selectMood("Angry", moodButtons[2]));

        // Anxious button
        moodButtons[3].setOnClickListener(v -> selectMood("Anxious", moodButtons[3]));

        // Calm button
        moodButtons[4].setOnClickListener(v -> selectMood("Calm", moodButtons[4]));
    }

    private void selectMood(String mood, Button selectedButton) {
        // Reset all button backgrounds to default
        resetMoodButtons();

        // Highlight selected button
        selectedButton.setBackgroundTint(getResources().getColor(R.color.selected_mood));
        selectedButton.setElevation(8f); // Add elevation for selected state

        selectedMood = mood;

        // Show feedback to user
        Toast.makeText(this, "Selected: " + mood, Toast.LENGTH_SHORT).show();
    }

    private void resetMoodButtons() {
        int[] originalColors = {
                R.color.mood_happy,
                R.color.mood_sad,
                R.color.mood_angry,
                R.color.mood_anxious,
                R.color.mood_calm
        };

        for (int i = 0; i < moodButtons.length; i++) {
            moodButtons[i].setBackgroundTint(getResources().getColor(originalColors[i]));
            moodButtons[i].setElevation(4f); // Reset elevation
        }
    }

    private void setupSaveButton() {
        Button saveBtn = findViewById(R.id.saveButton);
        saveBtn.setOnClickListener(v -> saveMoodEntry());
    }

    private void saveMoodEntry() {
        if (selectedMood.isEmpty()) {
            Toast.makeText(this, "Please select a mood first!", Toast.LENGTH_SHORT).show();
            return;
        }

        String note = noteEditText.getText().toString().trim();

        // TODO: This will be replaced with Firebase call when Person A is ready
        // For now, just simulate saving
        simulateSave(selectedMood, note);
    }

    private void simulateSave(String mood, String note) {
        // Simulate saving process
        Toast.makeText(this, "Saving mood: " + mood, Toast.LENGTH_SHORT).show();

        // Clear form after "saving"
        selectedMood = "";
        noteEditText.setText("");
        resetMoodButtons();

        Toast.makeText(this, "Mood saved successfully! ✅", Toast.LENGTH_LONG).show();
    }

    private void setupNavigationButtons() {
        Button historyBtn = findViewById(R.id.historyButton);
        Button settingsBtn = findViewById(R.id.settingsButton);
        Button logoutBtn = findViewById(R.id.logoutButton);

        // For now, just show placeholder messages
        historyBtn.setOnClickListener(v ->
                Toast.makeText(this, "History feature coming soon!", Toast.LENGTH_SHORT).show());

        settingsBtn.setOnClickListener(v ->
                Toast.makeText(this, "Settings feature coming soon!", Toast.LENGTH_SHORT).show());

        logoutBtn.setOnClickListener(v ->
                Toast.makeText(this, "Logout feature coming soon!", Toast.LENGTH_SHORT).show());
    }
}
