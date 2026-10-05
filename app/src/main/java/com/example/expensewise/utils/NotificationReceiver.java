package com.example.expensewise.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String ACTION_RECURRING_REMINDER = "com.example.expensewise.ACTION_RECURRING_REMINDER";
    public static final String ACTION_MONTHLY_SUMMARY = "com.example.expensewise.ACTION_MONTHLY_SUMMARY";

    public static final String EXTRA_EXPENSE_NAME = "extra_expense_name";
    public static final String EXTRA_EXPENSE_AMOUNT = "extra_expense_amount";
    public static final String EXTRA_DUE_DATE = "extra_due_date";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) return;

        String action = intent.getAction();
        NotificationHelper notificationHelper = new NotificationHelper(context);

        if (ACTION_RECURRING_REMINDER.equals(action)) {
            String name = intent.getStringExtra(EXTRA_EXPENSE_NAME);
            double amount = intent.getDoubleExtra(EXTRA_EXPENSE_AMOUNT, 0.0);
            String dueDate = intent.getStringExtra(EXTRA_DUE_DATE);

            if (name != null) {
                notificationHelper.sendRecurringExpenseReminder(name, amount, dueDate != null ? dueDate : "Soon");
            }
        } else if (ACTION_MONTHLY_SUMMARY.equals(action)) {
            double income = intent.getDoubleExtra("extra_income", 0.0);
            double expense = intent.getDoubleExtra("extra_expense", 0.0);
            notificationHelper.sendMonthlySummaryNotification(income, expense);
        }
    }
}
