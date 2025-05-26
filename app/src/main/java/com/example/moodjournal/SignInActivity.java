package com.example.moodjournal;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.util.TextUtils;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";

    private EditText editTextEmail, editTextPassword;
    private Button buttonSignIn;
    private TextView textViewGoToSignUp;
    private ProgressBar progressBarSignIn;

    private FirebaseAuth mAuth; // Firebase Auth instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance(); // Initialize Firebase Auth

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSignIn = findViewById(R.id.buttonSignIn);
        textViewGoToSignUp = findViewById(R.id.textViewGoToSignUp);
        progressBarSignIn = findViewById(R.id.progressBarSignIn);

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // 'v' will be of type android.view.View
                signInUser();
            }
        });

        textViewGoToSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // 'v' will be of type android.view.View
                Intent intent = new Intent(SignInActivity.this.peekAvailableContext(), SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    // Corrected and completed signInUser method
    private void signInUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("Email is required.");
            editTextEmail.requestFocus();
            return;
        }

        // You might want to add more robust email validation here
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Enter a valid email address.");
            editTextEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Password is required.");
            editTextPassword.requestFocus();
            return;
        }

        // Consider password length/complexity requirements if needed
        // if (password.length() < 6) {
        //     editTextPassword.setError("Password should be at least 6 characters.");
        //     editTextPassword.requestFocus();
        //     return;
        // }

        progressBarSignIn.setVisibility(View.VISIBLE);
        buttonSignIn.setEnabled(false); // Disable button during sign-in attempt

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    progressBarSignIn.setVisibility(View.GONE);
                    buttonSignIn.setEnabled(true); // Re-enable button
                    if (task.isSuccessful()) {
                        // Sign in success
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(SignInActivity.this, "Authentication successful. Welcome " + (user != null ? user.getEmail() : ""),
                                Toast.LENGTH_SHORT).show();

                        // Navigate to your main activity or dashboard
                        // Intent intent = new Intent(SignInActivity.this, MainActivity.class); // Replace MainActivity with your target
                        // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        // startActivity(intent);
                        // finish(); // Close SignInActivity so user can't go back to it with the back button

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.d(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(SignInActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        // You might want to provide more specific error messages based on task.getException()
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is already signed in, navigate to main activity
            // This prevents the user from seeing the sign-in screen if they are already logged in.
            // Intent intent = new Intent(SignInActivity.this, MainActivity.class); // Replace MainActivity
            // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // startActivity(intent);
            // finish();
            Log.d(TAG, "User " + currentUser.getEmail() + " already signed in.");
            // You could show a quick toast or directly navigate. For now, just logging.
        }
    }
}
