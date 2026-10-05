package com.example.expensewise.utils;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FinancialUtils {

    public static String formatCurrency(double amount, String currencySymbol) {
        if (currencySymbol == null || currencySymbol.trim().isEmpty()) {
            currencySymbol = "₹";
        }
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("en", "IN"));
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        String formatted = formatter.format(amount);
        return currencySymbol + " " + formatted;
    }

    public static String formatCurrency(double amount) {
        return formatCurrency(amount, "₹");
    }

    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static String getCurrentDateFormatted() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    public static String getCurrentTimeFormatted() {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date());
    }

    public static double calculateNetBalance(double totalIncome, double totalExpense) {
        return totalIncome - totalExpense;
    }

    public static double calculateBudgetPercentage(double totalSpent, double budgetAmount) {
        if (budgetAmount <= 0) return 0.0;
        double percentage = (totalSpent / budgetAmount) * 100.0;
        return Math.min(percentage, 100.0);
    }

    public static double calculateSavingsProgress(double currentAmount, double targetAmount) {
        if (targetAmount <= 0) return 0.0;
        double percentage = (currentAmount / targetAmount) * 100.0;
        return Math.min(percentage, 100.0);
    }
}
