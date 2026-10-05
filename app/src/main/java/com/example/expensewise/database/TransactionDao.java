package com.example.expensewise.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.expensewise.models.TransactionItem;

import java.util.List;

@Dao
public interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTransaction(TransactionItem transaction);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTransactions(List<TransactionItem> transactions);

    @Update
    void updateTransaction(TransactionItem transaction);

    @Delete
    void deleteTransaction(TransactionItem transaction);

    @Query("DELETE FROM transactions WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY createdAt DESC")
    LiveData<List<TransactionItem>> getTransactionsForUser(long userId);

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY createdAt DESC")
    List<TransactionItem> getTransactionsForUserSync(long userId);

    @Query("SELECT * FROM transactions WHERE userId = :userId AND type = :type ORDER BY createdAt DESC")
    LiveData<List<TransactionItem>> getTransactionsByType(long userId, String type);

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    TransactionItem getTransactionById(long id);

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = 'INCOME'")
    LiveData<Double> getTotalIncome(long userId);

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = 'EXPENSE'")
    LiveData<Double> getTotalExpense(long userId);

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = 'INCOME'")
    Double getTotalIncomeSync(long userId);

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = 'EXPENSE'")
    Double getTotalExpenseSync(long userId);

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND category = :category AND type = 'EXPENSE'")
    LiveData<Double> getTotalExpenseByCategory(long userId, String category);

    @Query("DELETE FROM transactions WHERE userId = :userId")
    void deleteAllForUser(long userId);
}
