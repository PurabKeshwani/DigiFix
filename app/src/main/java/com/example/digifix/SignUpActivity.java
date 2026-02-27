package com.example.digifix;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up); // We will need this XML next!

        // 1. Initialize Firebase & SharedPreferences
        mAuth = FirebaseAuth.getInstance();
        sharedPrefs = getSharedPreferences("DigiFixPrefs", MODE_PRIVATE);

        // UI References (Assuming similar IDs to your Login screen)
        EditText etEmail = findViewById(R.id.etSignUpEmail);
        EditText etPassword = findViewById(R.id.etSignUpPassword);
        EditText etConfirmPassword = findViewById(R.id.etConfirmPassword);
        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);

        // 2. Sign Up Click Listener
        btnCreateAccount.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // Basic Validation
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            // UI Loading State
            btnCreateAccount.setText("Creating Account...");
            btnCreateAccount.setEnabled(false);
            btnCreateAccount.setAlpha(0.7f);

            // 3. Firebase Create User Call
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Success!
                            FirebaseUser user = mAuth.getCurrentUser();

                            // Save the email locally for convenience on the login screen later
                            sharedPrefs.edit().putString("saved_email", email).apply();

                            Toast.makeText(SignUpActivity.this, "Account Created!", Toast.LENGTH_SHORT).show();

                            // Move directly to Dashboard (User is now authenticated)
                            startActivity(new Intent(SignUpActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            // Failure: Show error
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                            Toast.makeText(SignUpActivity.this, errorMsg, Toast.LENGTH_LONG).show();

                            // Reset UI
                            btnCreateAccount.setText("Sign Up");
                            btnCreateAccount.setEnabled(true);
                            btnCreateAccount.setAlpha(1f);
                        }
                    });
        });

        // 4. Back to Login button
        findViewById(R.id.tvBackToLogin).setOnClickListener(v -> {
            // Just finish this activity to drop back to MainActivity
            finish();
        });
    }
}