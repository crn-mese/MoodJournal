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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;

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
                finish(); // Just go back to SignInActivity
            }
        });
    }

    private void signUpUser() {
        String email = editTextSignUpEmail.getText().toString().trim();
        String password = editTextSignUpPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        // Add logging to see what email is being used
        Log.d(TAG, "Attempting to register with email: " + email);

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
            return;
        }

        progressBarSignUp.setVisibility(View.VISIBLE);
        buttonSignUp.setEnabled(false);
        textViewGoToSignIn.setEnabled(false);

        Log.d(TAG, "Starting Firebase registration process...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBarSignUp.setVisibility(View.GONE);
                        buttonSignUp.setEnabled(true);
                        textViewGoToSignIn.setEnabled(true);

                        if (task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(SignUpActivity.this, "Registration successful. Welcome!",
                                    Toast.LENGTH_SHORT).show();

                            // Navigate directly to MainActivity after successful registration
                            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();

                        } else {
                            // Enhanced error handling
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());

                            String errorMessage = "Registration failed";
                            Exception exception = task.getException();

                            if (exception != null) {
                                Log.e(TAG, "Exception type: " + exception.getClass().getSimpleName());
                                Log.e(TAG, "Exception message: " + exception.getMessage());

                                if (exception instanceof FirebaseAuthUserCollisionException) {
                                    errorMessage = "This email is already registered. Please use a different email or try signing in.";
                                    Log.e(TAG, "User collision - email already exists");
                                } else if (exception instanceof FirebaseAuthWeakPasswordException) {
                                    errorMessage = "Password is too weak. Please choose a stronger password.";
                                } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                                    errorMessage = "Invalid email format. Please check your email address.";
                                } else {
                                    errorMessage = "Registration failed: " + exception.getMessage();
                                }
                            }

                            Toast.makeText(SignUpActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
