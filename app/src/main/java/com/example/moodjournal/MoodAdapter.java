package com.example.moodjournal;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MoodAdapter extends RecyclerView.Adapter<MoodAdapter.MoodViewHolder> {
    private List<MoodEntry> moodEntries;

    public MoodAdapter(List<MoodEntry> moodEntries) {
        this.moodEntries = moodEntries;
    }

    public MoodAdapter(HistoryActivity historyActivity, List<JournalEntry> journalEntries) {

    }

    @NonNull
    @Override
    public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mood_entry, parent, false);
        return new MoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
        MoodEntry entry = moodEntries.get(position);

        // Set emoji and mood name
        holder.textMoodEmoji.setText(entry.getEmoji());
        holder.textMoodName.setText(entry.getMood());
        holder.textDate.setText(entry.getDate());

        // Set background color based on mood
        String backgroundColor = getMoodBackgroundColor(entry.getMood());
        if (backgroundColor != null) {
            holder.cardView.setCardBackgroundColor(Color.parseColor(backgroundColor));
        }

        // Handle note visibility
        if (entry.getNote() != null && !entry.getNote().trim().isEmpty()) {
            holder.textNote.setText(entry.getNote());
            holder.textNote.setVisibility(View.VISIBLE);
        } else {
            holder.textNote.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return moodEntries.size();
    }

    public void updateData(List<MoodEntry> newEntries) {
        this.moodEntries = newEntries;
        notifyDataSetChanged();
    }

    private String getMoodBackgroundColor(String mood) {
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

    public void updateEntries(List<JournalEntry> journalEntries) {

    }

    static class MoodViewHolder extends RecyclerView.ViewHolder {
        TextView textMoodEmoji, textMoodName, textDate, textNote;
        CardView cardView;

        public MoodViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            textMoodEmoji = itemView.findViewById(R.id.textMoodEmoji);
            textMoodName = itemView.findViewById(R.id.textMoodName);
            textDate = itemView.findViewById(R.id.textDate);
            textNote = itemView.findViewById(R.id.textNote);
        }
    }
}