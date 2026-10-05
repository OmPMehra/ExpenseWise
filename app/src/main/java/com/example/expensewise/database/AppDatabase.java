package com.example.expensewise.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.expensewise.models.Budget;
import com.example.expensewise.models.Category;
import com.example.expensewise.models.RecurringExpense;
import com.example.expensewise.models.SavingsGoal;
import com.example.expensewise.models.TransactionItem;
import com.example.expensewise.models.User;

@Database(
    entities = {
        User.class,
        TransactionItem.class,
        Category.class,
        Budget.class,
        SavingsGoal.class,
        RecurringExpense.class
    },
    version = 1,
    exportSchema = false
)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "expensewise_db";
    private static volatile AppDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract TransactionDao transactionDao();
    public abstract CategoryDao categoryDao();
    public abstract BudgetDao budgetDao();
    public abstract SavingsGoalDao savingsGoalDao();
    public abstract RecurringExpenseDao recurringExpenseDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
