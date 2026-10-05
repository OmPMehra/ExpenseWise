package com.example.expensewise.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private String email;
    private String passwordHash;
    private String pin;
    private boolean isPinEnabled;
    private String currencySymbol;
    private long createdAt;

    public User() {
        this.currencySymbol = "₹";
        this.createdAt = System.currentTimeMillis();
    }

    @Ignore
    public User(String name, String email, String passwordHash) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.pin = "";
        this.isPinEnabled = false;
        this.currencySymbol = "₹";
        this.createdAt = System.currentTimeMillis();
    }

    @Ignore
    public User(long id, String name, String email, String passwordHash, String pin, boolean isPinEnabled, String currencySymbol, long createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.pin = pin;
        this.isPinEnabled = isPinEnabled;
        this.currencySymbol = currencySymbol != null ? currencySymbol : "₹";
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public boolean isPinEnabled() {
        return isPinEnabled;
    }

    public void setPinEnabled(boolean pinEnabled) {
        isPinEnabled = pinEnabled;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
