package com.example.expensewise.utils;

import android.content.Context;

import com.example.expensewise.database.AppDatabase;
import com.example.expensewise.database.BudgetDao;
import com.example.expensewise.database.CategoryDao;
import com.example.expensewise.database.RecurringExpenseDao;
import com.example.expensewise.database.SavingsGoalDao;
import com.example.expensewise.database.TransactionDao;
import com.example.expensewise.models.Budget;
import com.example.expensewise.models.Category;
import com.example.expensewise.models.RecurringExpense;
import com.example.expensewise.models.SavingsGoal;
import com.example.expensewise.models.TransactionItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DemoDataGenerator {

    private final AppDatabase database;
    private final ExecutorService executorService;

    public interface DemoDataCallback {
        void onDataGenerated(boolean success);
    }

    public DemoDataGenerator(Context context) {
        this.database = AppDatabase.getInstance(context);
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public static void seedDemoData(Context context, long userId, DemoDataCallback callback) {
        new DemoDataGenerator(context).generateDemoDataForUser(userId, true, callback);
    }

    public void seedDemoData(long userId, DemoDataCallback callback) {
        generateDemoDataForUser(userId, true, callback);
    }

    public void populateDefaultCategoriesIfEmpty(DemoDataCallback callback) {
        executorService.execute(() -> {
            try {
                CategoryDao categoryDao = database.categoryDao();
                if (categoryDao.getCategoryCount() == 0) {
                    List<Category> categories = new ArrayList<>();
                    categories.add(new Category("Salary", "INCOME", "ic_salary", "#4CAF50", false));
                    categories.add(new Category("Freelance", "INCOME", "ic_freelance", "#2196F3", false));
                    categories.add(new Category("Investment", "INCOME", "ic_investment", "#009688", false));
                    categories.add(new Category("Food", "EXPENSE", "ic_food", "#FF5722", false));
                    categories.add(new Category("Transport", "EXPENSE", "ic_transport", "#3F51B5", false));
                    categories.add(new Category("Shopping", "EXPENSE", "ic_shopping", "#E91E63", false));
                    categories.add(new Category("Bills", "EXPENSE", "ic_bills", "#F44336", false));
                    categories.add(new Category("Entertainment", "EXPENSE", "ic_entertainment", "#9C27B0", false));
                    categories.add(new Category("Health", "EXPENSE", "ic_health", "#00BCD4", false));
                    categories.add(new Category("Other", "EXPENSE", "ic_other", "#607D8B", false));

                    categoryDao.insertCategories(categories);
                }
                if (callback != null) {
                    callback.onDataGenerated(true);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onDataGenerated(false);
                }
            }
        });
    }

    public void generateDemoDataForUser(long userId, boolean forceOverwrite, DemoDataCallback callback) {
        executorService.execute(() -> {
            try {
                populateDefaultCategoriesIfEmpty(null);

                TransactionDao transactionDao = database.transactionDao();
                List<TransactionItem> existing = transactionDao.getTransactionsForUserSync(userId);

                if (existing != null && !existing.isEmpty() && !forceOverwrite) {
                    if (callback != null) {
                        callback.onDataGenerated(true);
                    }
                    return;
                }

                if (forceOverwrite && existing != null) {
                    transactionDao.deleteAllForUser(userId);
                }

                String currentDate = FinancialUtils.getCurrentDateFormatted();
                String currentTime = FinancialUtils.getCurrentTimeFormatted();

                List<TransactionItem> demoTransactions = new ArrayList<>();

                // Sample salary income: ₹40,000
                demoTransactions.add(new TransactionItem(
                        userId, "INCOME", 40000.00, "Salary",
                        "Monthly Salary", currentDate, currentTime, "Bank Transfer", "Salary credited"
                ));

                // Sample expenses:
                // Food ₹4,500
                demoTransactions.add(new TransactionItem(
                        userId, "EXPENSE", 4500.00, "Food",
                        "Grocery & Dining out", currentDate, currentTime, "UPI", "Supermarket and dining"
                ));

                // Transport ₹2,000
                demoTransactions.add(new TransactionItem(
                        userId, "EXPENSE", 2000.00, "Transport",
                        "Fuel & Metro Pass", currentDate, currentTime, "Card", "Monthly fuel refilling"
                ));

                // Shopping ₹3,500
                demoTransactions.add(new TransactionItem(
                        userId, "EXPENSE", 3500.00, "Shopping",
                        "Clothes & Home decor", currentDate, currentTime, "Card", "Weekend shopping"
                ));

                // Bills ₹2,500
                demoTransactions.add(new TransactionItem(
                        userId, "EXPENSE", 2500.00, "Bills",
                        "Electricity & Internet Bill", currentDate, currentTime, "UPI", "Utility bills"
                ));

                // Entertainment ₹1,500
                demoTransactions.add(new TransactionItem(
                        userId, "EXPENSE", 1500.00, "Entertainment",
                        "Movie tickets & OTT subscription", currentDate, currentTime, "UPI", "OTT and movies"
                ));

                transactionDao.insertTransactions(demoTransactions);

                // Sample Budgets
                Calendar calendar = Calendar.getInstance();
                int currentMonth = calendar.get(Calendar.MONTH) + 1; // 1-12
                int currentYear = calendar.get(Calendar.YEAR);

                BudgetDao budgetDao = database.budgetDao();
                budgetDao.insertBudget(new Budget(userId, "OVERALL", 20000.00, currentMonth, currentYear));
                budgetDao.insertBudget(new Budget(userId, "Food", 6000.00, currentMonth, currentYear));

                // Sample Savings Goals
                SavingsGoalDao savingsGoalDao = database.savingsGoalDao();
                savingsGoalDao.insertSavingsGoal(new SavingsGoal(
                        userId, "Emergency Fund", 50000.00, 15000.00, "2026-12-31", "Fund for emergencies"
                ));
                savingsGoalDao.insertSavingsGoal(new SavingsGoal(
                        userId, "New Laptop", 60000.00, 20000.00, "2026-11-30", "High performance laptop"
                ));

                // Sample Recurring Expenses
                RecurringExpenseDao recurringExpenseDao = database.recurringExpenseDao();
                recurringExpenseDao.insertRecurringExpense(new RecurringExpense(
                        userId, "Internet Subscription", 999.00, "Bills", "Monthly", "2026-11-01", true
                ));

                if (callback != null) {
                    callback.onDataGenerated(true);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onDataGenerated(false);
                }
            }
        });
    }
}
