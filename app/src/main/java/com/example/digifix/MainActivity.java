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

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Initialize Firebase and SharedPreferences
        mAuth = FirebaseAuth.getInstance();
        sharedPrefs = getSharedPreferences("DigiFixPrefs", MODE_PRIVATE);

        // 2. Check if user is ALREADY logged in (Firebase handles the token!)
        if (mAuth.getCurrentUser() != null) {
            // Skip login and go straight to Dashboard
            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
            finish();
            return;
        }

        // 3. Enter Animation
        View glassCard = findViewById(R.id.glassCard);
        glassCard.setAlpha(0f);
        glassCard.setTranslationY(50f);
        glassCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(200)
                .start();

        // UI References
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);

        // 4. Pre-fill the email using SharedPreferences if it exists
        String savedEmail = sharedPrefs.getString("saved_email", "");
        etEmail.setText(savedEmail);

        // 5. Login Button Click
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            // UI Loading State
            btnLogin.setText("Signing In...");
            btnLogin.setEnabled(false);
            btnLogin.setAlpha(0.7f);

            // Firebase Authentication Call
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Success! Save the email locally for convenience
                            sharedPrefs.edit().putString("saved_email", email).apply();

                            // Move to Dashboard
                            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            // Failure: Show the error message
                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Authentication failed";
                            Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG).show();

                            // Reset UI
                            btnLogin.setText("Login  ➝");
                            btnLogin.setEnabled(true);
                            btnLogin.setAlpha(1f);
                        }
                    });
        });

        // 6. Navigate to Sign Up
        findViewById(R.id.tvSignUp).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SignUpActivity.class));
        });

        // 7. Google Login (Placeholder)
        findViewById(R.id.btnGoogle).setOnClickListener(v -> {
            Toast.makeText(this, "Google Login coming soon", Toast.LENGTH_SHORT).show();
        });
    }
}