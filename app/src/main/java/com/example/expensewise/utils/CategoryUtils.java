package com.example.expensewise.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.expensewise.R;

public class CategoryUtils {

    /**
     * Converts category name to 1 or 2 uppercase initials ONLY.
     * Examples:
     * - "Bank Transfer" -> "BT"
     * - "Food" / "Food & Dining" -> "F"
     * - "Transport" -> "T"
     * - "Entertainment" -> "E"
     * - "Healthcare" / "Health" -> "H"
     * - "Salary" -> "S"
     * - "Bills" -> "B"
     * - "Shopping" -> "SH"
     * - "Freelance" -> "FL"
     * - "Rent" -> "R"
     * - "Education" -> "ED"
     * - "Other" -> "O"
     */
    public static String getCategoryInitials(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return "OTH";
        }

        String clean = categoryName.trim();
        String lower = clean.toLowerCase();

        // Specific requirements mapping
        if (lower.contains("bank") || lower.contains("transfer")) {
            return "BNK";
        }
        if (lower.contains("salary")) {
            return "SAL";
        }
        if (lower.contains("food") || lower.contains("dining")) {
            return "FD";
        }
        if (lower.contains("transport")) {
            return "TRN";
        }
        if (lower.contains("shop")) {
            return "SHP";
        }
        if (lower.contains("bill") || lower.contains("utility")) {
            return "BIL";
        }
        if (lower.contains("entertainment")) {
            return "ENT";
        }
        if (lower.contains("education") || lower.contains("school") || lower.contains("course")) {
            return "EDU";
        }
        if (lower.contains("health") || lower.contains("medical")) {
            return "HLT";
        }
        if (lower.contains("rent")) {
            return "RNT";
        }
        if (lower.contains("travel")) {
            return "TRV";
        }
        if (lower.contains("freelance")) {
            return "FLC";
        }
        if (lower.contains("business")) {
            return "BUS";
        }
        if (lower.contains("allowance")) {
            return "ALW";
        }
        if (lower.contains("interest")) {
            return "INT";
        }
        if (lower.contains("gift")) {
            return "GFT";
        }
        if (lower.contains("other")) {
            return "OTH";
        }

        // Default fallback: first 2-3 uppercase letters of the category string
        String letters = clean.replaceAll("[^a-zA-Z]", "").toUpperCase();
        if (letters.length() >= 3) {
            return letters.substring(0, 3);
        } else if (!letters.isEmpty()) {
            return letters;
        }

        return "OTH";
    }

    /**
     * Maps category name to vector drawable resource.
     */
    public static int getCategoryIconResource(String categoryName) {
        if (TextUtils.isEmpty(categoryName)) {
            return R.drawable.ic_other;
        }

        String lower = categoryName.trim().toLowerCase();

        if (lower.contains("bank")) {
            return R.drawable.ic_bank;
        }
        if (lower.contains("food") || lower.contains("dining")) {
            return R.drawable.ic_food;
        }
        if (lower.contains("shop")) {
            return R.drawable.ic_shopping;
        }
        if (lower.contains("bill") || lower.contains("utility")) {
            return R.drawable.ic_bills;
        }
        if (lower.contains("transport")) {
            return R.drawable.ic_transport;
        }
        if (lower.contains("salary")) {
            return R.drawable.ic_salary;
        }
        if (lower.contains("entertainment") || lower.contains("movie")) {
            return R.drawable.ic_entertainment;
        }
        if (lower.contains("health") || lower.contains("medical")) {
            return R.drawable.ic_health;
        }
        if (lower.contains("education") || lower.contains("school") || lower.contains("course")) {
            return R.drawable.ic_education;
        }
        if (lower.contains("rent") || lower.contains("house")) {
            return R.drawable.ic_rent;
        }
        if (lower.contains("freelance") || lower.contains("income")) {
            return R.drawable.ic_payment;
        }
        if (lower.contains("invest") || lower.contains("save") || lower.contains("saving")) {
            return R.drawable.ic_savings;
        }

        return R.drawable.ic_other;
    }

    /**
     * Binds icon and/or initials badge for a category avatar container.
     */
    public static void setCategoryAvatar(ImageView ivIcon, TextView tvAvatar, String categoryName, String type) {
        int iconRes = getCategoryIconResource(categoryName);
        String initials = getCategoryInitials(categoryName);

        if (tvAvatar != null) {
            tvAvatar.setText(initials);
            tvAvatar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            tvAvatar.setTypeface(null, Typeface.BOLD);
            tvAvatar.setSingleLine(true);
            tvAvatar.setMaxLines(1);
            tvAvatar.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            tvAvatar.setSelected(true);
            tvAvatar.setGravity(android.view.Gravity.CENTER);
            tvAvatar.setIncludeFontPadding(false);
        }

        if (ivIcon != null) {
            ivIcon.setImageResource(iconRes);
            Context context = ivIcon.getContext();
            boolean isIncome = "INCOME".equalsIgnoreCase(type);
            int colorRes = isIncome ? R.color.success : R.color.primary;
            ivIcon.setColorFilter(ContextCompat.getColor(context, colorRes));
            ivIcon.setVisibility(View.VISIBLE);
        }

        if (tvAvatar != null && ivIcon == null) {
            tvAvatar.setVisibility(View.VISIBLE);
        }
    }
}
