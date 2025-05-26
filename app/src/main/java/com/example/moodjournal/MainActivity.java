package com.example.moodjournal;

import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {


    private void openHistoryActivity() {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }

    private void openSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}