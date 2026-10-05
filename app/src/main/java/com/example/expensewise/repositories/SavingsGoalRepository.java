package com.example.expensewise.repositories;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.expensewise.database.AppDatabase;
import com.example.expensewise.database.SavingsGoalDao;
import com.example.expensewise.models.SavingsGoal;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SavingsGoalRepository {

    private final SavingsGoalDao savingsGoalDao;
    private final ExecutorService executorService;

    public interface Callback<T> {
        void onResult(T result);
    }

    public SavingsGoalRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.savingsGoalDao = database.savingsGoalDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(SavingsGoal goal, Callback<Long> callback) {
        executorService.execute(() -> {
            long id = savingsGoalDao.insertSavingsGoal(goal);
            if (callback != null) {
                callback.onResult(id);
            }
        });
    }

    public void update(SavingsGoal goal) {
        executorService.execute(() -> savingsGoalDao.updateSavingsGoal(goal));
    }

    public void delete(SavingsGoal goal) {
        executorService.execute(() -> savingsGoalDao.deleteSavingsGoal(goal));
    }

    public LiveData<List<SavingsGoal>> getSavingsGoalsForUser(long userId) {
        return savingsGoalDao.getSavingsGoalsForUser(userId);
    }

    public void getGoalById(long id, Callback<SavingsGoal> callback) {
        executorService.execute(() -> {
            SavingsGoal goal = savingsGoalDao.getGoalById(id);
            if (callback != null) {
                callback.onResult(goal);
            }
        });
    }
}
