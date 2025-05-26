package com.example.moodjournal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MoodAdapter extends RecyclerView.Adapter<MoodAdapter.MoodViewHolder> {
    private List<MoodEntry> moodEntries;

    public MoodAdapter(List<MoodEntry> moodEntries) {
        this.moodEntries = moodEntries;
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

        holder.textMoodEmoji.setText(entry.getEmoji());
        holder.textMoodName.setText(entry.getMood());
        holder.textDate.setText(entry.getDate());

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

    static class MoodViewHolder extends RecyclerView.ViewHolder {
        TextView textMoodEmoji, textMoodName, textDate, textNote;

        public MoodViewHolder(@NonNull View itemView) {
            super(itemView);
            textMoodEmoji = itemView.findViewById(R.id.textMoodEmoji);
            textMoodName = itemView.findViewById(R.id.textMoodName);
            textDate = itemView.findViewById(R.id.textDate);
            textNote = itemView.findViewById(R.id.textNote);
        }
    }
}