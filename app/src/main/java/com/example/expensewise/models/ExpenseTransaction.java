package com.example.expensewise.models;

import androidx.room.Ignore;

public class ExpenseTransaction extends TransactionItem {

    public ExpenseTransaction() {
        super();
    }

    @Ignore
    public ExpenseTransaction(long userId, String type, double amount, String category, String description, String date, String time, String paymentMethod, String notes) {
        super(userId, type, amount, category, description, date, time, paymentMethod, notes);
    }

    @Ignore
    public ExpenseTransaction(long id, long userId, String type, double amount, String category, String description, String date, String time, String paymentMethod, String notes, long createdAt) {
        super(id, userId, type, amount, category, description, date, time, paymentMethod, notes, createdAt);
    }
}
