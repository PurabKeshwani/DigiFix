package com.example.digifix;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * DailyBriefingWorker — Android WorkManager background task
 *
 * Runs daily at 9 AM. Fetches the following from Supabase:
 *   1. Repairs completed yesterday → count + sum of estimated_price
 *   2. All currently pending/active tickets → count + count of "High" priority
 *
 * Then fires a styled InboxStyle notification via NotificationHelper.
 *
 * Uses only OkHttp (already in the project) — no extra dependencies.
 */
public class DailyBriefingWorker extends Worker {

    private static final String TAG = "DailyBriefingWorker";
    private final OkHttpClient httpClient = new OkHttpClient();

    public DailyBriefingWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "DailyBriefingWorker started");

        try {
            // ── 1. Yesterday's date range ─────────────────────────────────────
            String[] range       = getYesterdayRange();
            String   ydayStart   = range[0]; // e.g. "2026-04-09T00:00:00"
            String   ydayEnd     = range[1]; // e.g. "2026-04-09T23:59:59"

            // ── 2. Fetch yesterday's COMPLETED tickets ────────────────────────
            //    Filter: status=eq.completed AND updated_at between ydayStart and ydayEnd
            //    We use created_at here since updated_at may not exist in all schemas.
            String completedUrl = SupabaseConfig.REST_URL
                    + "/repair_tickets"
                    + "?status=eq.completed"
                    + "&created_at=gte." + ydayStart
                    + "&created_at=lte." + ydayEnd
                    + "&select=id,estimated_price";

            JSONArray completedRows = fetchJsonArray(completedUrl);
            int    completedYday = 0;
            double revenueYday   = 0.0;

            if (completedRows != null) {
                completedYday = completedRows.length();
                for (int i = 0; i < completedRows.length(); i++) {
                    JSONObject row = completedRows.getJSONObject(i);
                    if (row.has("estimated_price") && !row.isNull("estimated_price")) {
                        try {
                            // estimated_price may be a string like "₹15,000" or a number
                            String raw = row.getString("estimated_price");
                            // Strip non-numeric chars except dot
                            String clean = raw.replaceAll("[^0-9.]", "");
                            if (!clean.isEmpty()) {
                                revenueYday += Double.parseDouble(clean);
                            }
                        } catch (NumberFormatException e) {
                            // ignore unparseable price rows
                        }
                    }
                }
            }

            // ── 3. Fetch ALL currently active (non-completed) tickets ─────────
            //    Pending + In Progress combined
            String activeUrl = SupabaseConfig.REST_URL
                    + "/repair_tickets"
                    + "?status=neq.completed"
                    + "&select=id,priority,status";

            JSONArray activeRows = fetchJsonArray(activeUrl);
            int activePending = 0;
            int highPriority  = 0;

            if (activeRows != null) {
                activePending = activeRows.length();
                for (int i = 0; i < activeRows.length(); i++) {
                    JSONObject row = activeRows.getJSONObject(i);
                    if (row.has("priority") && !row.isNull("priority")) {
                        String priority = row.getString("priority");
                        if ("High".equalsIgnoreCase(priority) || "high".equals(priority)) {
                            highPriority++;
                        }
                    }
                }
            }

            Log.d(TAG, String.format("Briefing: completed=%d revenue=%.0f active=%d high=%d",
                    completedYday, revenueYday, activePending, highPriority));

            // ── 4. Fire the notification ──────────────────────────────────────
            NotificationHelper.sendBriefing(
                    getApplicationContext(),
                    completedYday,
                    revenueYday,
                    activePending,
                    highPriority
            );

            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Worker failed: " + e.getMessage(), e);
            return Result.retry(); // WorkManager will retry with exponential back-off
        }
    }

    // ─── Supabase REST Helper ─────────────────────────────────────────────────

    /**
     * Makes a GET request to the given Supabase REST URL and returns
     * the response body parsed as a JSONArray.
     * Returns null on network or parse error (caller decides what to show).
     */
    private JSONArray fetchJsonArray(String url) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("apikey",        SupabaseConfig.ANON_KEY)
                .addHeader("Authorization", "Bearer " + SupabaseConfig.ANON_KEY)
                .addHeader("Accept",        "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                Log.w(TAG, "Supabase response error: " + response.code() + " for " + url);
                return null;
            }
            String body = response.body().string();
            return new JSONArray(body);
        } catch (IOException | org.json.JSONException e) {
            Log.e(TAG, "fetchJsonArray error: " + e.getMessage());
            return null;
        }
    }

    // ─── Date Helpers ─────────────────────────────────────────────────────────

    /**
     * Returns ISO-8601 start and end of yesterday.
     * Example: ["2026-04-09T00:00:00", "2026-04-09T23:59:59"]
     */
    private String[] getYesterdayRange() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);

        // Start of yesterday
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        String start = sdf.format(cal.getTime());

        // End of yesterday
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        String end = sdf.format(cal.getTime());

        return new String[]{start, end};
    }
}
