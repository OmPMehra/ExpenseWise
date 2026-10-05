package com.example.expensewise.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.expensewise.models.Budget;

import java.util.List;

@Dao
public interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertBudget(Budget budget);

    @Update
    void updateBudget(Budget budget);

    @Delete
    void deleteBudget(Budget budget);

    @Query("SELECT * FROM budgets WHERE userId = :userId AND month = :month AND year = :year")
    LiveData<List<Budget>> getBudgetsForMonth(long userId, int month, int year);

    @Query("SELECT * FROM budgets WHERE userId = :userId AND category = :category AND month = :month AND year = :year LIMIT 1")
    Budget getBudgetForCategory(long userId, String category, int month, int year);

    @Query("SELECT * FROM budgets WHERE userId = :userId ORDER BY year DESC, month DESC")
    LiveData<List<Budget>> getAllBudgetsForUser(long userId);
}
