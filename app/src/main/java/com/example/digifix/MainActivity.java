package com.example.digifix;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Enter Animation
        View glassCard = findViewById(R.id.glassCard);
        glassCard.setAlpha(0f);
        glassCard.setTranslationY(50f);
        glassCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(200)
                .start();

        // 2. Login Logic
        View btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(v -> {
            // Loading State
            if (v instanceof android.widget.Button) {
                ((android.widget.Button) v).setText("Signing In...");
            }
            v.setEnabled(false);
            v.setAlpha(0.7f);

            // Simulate Network Delay
            new android.os.Handler().postDelayed(() -> {
                startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                finish(); // Close Login
            }, 1000);
        });

        // 3. Sign Up
        findViewById(R.id.tvSignUp).setOnClickListener(v -> {
            android.widget.Toast.makeText(this, "Sign Up Unavailable in Demo", android.widget.Toast.LENGTH_SHORT).show();
        });
        
        // 4. Google Login
        findViewById(R.id.btnGoogle).setOnClickListener(v -> {
             android.widget.Toast.makeText(this, "Google Login Unavailable in Demo", android.widget.Toast.LENGTH_SHORT).show();
        });
    }
}
