package com.example.expensewise.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.expensewise.models.SavingsGoal;

import java.util.List;

@Dao
public interface SavingsGoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSavingsGoal(SavingsGoal goal);

    @Update
    void updateSavingsGoal(SavingsGoal goal);

    @Delete
    void deleteSavingsGoal(SavingsGoal goal);

    @Query("SELECT * FROM savings_goals WHERE userId = :userId ORDER BY id DESC")
    LiveData<List<SavingsGoal>> getSavingsGoalsForUser(long userId);

    @Query("SELECT * FROM savings_goals WHERE id = :id LIMIT 1")
    SavingsGoal getGoalById(long id);
}
