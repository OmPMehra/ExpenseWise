package com.example.expensewise.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "recurring_expenses")
public class RecurringExpense {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private long userId;
    private String name;
    private double amount;
    private String category;
    private String frequency; // "Daily", "Weekly", "Monthly", "Yearly"
    private String nextDueDate;
    private boolean isNotificationEnabled;

    public RecurringExpense() {
    }

    @Ignore
    public RecurringExpense(long userId, String name, double amount, String category, String frequency, String nextDueDate, boolean isNotificationEnabled) {
        this.userId = userId;
        this.name = name;
        this.amount = amount;
        this.category = category;
        this.frequency = frequency;
        this.nextDueDate = nextDueDate;
        this.isNotificationEnabled = isNotificationEnabled;
    }

    @Ignore
    public RecurringExpense(long id, long userId, String name, double amount, String category, String frequency, String nextDueDate, boolean isNotificationEnabled) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.amount = amount;
        this.category = category;
        this.frequency = frequency;
        this.nextDueDate = nextDueDate;
        this.isNotificationEnabled = isNotificationEnabled;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(String nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public boolean isNotificationEnabled() {
        return isNotificationEnabled;
    }

    public void setNotificationEnabled(boolean notificationEnabled) {
        isNotificationEnabled = notificationEnabled;
    }
}
