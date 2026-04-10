package com.example.digifix;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * AddRepairActivity — Experiment 7
 *
 * Demonstrates:
 * 1. GPS Sensor via FusedLocationProviderClient (pickup location)
 * 2. Media Handling — camera capture + gallery picker, displayed via Glide
 * 3. Supabase Storage upload (device photo)
 * 4. Supabase REST API insert (repair_tickets table)
 */
public class AddRepairActivity extends AppCompatActivity {

    // ─── Permission Request Codes ───────────────────────────────────────────
    private static final int REQ_LOCATION_PERMISSION = 101;
    private static final int REQ_CAMERA_PERMISSION   = 102;

    // ─── State ──────────────────────────────────────────────────────────────
    private double  capturedLatitude  = 0.0;
    private double  capturedLongitude = 0.0;
    private boolean locationCaptured  = false;
    private Uri     photoUri          = null;
    private Uri     cameraFileUri     = null;

    // ─── GPS animators (pulse effect while fetching) ─────────────────────────
    private ObjectAnimator gpsPulseX;
    private ObjectAnimator gpsPulseY;

    // ─── GPS ────────────────────────────────────────────────────────────────
    private FusedLocationProviderClient fusedLocationClient;

    // ─── Views ──────────────────────────────────────────────────────────────
    private ImageView ivDevicePhoto;
    private View      photoPlaceholder;
    private TextView  tvGpsStatus;
    private TextView  tvGpsCoords;
    private View      btnCaptureLocation;
    private EditText  etCustomerName;
    private EditText  etCustomerPhone;
    private EditText  etDeviceName;
    private EditText  etIssue;
    private TextView  btnSubmit;
    private ProgressBar progressBar;

    // ─── Animation section roots ─────────────────────────────────────────────
    private View sectionHeader;
    private View sectionPhoto;
    private View sectionGps;
    private View sectionForm;
    private View sectionSubmit;

    // ─── HTTP Client ─────────────────────────────────────────────────────────
    private final OkHttpClient httpClient = new OkHttpClient();

    // ─── Activity Result Launchers ───────────────────────────────────────────

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && cameraFileUri != null) {
                    photoUri = cameraFileUri;
                    displayPhotoWithAnimation(photoUri);
                }
            });

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    photoUri = result.getData().getData();
                    displayPhotoWithAnimation(photoUri);
                }
            });

    // ────────────────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_repair);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        bindViews();
        setupClickListeners();

        // ── ENTRANCE ANIMATION ──────────────────────────────────────────────
        // Staggered slide-up + fade-in for each section
        animateEntrance();
    }

    // ─── View Binding ────────────────────────────────────────────────────────
    private void bindViews() {
        ivDevicePhoto      = findViewById(R.id.ivDevicePhoto);
        photoPlaceholder   = findViewById(R.id.photoPlaceholder);
        tvGpsStatus        = findViewById(R.id.tvGpsStatus);
        tvGpsCoords        = findViewById(R.id.tvGpsCoords);
        btnCaptureLocation = findViewById(R.id.btnCaptureLocation);
        etCustomerName     = findViewById(R.id.etCustomerName);
        etCustomerPhone    = findViewById(R.id.etCustomerPhone);
        etDeviceName       = findViewById(R.id.etDeviceName);
        etIssue            = findViewById(R.id.etIssue);
        btnSubmit          = findViewById(R.id.btnSubmit);
        progressBar        = findViewById(R.id.progressBar);

        sectionHeader = findViewById(R.id.sectionHeader);
        sectionPhoto  = findViewById(R.id.sectionPhoto);
        sectionGps    = findViewById(R.id.sectionGps);
        sectionForm   = findViewById(R.id.sectionForm);
        sectionSubmit = findViewById(R.id.sectionSubmit);
    }

    // ─── Click Listeners ─────────────────────────────────────────────────────
    private void setupClickListeners() {
        // Back — with spring-out animation
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            v.animate().scaleX(0.85f).scaleY(0.85f).setDuration(80)
                    .withEndAction(() -> {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(80).start();
                        finish();
                    }).start();
        });

        // GPS (with press animation)
        btnCaptureLocation.setOnClickListener(v -> {
            animateButtonPress(v);
            captureGpsLocation();
        });

        // Camera
        findViewById(R.id.btnCamera).setOnClickListener(v -> {
            animateButtonPress(v);
            openCamera();
        });

        // Gallery
        findViewById(R.id.btnGallery).setOnClickListener(v -> {
            animateButtonPress(v);
            openGallery();
        });

        // Submit
        btnSubmit.setOnClickListener(v -> {
            animateButtonPress(v);
            submitRepairTicket();
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  ANIMATIONS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Staggered slide-up + fade-in entrance for all 5 sections.
     * Each card slides up from 90dp below its final position with a 120ms delay.
     */
    private void animateEntrance() {
        View[] sections = {sectionHeader, sectionPhoto, sectionGps, sectionForm, sectionSubmit};
        int[]  delays   = {0, 120, 240, 360, 480};
        float  startY   = 90f;

        for (int i = 0; i < sections.length; i++) {
            if (sections[i] == null) continue;
            View v = sections[i];
            v.setAlpha(0f);
            v.setTranslationY(startY);
            v.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(450)
                    .setStartDelay(delays[i])
                    .setInterpolator(new DecelerateInterpolator(2f))
                    .start();
        }
    }

    /**
     * Quick press-down / spring-back effect on any button tap.
     */
    private void animateButtonPress(View v) {
        v.animate()
                .scaleX(0.93f).scaleY(0.93f)
                .setDuration(80)
                .withEndAction(() ->
                        v.animate()
                                .scaleX(1f).scaleY(1f)
                                .setDuration(200)
                                .setInterpolator(new OvershootInterpolator(3f))
                                .start())
                .start();
    }

    /**
     * Pulsing scale animation on the GPS button while location is being fetched.
     * Indicates to the user that the app is working.
     */
    private void startGpsPulse() {
        gpsPulseX = ObjectAnimator.ofFloat(btnCaptureLocation, "scaleX", 1f, 1.05f, 1f);
        gpsPulseY = ObjectAnimator.ofFloat(btnCaptureLocation, "scaleY", 1f, 1.05f, 1f);
        gpsPulseX.setDuration(700);
        gpsPulseY.setDuration(700);
        gpsPulseX.setRepeatCount(ObjectAnimator.INFINITE);
        gpsPulseY.setRepeatCount(ObjectAnimator.INFINITE);
        gpsPulseX.setInterpolator(new AccelerateDecelerateInterpolator());
        gpsPulseY.setInterpolator(new AccelerateDecelerateInterpolator());
        gpsPulseX.start();
        gpsPulseY.start();
    }

    /**
     * Stops the GPS pulse and snaps the button back to normal scale.
     */
    private void stopGpsPulse() {
        if (gpsPulseX != null) gpsPulseX.cancel();
        if (gpsPulseY != null) gpsPulseY.cancel();
        btnCaptureLocation.setScaleX(1f);
        btnCaptureLocation.setScaleY(1f);
    }

    /**
     * When the GPS is successfully captured, animate the inner status card
     * with a bounce scale to draw attention to the coordinates.
     */
    private void animateGpsSuccess(View statusCard) {
        statusCard.setScaleX(0.9f);
        statusCard.setScaleY(0.9f);
        statusCard.animate()
                .scaleX(1f).scaleY(1f)
                .setDuration(400)
                .setInterpolator(new OvershootInterpolator(2f))
                .start();
    }

    /**
     * Photo reveal — fade-in + scale-up with overshoot when photo loads.
     */
    private void displayPhotoWithAnimation(Uri uri) {
        photoPlaceholder.setVisibility(View.GONE);
        ivDevicePhoto.setAlpha(0f);
        ivDevicePhoto.setScaleX(0.85f);
        ivDevicePhoto.setScaleY(0.85f);

        Glide.with(this).load(uri).centerCrop().into(ivDevicePhoto);

        ivDevicePhoto.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(400)
                .setInterpolator(new OvershootInterpolator(1.5f))
                .start();

        // Also pop the photo card slightly to acknowledge the selection
        sectionPhoto.animate()
                .scaleX(1.02f).scaleY(1.02f)
                .setDuration(150)
                .withEndAction(() ->
                        sectionPhoto.animate()
                                .scaleX(1f).scaleY(1f)
                                .setDuration(200)
                                .setInterpolator(new OvershootInterpolator())
                                .start())
                .start();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  EXPERIMENT 1 — GPS SENSOR
    // ═══════════════════════════════════════════════════════════════════════

    private void captureGpsLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                 Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQ_LOCATION_PERMISSION);
            return;
        }

        tvGpsStatus.setText("📡 Acquiring GPS signal...");
        tvGpsStatus.setTextColor(0xCCFFFFFF);
        startGpsPulse();

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    stopGpsPulse();
                    if (location != null) {
                        capturedLatitude  = location.getLatitude();
                        capturedLongitude = location.getLongitude();
                        locationCaptured  = true;

                        tvGpsStatus.setText("✅ Location captured");
                        tvGpsStatus.setTextColor(0xFF4CAF50); // green
                        tvGpsCoords.setText(
                                String.format(Locale.US, "%.6f,  %.6f",
                                        capturedLatitude, capturedLongitude));

                        // Bounce the status card to celebrate
                        View statusCard = sectionGps.findViewWithTag("gpsStatusCard");
                        animateGpsSuccess(sectionGps);

                    } else {
                        tvGpsStatus.setText("⚠️  Could not get location");
                        tvGpsStatus.setTextColor(0xFFFF9800); // orange warning
                    }
                })
                .addOnFailureListener(e -> {
                    stopGpsPulse();
                    tvGpsStatus.setText("❌  GPS error: " + e.getMessage());
                    tvGpsStatus.setTextColor(0xFFEF5350);
                });
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  EXPERIMENT 2 — MEDIA HANDLING: CAMERA
    // ═══════════════════════════════════════════════════════════════════════

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQ_CAMERA_PERMISSION);
            return;
        }

        File photoFile = createImageFile();
        if (photoFile == null) {
            Toast.makeText(this, "Could not create image file", Toast.LENGTH_SHORT).show();
            return;
        }

        cameraFileUri = FileProvider.getUriForFile(
                this,
                "com.example.digifix.fileprovider",
                photoFile);

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraFileUri);
        cameraLauncher.launch(cameraIntent);
    }

    private File createImageFile() {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            File storageDir  = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            return File.createTempFile("DEVICE_" + timestamp, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  EXPERIMENT 2 — MEDIA HANDLING: GALLERY
    // ═══════════════════════════════════════════════════════════════════════

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        galleryLauncher.launch(galleryIntent);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  SUBMIT — Upload to Supabase Storage + Insert to DB
    // ═══════════════════════════════════════════════════════════════════════

    private void submitRepairTicket() {
        String customerName  = etCustomerName.getText()  != null ? etCustomerName.getText().toString().trim()  : "";
        String customerPhone = etCustomerPhone.getText() != null ? etCustomerPhone.getText().toString().trim() : "";
        String deviceName    = etDeviceName.getText()    != null ? etDeviceName.getText().toString().trim()    : "";
        String issue         = etIssue.getText()         != null ? etIssue.getText().toString().trim()         : "";

        if (customerName.isEmpty() || customerPhone.isEmpty() || deviceName.isEmpty()) {
            // Shake the form card to indicate validation error
            shakeView(sectionForm);
            Toast.makeText(this, "Please fill in Customer Name, Phone and Device", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        if (photoUri != null) {
            uploadPhotoThenSubmit(customerName, customerPhone, deviceName, issue);
        } else {
            insertTicketToDatabase(customerName, customerPhone, deviceName, issue, null);
        }
    }

    /**
     * Horizontal shake animation for validation errors — mimics iOS shake.
     */
    private void shakeView(View v) {
        ObjectAnimator shaker = ObjectAnimator.ofFloat(v, "translationX",
                0f, -18f, 18f, -14f, 14f, -8f, 8f, 0f);
        shaker.setDuration(500);
        shaker.start();
    }

    private void uploadPhotoThenSubmit(String name, String phone, String device, String issue) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(photoUri);
            if (inputStream == null) {
                Toast.makeText(this, "Cannot read photo", Toast.LENGTH_SHORT).show();
                setLoading(false);
                return;
            }
            byte[] imageBytes = inputStream.readAllBytes();
            inputStream.close();

            String fileName  = "ticket_" + System.currentTimeMillis() + ".jpg";
            String uploadUrl = SupabaseConfig.STORAGE_URL + "/" + fileName;

            Request uploadRequest = new Request.Builder()
                    .url(uploadUrl)
                    .post(RequestBody.create(imageBytes, MediaType.parse("image/jpeg")))
                    .addHeader("apikey", SupabaseConfig.ANON_KEY)
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.ANON_KEY)
                    .addHeader("Content-Type", "image/jpeg")
                    .addHeader("x-upsert", "true")
                    .build();

            httpClient.newCall(uploadRequest).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        setLoading(false);
                        Toast.makeText(AddRepairActivity.this,
                                "Photo upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    if (response.isSuccessful()) {
                        String photoUrl = SupabaseConfig.PROJECT_URL
                                + "/storage/v1/object/public/"
                                + SupabaseConfig.BUCKET_NAME + "/" + fileName;
                        insertTicketToDatabase(name, phone, device, issue, photoUrl);
                    } else {
                        runOnUiThread(() -> {
                            setLoading(false);
                            Toast.makeText(AddRepairActivity.this,
                                    "Storage error " + response.code(), Toast.LENGTH_LONG).show();
                        });
                    }
                }
            });

        } catch (IOException e) {
            setLoading(false);
            Toast.makeText(this, "Error reading photo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void insertTicketToDatabase(String name, String phone,
                                         String device, String issue, String photoUrl) {
        try {
            JSONObject body = new JSONObject();
            body.put("customer_name",     name);
            body.put("customer_phone",    phone);
            body.put("device_name",       device);
            body.put("issue_description", issue);
            body.put("status",            "pending");
            if (locationCaptured) {
                body.put("latitude",  capturedLatitude);
                body.put("longitude", capturedLongitude);
            }
            if (photoUrl != null) {
                body.put("photo_url", photoUrl);
            }

            Request dbRequest = new Request.Builder()
                    .url(SupabaseConfig.REST_URL + "/repair_tickets")
                    .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                    .addHeader("apikey",        SupabaseConfig.ANON_KEY)
                    .addHeader("Authorization", "Bearer " + SupabaseConfig.ANON_KEY)
                    .addHeader("Content-Type",  "application/json")
                    .addHeader("Prefer",        "return=minimal")
                    .build();

            httpClient.newCall(dbRequest).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        setLoading(false);
                        Toast.makeText(AddRepairActivity.this,
                                "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    runOnUiThread(() -> {
                        setLoading(false);
                        if (response.isSuccessful() || response.code() == 201) {
                            // Celebration scale animation on submit button before finishing
                            btnSubmit.animate()
                                    .scaleX(1.08f).scaleY(1.08f)
                                    .setDuration(150)
                                    .withEndAction(() -> {
                                        btnSubmit.animate().scaleX(1f).scaleY(1f)
                                                .setDuration(200).start();
                                        Toast.makeText(AddRepairActivity.this,
                                                "✅  Ticket submitted!", Toast.LENGTH_SHORT).show();
                                        // Slide out the whole screen to the left before finishing
                                        animateExitAndFinish();
                                    }).start();
                        } else {
                            Toast.makeText(AddRepairActivity.this,
                                    "DB error: " + response.code(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });

        } catch (Exception e) {
            setLoading(false);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Slides the entire screen down and fades out before calling finish().
     */
    private void animateExitAndFinish() {
        View root = findViewById(android.R.id.content);
        root.animate()
                .alpha(0f)
                .translationY(60f)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(this::finish)
                .start();
    }

    // ─── Permission Handling ─────────────────────────────────────────────────

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureGpsLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQ_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSubmit.setEnabled(!loading);
        btnSubmit.setText(loading ? "SUBMITTING..." : "SUBMIT TICKET");
        if (loading) {
            btnSubmit.setAlpha(0.6f);
        } else {
            btnSubmit.animate().alpha(1f).setDuration(200).start();
        }
    }
}
