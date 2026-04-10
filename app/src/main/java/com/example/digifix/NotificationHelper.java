package com.example.digifix;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

/**
 * NotificationHelper — Daily Revenue Pulse
 *
 * Manages the notification channel and constructs the styled
 * morning briefing notification with an InboxStyle expanded view.
 */
public class NotificationHelper {

    // Channel
    public static final String CHANNEL_ID   = "digifix_daily_pulse";
    public static final String CHANNEL_NAME = "Daily Revenue Pulse";
    public static final int    NOTIF_ID     = 1001;

    /**
     * Creates the notification channel on Android 8+.
     * Safe to call multiple times — system ignores duplicates.
     */
    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Your daily shop briefing at 9 AM");
            channel.enableLights(true);
            channel.setLightColor(Color.parseColor("#7C4DFF")); // DigiFix purple
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 250, 150, 250});

            NotificationManager manager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    /**
     * Builds and returns the morning briefing notification.
     *
     * @param context          application context
     * @param completedYday    number of repairs completed yesterday
     * @param revenueYday      total estimated revenue from yesterday's completions (₹)
     * @param activePending    number of currently active/pending tickets today
     * @param highPriority     number of high-priority tickets currently open
     */
    public static Notification buildBriefingNotification(
            Context context,
            int completedYday,
            double revenueYday,
            int activePending,
            int highPriority) {

        // Deep-link tap → Dashboard
        Intent tapIntent = new Intent(context, DashboardActivity.class);
        tapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent tapPendingIntent = PendingIntent.getActivity(
                context, 0, tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Action button → Repairs list
        Intent repairIntent = new Intent(context, RepairsActivity.class);
        repairIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent repairPendingIntent = PendingIntent.getActivity(
                context, 1, repairIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Format revenue nicely
        String revenueStr = formatRevenue(revenueYday);

        // Build InboxStyle — two-line collapsed summary + expanded detail rows
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle()
                .setBigContentTitle("☀️ Good morning! Here's your shop pulse")
                .addLine("🔧  Yesterday: " + completedYday + " repair(s) completed")
                .addLine("💰  Revenue: " + revenueStr + " collected")
                .addLine("📋  Today: " + activePending + " active ticket(s)")
                .addLine(highPriority > 0
                        ? "🔴  " + highPriority + " high-priority ticket(s) need attention!"
                        : "✅  No high-priority tickets — looking good!")
                .setSummaryText("DigiFix • Daily Pulse");

        // Collapsed ticker text
        String tickerText = completedYday > 0
                ? completedYday + " repairs done • " + revenueStr + " earned yesterday"
                : activePending + " active tickets today";

        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_repairs)          // reuse existing icon
                .setContentTitle("☀️ Good morning!")
                .setContentText(tickerText)
                .setStyle(inboxStyle)
                .setColor(Color.parseColor("#7C4DFF"))         // DigiFix purple accent
                .setColorized(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(tapPendingIntent)
                .setAutoCancel(true)
                .addAction(
                        R.drawable.ic_repairs,
                        "VIEW REPAIRS",
                        repairPendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .build();
    }

    /** Fires the notification immediately. Worker calls this. */
    public static void sendBriefing(Context context, int completedYday,
                                    double revenueYday, int activePending, int highPriority) {
        createChannel(context);
        Notification notification = buildBriefingNotification(
                context, completedYday, revenueYday, activePending, highPriority);
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIF_ID, notification);
        }
    }

    // ─── Formatting Helpers ──────────────────────────────────────────────────

    /** Formats a double revenue value as ₹X,XXX or ₹X.XL etc. */
    private static String formatRevenue(double amount) {
        if (amount >= 100_000) {
            return String.format("₹%.1fL", amount / 100_000);
        } else if (amount >= 1_000) {
            // e.g. ₹15,500
            return "₹" + String.format("%,.0f", amount);
        } else {
            return String.format("₹%.0f", amount);
        }
    }
}
