package com.example.expensewise.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.expensewise.MainActivity;
import com.example.expensewise.R;

public class NotificationHelper {

    public static final String CHANNEL_ID = "expensewise_channel";
    public static final String CHANNEL_NAME = "ExpenseWise Alerts";
    public static final String CHANNEL_DESC = "Notifications for budget warnings, recurring expense reminders, and summaries.";

    private final Context context;

    public NotificationHelper(Context context) {
        this.context = context.getApplicationContext();
        createNotificationChannel();
    }

    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);
            channel.enableVibration(true);

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private PendingIntent getContentIntent() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }
        return PendingIntent.getActivity(context, 0, intent, flags);
    }

    public void sendBudgetWarningNotification(String category, double spent, double budget) {
        if (!hasNotificationPermission()) return;

        int percentage = (int) ((spent / budget) * 100);
        String title = "⚠️ Budget Warning: " + category;
        String content = "You have used " + percentage + "% of your monthly " + category + " budget.";

        showNotification(1001, title, content);
    }

    public void sendBudgetExceededNotification(String category, double spent, double budget) {
        if (!hasNotificationPermission()) return;

        String title = "⚠️ Budget Exceeded: " + category;
        String content = "Your " + category + " budget has been exceeded! (Spent: " +
                FinancialUtils.formatCurrency(spent) + " / " + FinancialUtils.formatCurrency(budget) + ")";

        showNotification(1002, title, content);
    }

    public void sendRecurringExpenseReminder(String expenseName, double amount, String dueDate) {
        if (!hasNotificationPermission()) return;

        String title = "Reminder: Recurring Expense Due";
        String content = "Reminder: Your " + expenseName + " payment of " +
                FinancialUtils.formatCurrency(amount) + " is due on " + dueDate + ".";

        showNotification(2001, title, content);
    }

    public void sendMonthlySummaryNotification(double totalIncome, double totalExpense) {
        if (!hasNotificationPermission()) return;

        double savings = totalIncome - totalExpense;
        String title = "Monthly Summary Ready 📊";
        String content = "Your monthly spending summary is ready. Total Expense: " +
                FinancialUtils.formatCurrency(totalExpense) + ", Net Savings: " +
                FinancialUtils.formatCurrency(savings);

        showNotification(3001, title, content);
    }

    public void showNotification(int notificationId, String title, String message) {
        if (!hasNotificationPermission()) return;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_expensewise_logo)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(true)
                .setContentIntent(getContentIntent());

        try {
            NotificationManagerCompat manager = NotificationManagerCompat.from(context);
            manager.notify(notificationId, builder.build());
        } catch (SecurityException ignored) {
        }
    }
}
