package com.example.digifix;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupNavigation();
        setupMenuActions();
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

        // Logout
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            Toast.makeText(this, "Logging Out...", Toast.LENGTH_SHORT).show();
            // In real app: clear prefs and go to Login
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

        // Swipe Gestures
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
