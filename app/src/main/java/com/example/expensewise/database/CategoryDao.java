package com.example.expensewise.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.expensewise.models.Category;

import java.util.List;

@Dao
public interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertCategory(Category category);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategories(List<Category> categories);

    @Update
    void updateCategory(Category category);

    @Delete
    void deleteCategory(Category category);

    @Query("SELECT * FROM categories ORDER BY name ASC")
    LiveData<List<Category>> getAllCategories();

    @Query("SELECT * FROM categories ORDER BY name ASC")
    List<Category> getAllCategoriesSync();

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name ASC")
    LiveData<List<Category>> getCategoriesByType(String type);

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    Category getCategoryByName(String name);

    @Query("SELECT COUNT(*) FROM categories")
    int getCategoryCount();
}
