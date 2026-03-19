package com.example.digifix;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        setupNavigation();
        setupHeaderButtons();
    }

    private void setupHeaderButtons() {
        // Profile
        findViewById(R.id.btnProfile).setOnClickListener(v -> {
            android.widget.Toast.makeText(this, "Profile Clicked", android.widget.Toast.LENGTH_SHORT).show();
            // Optional: Start Profile Activity or Settings
             startActivity(new android.content.Intent(this, SettingsActivity.class));
        });

        // Notifications
        findViewById(R.id.btnNotif).setOnClickListener(v -> {
            android.widget.Toast.makeText(this, "No New Notifications", android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    private void setupNavigation() {
        // 1. Home - Already here
        findViewById(R.id.btnNavHome).setOnClickListener(v -> {
            // Do nothing or scroll to top
        });

        // 2. Repairs
        findViewById(R.id.btnNavRepairs).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, RepairsActivity.class));
            overridePendingTransition(0, 0);
        });

        // 3. Clients
        findViewById(R.id.btnNavClients).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, ClientsActivity.class));
            overridePendingTransition(0, 0);
        });

        // 4. Settings
        findViewById(R.id.btnNavSettings).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, SettingsActivity.class));
            overridePendingTransition(0, 0);
        });

        // FAB
        findViewById(R.id.fabMain).setOnClickListener(v -> showQuickAddSheet());

        // Swipe Gestures
        findViewById(android.R.id.content).setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() {
                // Go to Repairs
                startActivity(new android.content.Intent(DashboardActivity.this, RepairsActivity.class));
                overridePendingTransition(0, 0);
            }
        });
    }

    private void showQuickAddSheet() {
        android.widget.Toast.makeText(this, "Quick Add (Placeholder)", android.widget.Toast.LENGTH_SHORT).show();
    }
}
