package com.example.expensewise.repositories;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.expensewise.database.AppDatabase;
import com.example.expensewise.database.BudgetDao;
import com.example.expensewise.models.Budget;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BudgetRepository {

    private final BudgetDao budgetDao;
    private final ExecutorService executorService;

    public interface Callback<T> {
        void onResult(T result);
    }

    public BudgetRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.budgetDao = database.budgetDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(Budget budget, Callback<Long> callback) {
        executorService.execute(() -> {
            long id = budgetDao.insertBudget(budget);
            if (callback != null) {
                callback.onResult(id);
            }
        });
    }

    public void update(Budget budget) {
        executorService.execute(() -> budgetDao.updateBudget(budget));
    }

    public void delete(Budget budget) {
        executorService.execute(() -> budgetDao.deleteBudget(budget));
    }

    public LiveData<List<Budget>> getBudgetsForMonth(long userId, int month, int year) {
        return budgetDao.getBudgetsForMonth(userId, month, year);
    }

    public void getBudgetForCategory(long userId, String category, int month, int year, Callback<Budget> callback) {
        executorService.execute(() -> {
            Budget budget = budgetDao.getBudgetForCategory(userId, category, month, year);
            if (callback != null) {
                callback.onResult(budget);
            }
        });
    }

    public LiveData<List<Budget>> getAllBudgetsForUser(long userId) {
        return budgetDao.getAllBudgetsForUser(userId);
    }
}
