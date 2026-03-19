package com.example.digifix;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

// Don't forget these Firebase imports!
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupUserProfile();
        setupNavigation();
        setupMenuActions();
    }

    private void setupUserProfile() {
        // Fetch the currently logged-in user from Firebase
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userEmail = currentUser.getEmail();

            // Find the TextView and update it with the real email
            TextView tvUserEmail = findViewById(R.id.tvUserEmail);
            if (tvUserEmail != null && userEmail != null) {
                tvUserEmail.setText(userEmail);
            }
        }
    }

    private void setupMenuActions() {
        // Shop Profile
        findViewById(R.id.btnShopProfile).setOnClickListener(v ->
                Toast.makeText(this, "Shop Profile Clicked", Toast.LENGTH_SHORT).show()
        );

        // Security
        findViewById(R.id.btnSecurity).setOnClickListener(v ->
                Toast.makeText(this, "Security Settings Clicked", Toast.LENGTH_SHORT).show()
        );

        // Notifications
        findViewById(R.id.btnNotifications).setOnClickListener(v ->
                Toast.makeText(this, "Notification Settings Clicked", Toast.LENGTH_SHORT).show()
        );

        // Dark Mode
        findViewById(R.id.btnDarkMode).setOnClickListener(v ->
                Toast.makeText(this, "Toggled Dark Mode", Toast.LENGTH_SHORT).show()
        );

        // Logout Implementation
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            // 1. Terminate the secure Firebase session
            FirebaseAuth.getInstance().signOut();

            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            // 2. Redirect to Login and Clear Activity History (Back Stack)
            Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // 3. Close SettingsActivity
            finish();
        });
    }

    private void setupNavigation() {
        // 1. Home
        findViewById(R.id.btnNavHome).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, DashboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }
        });

        // 2. Repairs
        findViewById(R.id.btnNavRepairs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, RepairsActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }
        });

        // 3. Clients
        findViewById(R.id.btnNavClients).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingsActivity.this, ClientsActivity.class));
                overridePendingTransition(0, 0);
                finish(); // Standardize: Finish Settings when leaving
            }
        });

        // 4. Settings (Already Here)
        findViewById(R.id.btnNavSettings).setOnClickListener(v -> {});

        // FAB
        findViewById(R.id.fabMain).setOnClickListener(v -> {
            Toast.makeText(this, "Quick Add (Placeholder)", Toast.LENGTH_SHORT).show();
        });

        // Swipe Gestures (Assumes you have your OnSwipeTouchListener class in the project)
        findViewById(android.R.id.content).setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeRight() {
                // Go to Clients
                startActivity(new Intent(SettingsActivity.this, ClientsActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }
        });
    }
}