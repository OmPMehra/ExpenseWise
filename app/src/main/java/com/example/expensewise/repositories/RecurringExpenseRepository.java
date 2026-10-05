package com.example.expensewise.repositories;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.expensewise.database.AppDatabase;
import com.example.expensewise.database.RecurringExpenseDao;
import com.example.expensewise.models.RecurringExpense;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecurringExpenseRepository {

    private final RecurringExpenseDao recurringExpenseDao;
    private final ExecutorService executorService;

    public interface Callback<T> {
        void onResult(T result);
    }

    public RecurringExpenseRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.recurringExpenseDao = database.recurringExpenseDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(RecurringExpense expense, Callback<Long> callback) {
        executorService.execute(() -> {
            long id = recurringExpenseDao.insertRecurringExpense(expense);
            if (callback != null) {
                callback.onResult(id);
            }
        });
    }

    public void update(RecurringExpense expense) {
        executorService.execute(() -> recurringExpenseDao.updateRecurringExpense(expense));
    }

    public void delete(RecurringExpense expense) {
        executorService.execute(() -> recurringExpenseDao.deleteRecurringExpense(expense));
    }

    public LiveData<List<RecurringExpense>> getRecurringExpensesForUser(long userId) {
        return recurringExpenseDao.getRecurringExpensesForUser(userId);
    }
}
