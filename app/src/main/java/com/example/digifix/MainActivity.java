package com.example.digifix;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

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
            // Schedule the daily briefing (safe to call every time — idempotent)
            scheduleDailyBriefing();
            requestNotificationPermission();
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

                            // ── Schedule Daily Revenue Pulse ─────────────────
                            scheduleDailyBriefing();
                            requestNotificationPermission();

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

    // ═══════════════════════════════════════════════════════════════════════
    //  DAILY REVENUE PULSE — SCHEDULING
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Schedules DailyBriefingWorker to run once every 24 hours, starting
     * at 9:00 AM today (or tomorrow if it's already past 9 AM).
     *
     * Uses KEEP policy — so re-scheduling on every login won't stack duplicates.
     * The worker tag "daily_briefing" ensures exactly one periodic job exists.
     */
    private void scheduleDailyBriefing() {
        // Create the notification channel upfront (safe to call multiple times)
        NotificationHelper.createChannel(getApplicationContext());

        // Calculate delay to next 9 AM
        long initialDelay = calculateDelayToNextNineAM();

        PeriodicWorkRequest dailyRequest =
                new PeriodicWorkRequest.Builder(
                        DailyBriefingWorker.class,
                        24, TimeUnit.HOURS)                  // Repeat every 24 hours
                        .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                        .build();

        // KEEP = don't replace if already scheduled (idempotent)
        WorkManager.getInstance(getApplicationContext())
                .enqueueUniquePeriodicWork(
                        "daily_briefing",
                        ExistingPeriodicWorkPolicy.KEEP,
                        dailyRequest);

        android.util.Log.d("DigiFix", "Daily briefing scheduled. Delay: "
                + (initialDelay / 60_000) + " minutes");
    }

    /**
     * Returns milliseconds until the next 9:00 AM.
     * If current time is before 9 AM → fires today.
     * If current time is after  9 AM → fires tomorrow.
     */
    private long calculateDelayToNextNineAM() {
        Calendar now  = Calendar.getInstance();
        Calendar nine = Calendar.getInstance();
        nine.set(Calendar.HOUR_OF_DAY, 9);
        nine.set(Calendar.MINUTE,      0);
        nine.set(Calendar.SECOND,      0);
        nine.set(Calendar.MILLISECOND, 0);

        // If 9 AM is already past for today, roll to tomorrow
        if (now.after(nine)) {
            nine.add(Calendar.DAY_OF_YEAR, 1);
        }

        return nine.getTimeInMillis() - now.getTimeInMillis();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  NOTIFICATION PERMISSION (Android 13+)
    // ═══════════════════════════════════════════════════════════════════════

    private static final int REQ_NOTIF_PERMISSION = 200;

    /**
     * On Android 13+ (API 33), POST_NOTIFICATIONS is a runtime permission.
     * We request it here on first login so the daily briefing actually appears.
     */
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQ_NOTIF_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_NOTIF_PERMISSION) {
            boolean granted = grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            Toast.makeText(this,
                    granted ? "Daily briefing notifications enabled ✅"
                            : "Notifications blocked — briefings won't appear",
                    Toast.LENGTH_SHORT).show();
        }
    }
}