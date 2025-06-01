package com.example.moodjournal;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity {
    private RadioGroup themeRadioGroup;
    private SwitchMaterial switchNotifications;
    private Button buttonSetTime, saveButton, backButton;
    private TextView textCurrentTime;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initializeViews();
        loadSettings();
        setupListeners();
    }

    private void initializeViews() {
        themeRadioGroup = findViewById(R.id.themeRadioGroup);
        switchNotifications = findViewById(R.id.switchTheme);
        buttonSetTime = findViewById(R.id.buttonSetTime);
        textCurrentTime = findViewById(R.id.textCurrentTime);
        saveButton = findViewById(R.id.saveButton);
        backButton = findViewById(R.id.backButton);

        sharedPreferences = getSharedPreferences("MoodJournalPrefs", MODE_PRIVATE);
    }

    private void loadSettings() {
        // Load theme preference
        String currentTheme = sharedPreferences.getString("theme_preference", "light");
        switch (currentTheme) {
            case "light":
                themeRadioGroup.check(R.id.lightThemeRadio);
                break;
            case "dark":
                themeRadioGroup.check(R.id.darkThemeRadio);
                break;
            case "system":
                themeRadioGroup.check(R.id.systemThemeRadio);
                break;
        }

        // Load notification preference
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true);
        switchNotifications.setChecked(notificationsEnabled);

        // Load reminder time
        int hour = sharedPreferences.getInt("reminder_hour", 21);
        int minute = sharedPreferences.getInt("reminder_minute", 0);
        updateTimeDisplay(hour, minute);
    }

    private void setupListeners() {
        // Theme selection listener - immediate feedback
        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            // Visual feedback only - actual saving happens on Save button
            Toast.makeText(this, "Theme selected! Click Save to apply.", Toast.LENGTH_SHORT).show();
        });

        // Notification switch listener
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("notifications_enabled", isChecked);
            editor.apply();
        });

        // Time picker listener
        buttonSetTime.setOnClickListener(v -> showTimePickerDialog());

        // Save button listener
        saveButton.setOnClickListener(v -> saveThemeSettings());

        // Back button listener
        backButton.setOnClickListener(v -> finish());
    }

    private void saveThemeSettings() {
        String selectedTheme = "light";

        int selectedId = themeRadioGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.lightThemeRadio) {
            selectedTheme = "light";
        } else if (selectedId == R.id.darkThemeRadio) {
            selectedTheme = "dark";
        } else if (selectedId == R.id.systemThemeRadio) {
            selectedTheme = "system";
        }

        // Save theme preference
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("theme_preference", selectedTheme);
        editor.apply();

        // Apply theme immediately
        applyTheme(selectedTheme);

        Toast.makeText(this, "Settings saved successfully!", Toast.LENGTH_SHORT).show();
    }

    private void applyTheme(String theme) {
        switch (theme) {
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "system":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
        recreate();
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int currentHour = sharedPreferences.getInt("reminder_hour", 21);
        int currentMinute = sharedPreferences.getInt("reminder_minute", 0);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("reminder_hour", hourOfDay);
                    editor.putInt("reminder_minute", minute);
                    editor.apply();

                    updateTimeDisplay(hourOfDay, minute);
                },
                currentHour,
                currentMinute,
                false
        );

        timePickerDialog.show();
    }

    private void updateTimeDisplay(int hour, int minute) {
        String amPm = hour >= 12 ? "PM" : "AM";
        int displayHour = hour == 0 ? 12 : (hour > 12 ? hour - 12 : hour);
        String timeText = String.format("Current: %d:%02d %s", displayHour, minute, amPm);
        textCurrentTime.setText(timeText);
    }
}
