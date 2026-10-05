package com.example.expensewise.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "ExpenseWisePref";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_CURRENT_USER_ID = "currentUserId";
    private static final String KEY_CURRENT_USER_NAME = "currentUserName";
    private static final String KEY_CURRENT_USER_EMAIL = "currentUserEmail";
    private static final String KEY_IS_PIN_ENABLED = "isPinEnabled";
    private static final String KEY_APP_PIN = "appPin";
    private static final String KEY_CURRENCY_SYMBOL = "currencySymbol";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notificationsEnabled";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(long userId, String name, String email) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_CURRENT_USER_ID, userId);
        editor.putString(KEY_CURRENT_USER_NAME, name);
        editor.putString(KEY_CURRENT_USER_EMAIL, email);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    public long getCurrentUserId() {
        return pref.getLong(KEY_CURRENT_USER_ID, -1);
    }

    public void setCurrentUserId(long userId) {
        editor.putLong(KEY_CURRENT_USER_ID, userId);
        editor.apply();
    }

    public String getCurrentUserName() {
        return pref.getString(KEY_CURRENT_USER_NAME, "");
    }

    public void setCurrentUserName(String name) {
        editor.putString(KEY_CURRENT_USER_NAME, name);
        editor.apply();
    }

    public String getCurrentUserEmail() {
        return pref.getString(KEY_CURRENT_USER_EMAIL, "");
    }

    public void setCurrentUserEmail(String email) {
        editor.putString(KEY_CURRENT_USER_EMAIL, email);
        editor.apply();
    }

    public boolean isPinEnabled() {
        return pref.getBoolean(KEY_IS_PIN_ENABLED, false);
    }

    public void setPinEnabled(boolean isPinEnabled) {
        editor.putBoolean(KEY_IS_PIN_ENABLED, isPinEnabled);
        editor.apply();
    }

    public String getAppPin() {
        return pref.getString(KEY_APP_PIN, "");
    }

    public void setAppPin(String pin) {
        editor.putString(KEY_APP_PIN, pin);
        editor.apply();
    }

    public String getCurrencySymbol() {
        return pref.getString(KEY_CURRENCY_SYMBOL, "₹");
    }

    public void setCurrencySymbol(String currencySymbol) {
        editor.putString(KEY_CURRENCY_SYMBOL, currencySymbol != null ? currencySymbol : "₹");
        editor.apply();
    }

    public boolean isNotificationsEnabled() {
        return pref.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }

    public void setNotificationsEnabled(boolean enabled) {
        editor.putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled);
        editor.apply();
    }

    public void logoutUser() {
        editor.clear();
        editor.apply();
    }
}
