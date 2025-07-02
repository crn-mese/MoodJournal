package com.example.moodjournal;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MoodDetailsDialog extends Dialog {
    private Calendar date;
    private List<MoodEntry> moods;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

    public MoodDetailsDialog(Context context, Calendar date, List<MoodEntry> moods) {
        super(context);
        this.date = date;
        this.moods = moods;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_mood_details);

        TextView dateText = findViewById(R.id.dateText);
        LinearLayout moodContainer = findViewById(R.id.moodContainer);

        dateText.setText(dateFormat.format(date.getTime()));

        // Add mood entries using the same style as your existing cards
        for (MoodEntry mood : moods) {
            addMoodEntry(moodContainer, mood);
        }
    }

    private void addMoodEntry(LinearLayout container, MoodEntry mood) {
        // Create a card view similar to your existing item_mood_entry.xml
        CardView cardView = new CardView(getContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(16, 8, 16, 8);
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(24f);
        cardView.setCardElevation(8f);

        // Set background color based on mood (same as your adapter)
        String backgroundColor = getMoodBackgroundColor(mood.getMoodType());
        if (backgroundColor != null) {
            cardView.setCardBackgroundColor(Color.parseColor(backgroundColor));
        }

        // Create inner layout
        LinearLayout innerLayout = new LinearLayout(getContext());
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        innerLayout.setPadding(32, 32, 32, 32);

        // Create horizontal layout for emoji and mood info
        LinearLayout horizontalLayout = new LinearLayout(getContext());
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
        horizontalLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);

        // Emoji
        TextView emojiText = new TextView(getContext());
        emojiText.setText(mood.getEmoji());
        emojiText.setTextSize(28f);
        LinearLayout.LayoutParams emojiParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        emojiParams.setMarginEnd(24);
        emojiText.setLayoutParams(emojiParams);

        // Mood info layout
        LinearLayout moodInfoLayout = new LinearLayout(getContext());
        moodInfoLayout.setOrientation(LinearLayout.VERTICAL);
        moodInfoLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        // Mood name
        TextView moodText = new TextView(getContext());
        moodText.setText(mood.getMoodType());
        moodText.setTextSize(16f);
        moodText.setTypeface(null, android.graphics.Typeface.BOLD);
        moodText.setTextColor(Color.parseColor("#333333"));

        // Time
        TextView timeText = new TextView(getContext());
        timeText.setText(timeFormat.format(mood.getTimestamp()));
        timeText.setTextSize(12f);
        timeText.setTextColor(Color.parseColor("#666666"));

        moodInfoLayout.addView(moodText);
        moodInfoLayout.addView(timeText);

        horizontalLayout.addView(emojiText);
        horizontalLayout.addView(moodInfoLayout);

        innerLayout.addView(horizontalLayout);

        // Note (if exists)
        if (mood.getNote() != null && !mood.getNote().trim().isEmpty()) {
            TextView noteText = new TextView(getContext());
            noteText.setText(mood.getNote());
            noteText.setTextSize(14f);
            noteText.setTextColor(Color.parseColor("#555555"));
            LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            noteParams.setMargins(0, 24, 0, 0);
            noteText.setLayoutParams(noteParams);
            innerLayout.addView(noteText);
        }

        cardView.addView(innerLayout);
        container.addView(cardView);
    }

    private String getMoodBackgroundColor(String mood) {
        // Use the same colors as your MoodAdapter
        switch (mood.toLowerCase()) {
            case "happy": return "#FFF1B6";      // Pastel Yellow
            case "sad": return "#D4E2FC";        // Soft Blue
            case "angry": return "#F5B6B6";      // Soft Red
            case "anxious": return "#E8DFF5";    // Lavender
            case "tired": return "#DADADA";      // Muted Gray
            case "calm": return "#B8E2C8";       // Mint Green
            case "stressed": return "#FFC8A2";   // Peach Orange
            case "excited": return "#FFD6E0";    // Pink Blush
            case "lonely": return "#C3C9E9";     // Pale Indigo
            case "neutral": return "#F1F1F1";    // Light Gray
            default: return "#FFFFFF";           // White default
        }
    }
}
