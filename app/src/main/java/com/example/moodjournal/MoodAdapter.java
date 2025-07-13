package com.example.moodjournal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.List;

// Step 1: Add delete listener interface
interface OnMoodDeleteListener {
    void onDelete(MoodEntry entry);
}

public class MoodAdapter extends RecyclerView.Adapter<MoodAdapter.MoodViewHolder> {
    private Context context;
    private List<MoodEntry> moodEntries;

    // Step 2: Delete listener
    private OnMoodDeleteListener deleteListener;

    public MoodAdapter(Context context, List<MoodEntry> moodEntries) {
        this.context = context;
        this.moodEntries = moodEntries;
    }

    public void setOnMoodDeleteListener(OnMoodDeleteListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public MoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_mood_entry, parent, false);
        return new MoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoodViewHolder holder, int position) {
        MoodEntry entry = moodEntries.get(position);

        // Set emoji and mood name
        holder.textMoodEmoji.setText(entry.getEmoji());
        holder.textMoodName.setText(entry.getMood());

        // Set date/timestamp
        holder.textDate.setText(entry.getDate());

        // Handle note visibility
        if (entry.getNote() != null && !entry.getNote().trim().isEmpty()) {
            holder.textNote.setText(entry.getNote());
            holder.textNote.setVisibility(View.VISIBLE);
        } else {
            holder.textNote.setVisibility(View.GONE);
        }

        // Set card background color based on mood
        String backgroundColor = getMoodBackgroundColor(entry.getMood());
        if (backgroundColor != null) {
            holder.cardView.setCardBackgroundColor(Color.parseColor(backgroundColor));
        } else {
            holder.cardView.setCardBackgroundColor(Color.WHITE);
        }

        // Handle photo display
        if (entry.hasPhoto()) {
            File photoFile = new File(entry.getPhotoPath());
            if (photoFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(entry.getPhotoPath());
                if (bitmap != null) {
                    holder.photoImageView.setImageBitmap(bitmap);
                    holder.photoImageView.setVisibility(View.VISIBLE);
                } else {
                    holder.photoImageView.setVisibility(View.GONE);
                }
            } else {
                holder.photoImageView.setVisibility(View.GONE);
            }
        } else {
            holder.photoImageView.setVisibility(View.GONE);
        }

        // Step 3: Handle delete click
        holder.iconDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDelete(entry);
            }
        });
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
            case "happy":
                return "#FFF1B6";
            case "sad":
                return "#D4E2FC";
            case "angry":
                return "#F5B6B6";
            case "anxious":
                return "#E8DFF5";
            case "tired":
                return "#DADADA";
            case "calm":
                return "#B8E2C8";
            case "stressed":
                return "#FFC8A2";
            case "excited":
                return "#FFD6E0";
            case "lonely":
                return "#C3C9E9";
            case "neutral":
                return "#F1F1F1";
            default:
                return "#FFFFFF";
        }
    }

    static class MoodViewHolder extends RecyclerView.ViewHolder {
        TextView textMoodEmoji, textMoodName, textDate, textNote;
        ImageView photoImageView, iconDelete;
        CardView cardView;

        public MoodViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            textMoodEmoji = itemView.findViewById(R.id.textMoodEmoji);
            textMoodName = itemView.findViewById(R.id.textMoodName);
            textDate = itemView.findViewById(R.id.textDate);
            textNote = itemView.findViewById(R.id.textNote);
            photoImageView = itemView.findViewById(R.id.photoImageView);
            iconDelete = itemView.findViewById(R.id.iconDelete); // ✅ ADD THIS
        }
    }
}