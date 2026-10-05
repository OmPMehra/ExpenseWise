package com.example.expensewise.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class TransactionItem {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private long userId;
    private String type; // "INCOME" or "EXPENSE"
    private double amount;
    private String category;
    private String description;
    private String date; // Formatted date e.g. "yyyy-MM-dd" or "dd MMM yyyy"
    private String time; // Formatted time e.g. "HH:mm"
    private String paymentMethod; // e.g., "Cash", "Card", "UPI", "Bank Transfer"
    private String notes;
    private long createdAt; // Timestamp in milliseconds

    public TransactionItem() {
        this.createdAt = System.currentTimeMillis();
    }

    @Ignore
    public TransactionItem(long userId, String type, double amount, String category, String description, String date, String time, String paymentMethod, String notes) {
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.date = date;
        this.time = time;
        this.paymentMethod = paymentMethod;
        this.notes = notes;
        this.createdAt = System.currentTimeMillis();
    }

    @Ignore
    public TransactionItem(long id, long userId, String type, double amount, String category, String description, String date, String time, String paymentMethod, String notes, long createdAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.description = description;
        this.date = date;
        this.time = time;
        this.paymentMethod = paymentMethod;
        this.notes = notes;
        this.createdAt = createdAt;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
