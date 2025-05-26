package com.example.moodjournal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull; // Required for @NonNull
import androidx.appcompat.app.AppCompatActivity;

// It seems you were using a relocated TextUtils, stick to android.text.TextUtils for consistency
import android.text.TextUtils; // Import standard TextUtils

import com.google.android.gms.tasks.OnCompleteListener; // Required for OnCompleteListener
import com.google.android.gms.tasks.Task; // Required for Task
import com.google.firebase.auth.AuthResult; // Required for AuthResult
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
// import com.google.firebase.auth.UserProfileChangeRequest; // Keep if you plan to set display name immediately


public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = "SignUpActivity";

    private EditText editTextSignUpEmail, editTextSignUpPassword, editTextConfirmPassword;
    private Button buttonSignUp;
    private TextView textViewGoToSignIn;
    private ProgressBar progressBarSignUp;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        editTextSignUpEmail = findViewById(R.id.editTextSignUpEmail);
        editTextSignUpPassword = findViewById(R.id.editTextSignUpPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        textViewGoToSignIn = findViewById(R.id.textViewGoToSignIn);
        progressBarSignUp = findViewById(R.id.progressBarSignUp);

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpUser();
            }
        });

        textViewGoToSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to SignInActivity
                Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                // Clear previous activities from the back stack to prevent user from going back to sign up
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish(); // Close SignUpActivity
            }
        });
    }

    private void signUpUser() {
        String email = editTextSignUpEmail.getText().toString().trim();
        String password = editTextSignUpPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editTextSignUpEmail.setError("Email is required.");
            editTextSignUpEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextSignUpEmail.setError("Enter a valid email address.");
            editTextSignUpEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextSignUpPassword.setError("Password is required.");
            editTextSignUpPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editTextSignUpPassword.setError("Password should be at least 6 characters.");
            editTextSignUpPassword.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            editTextConfirmPassword.setError("Confirm password is required.");
            editTextConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Passwords do not match.");
            editTextConfirmPassword.requestFocus();
            // Optionally clear both password fields
            // editTextSignUpPassword.setText("");
            // editTextConfirmPassword.setText("");
            return;
        }

        progressBarSignUp.setVisibility(View.VISIBLE);
        buttonSignUp.setEnabled(false);
        textViewGoToSignIn.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBarSignUp.setVisibility(View.GONE);
                        buttonSignUp.setEnabled(true);
                        textViewGoToSignIn.setEnabled(true);

                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(SignUpActivity.this, "Registration successful. Welcome!",
                                    Toast.LENGTH_SHORT).show();

                            // Optional: Send verification email
                            /*
                            if (user != null) {
                                user.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d(TAG, "Email sent.");
                                                    Toast.makeText(SignUpActivity.this,
                                                            "Verification email sent to " + user.getEmail(),
                                                            Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Log.e(TAG, "sendEmailVerification", task.getException());
                                                    Toast.makeText(SignUpActivity.this,
                                                            "Failed to send verification email.",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                            */

                            // Navigate to SignInActivity or directly to MainActivity
                            // For now, let's navigate to SignInActivity to let the user sign in
                            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish(); // Close SignUpActivity

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                            // Consider more specific error handling, e.g., if email is already in use.
                        }
                    }
                });
    }
}