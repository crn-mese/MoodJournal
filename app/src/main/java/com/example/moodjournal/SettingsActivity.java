package com.example.moodjournal;

import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity {
    private Switch switchTheme;
    private Button buttonSetTime;
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
        switchTheme = findViewById(R.id.switchTheme);
        buttonSetTime = findViewById(R.id.buttonSetTime);
        textCurrentTime = findViewById(R.id.textCurrentTime);

        sharedPreferences = getSharedPreferences("MoodJournalPrefs", MODE_PRIVATE);
    }

    private void loadSettings() {
        // Load theme preference
        boolean isDarkMode = sharedPreferences.getBoolean("dark_theme", false);
        switchTheme.setChecked(isDarkMode);

        // Load reminder time
        int hour = sharedPreferences.getInt("reminder_hour", 9);
        int minute = sharedPreferences.getInt("reminder_minute", 0);
        updateTimeDisplay(hour, minute);
    }

    private void setupListeners() {
        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // save theme preference
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("dark_theme", isChecked);
            editor.apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        buttonSetTime.setOnClickListener(v -> showTimePickerDialog());
    }

    private void showTimePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int currentHour = sharedPreferences.getInt("reminder_hour", 9);
        int currentMinute = sharedPreferences.getInt("reminder_minute", 0);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    // save the selected time
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