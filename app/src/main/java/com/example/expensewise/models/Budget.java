package com.example.expensewise.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "budgets")
public class Budget {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private long userId;
    private String category; // "OVERALL" or category name like "Food"
    private double amount;
    private int month; // 1 - 12
    private int year;

    public Budget() {
    }

    @Ignore
    public Budget(long userId, String category, double amount, int month, int year) {
        this.userId = userId;
        this.category = category;
        this.amount = amount;
        this.month = month;
        this.year = year;
    }

    @Ignore
    public Budget(long id, long userId, String category, double amount, int month, int year) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.amount = amount;
        this.month = month;
        this.year = year;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
