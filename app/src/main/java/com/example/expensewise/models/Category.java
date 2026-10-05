package com.example.expensewise.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class Category {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private String type; // "INCOME" or "EXPENSE"
    private String iconResName;
    private String colorHex;
    private boolean isCustom;

    public Category() {
    }

    @Ignore
    public Category(String name, String type, String iconResName, String colorHex, boolean isCustom) {
        this.name = name;
        this.type = type;
        this.iconResName = iconResName;
        this.colorHex = colorHex;
        this.isCustom = isCustom;
    }

    @Ignore
    public Category(long id, String name, String type, String iconResName, String colorHex, boolean isCustom) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.iconResName = iconResName;
        this.colorHex = colorHex;
        this.isCustom = isCustom;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIconResName() {
        return iconResName;
    }

    public void setIconResName(String iconResName) {
        this.iconResName = iconResName;
    }

    public String getColorHex() {
        return colorHex;
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public void setCustom(boolean custom) {
        isCustom = custom;
    }
}
