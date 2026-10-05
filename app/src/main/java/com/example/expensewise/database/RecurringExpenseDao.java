package com.example.expensewise.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.expensewise.models.RecurringExpense;

import java.util.List;

@Dao
public interface RecurringExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertRecurringExpense(RecurringExpense expense);

    @Update
    void updateRecurringExpense(RecurringExpense expense);

    @Delete
    void deleteRecurringExpense(RecurringExpense expense);

    @Query("SELECT * FROM recurring_expenses WHERE userId = :userId ORDER BY id DESC")
    LiveData<List<RecurringExpense>> getRecurringExpensesForUser(long userId);
}
