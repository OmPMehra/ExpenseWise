package com.example.expensewise.repositories;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.expensewise.database.AppDatabase;
import com.example.expensewise.database.TransactionDao;
import com.example.expensewise.models.TransactionItem;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TransactionRepository {

    private final TransactionDao transactionDao;
    private final ExecutorService executorService;

    public interface Callback<T> {
        void onResult(T result);
    }

    public TransactionRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.transactionDao = database.transactionDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(TransactionItem transaction, Callback<Long> callback) {
        executorService.execute(() -> {
            long id = transactionDao.insertTransaction(transaction);
            if (callback != null) {
                callback.onResult(id);
            }
        });
    }

    public void update(TransactionItem transaction) {
        executorService.execute(() -> transactionDao.updateTransaction(transaction));
    }

    public void delete(TransactionItem transaction) {
        executorService.execute(() -> transactionDao.deleteTransaction(transaction));
    }

    public void deleteById(long id) {
        executorService.execute(() -> transactionDao.deleteById(id));
    }

    public LiveData<List<TransactionItem>> getTransactionsForUser(long userId) {
        return transactionDao.getTransactionsForUser(userId);
    }

    public LiveData<List<TransactionItem>> getTransactionsByType(long userId, String type) {
        return transactionDao.getTransactionsByType(userId, type);
    }

    public LiveData<Double> getTotalIncome(long userId) {
        return transactionDao.getTotalIncome(userId);
    }

    public LiveData<Double> getTotalExpense(long userId) {
        return transactionDao.getTotalExpense(userId);
    }

    public LiveData<Double> getTotalExpenseByCategory(long userId, String category) {
        return transactionDao.getTotalExpenseByCategory(userId, category);
    }

    public void getTransactionById(long id, Callback<TransactionItem> callback) {
        executorService.execute(() -> {
            TransactionItem item = transactionDao.getTransactionById(id);
            if (callback != null) {
                callback.onResult(item);
            }
        });
    }
}
