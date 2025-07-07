package com.example.moodjournal;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity"; // For logging
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String selectedMood = "";
    private String currentPhotoPath = "";
    private EditText noteEditText;
    private TextView dateTextView;
    private Button[] moodButtons;
    private ImageView photoImageView;
    private LinearLayout photoPlaceholder;
    private ImageButton removePhotoButton;
    private Button cameraButton, galleryButton;

    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;

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
        setupActivityLaunchers();
        setupDateDisplay();
        setupMoodButtons();
        setupPhotoButtons();
        setupSaveButton();
        setupNavigationButtons();
    }

    private void initializeViews() {
        dateTextView = findViewById(R.id.dateTextView);
        noteEditText = findViewById(R.id.noteEditText);
        photoImageView = findViewById(R.id.photoImageView);
        photoPlaceholder = findViewById(R.id.photoPlaceholder);
        removePhotoButton = findViewById(R.id.removePhotoButton);
        cameraButton = findViewById(R.id.cameraButton);
        galleryButton = findViewById(R.id.galleryButton);

        moodButtons = new Button[]{
                findViewById(R.id.happyButton),
                findViewById(R.id.sadButton),
                findViewById(R.id.angryButton),
                findViewById(R.id.anxiousButton),
                findViewById(R.id.calmButton)
        };
    }

    private void setupActivityLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            displayPhoto();
                        }
                    }
                }
        );

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Uri selectedImageUri = result.getData().getData();
                            if (selectedImageUri != null) {
                                currentPhotoPath = saveImageFromUri(selectedImageUri);
                                displayPhoto();
                            }
                        }
                    }
                }
        );
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

    private void setupPhotoButtons() {
        photoPlaceholder.setOnClickListener(v -> showPhotoOptions());
        cameraButton.setOnClickListener(v -> openCamera());
        galleryButton.setOnClickListener(v -> openGallery());
        removePhotoButton.setOnClickListener(v -> removePhoto());
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

    private void showPhotoOptions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Photo")
                .setItems(new String[]{"Take Photo", "Choose from Gallery"}, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else {
                        openGallery();
                    }
                });
        builder.show();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = createImageFile();
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.moodjournal.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                cameraLauncher.launch(takePictureIntent);
            }
        }
    }

    private void openGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            return;
        }

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "MOOD_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File image = File.createTempFile(imageFileName, ".jpg", storageDir);
            currentPhotoPath = image.getAbsolutePath();
            return image;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private String saveImageFromUri(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "MOOD_" + timeStamp + ".jpg";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File imageFile = new File(storageDir, imageFileName);

            FileOutputStream out = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void displayPhoto() {
        if (!currentPhotoPath.isEmpty()) {
            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
            if (bitmap != null) {
                photoImageView.setImageBitmap(bitmap);
                photoImageView.setVisibility(View.VISIBLE);
                photoPlaceholder.setVisibility(View.GONE);
                removePhotoButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private void removePhoto() {
        currentPhotoPath = "";
        photoImageView.setVisibility(View.GONE);
        photoPlaceholder.setVisibility(View.VISIBLE);
        removePhotoButton.setVisibility(View.GONE);
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
        removePhoto();
        resetMoodButtonsUI();
    }

    private void setupNavigationButtons() {
        Button historyBtn = findViewById(R.id.historyButton);
        Button settingsBtn = findViewById(R.id.settingsButton);
        Button logoutBtn = findViewById(R.id.logoutButton);

        historyBtn.setOnClickListener(v -> {
            // TODO: Implement HistoryActivity and navigate to it
             Intent intent = new Intent(MainActivity.this, EnhancedHistoryActivity.class);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(this, "Storage permission is required to access gallery", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
