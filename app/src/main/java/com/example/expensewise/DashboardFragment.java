package com.example.expensewise;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensewise.models.Budget;
import com.example.expensewise.models.TransactionItem;
import com.example.expensewise.repositories.BudgetRepository;
import com.example.expensewise.repositories.TransactionRepository;
import com.example.expensewise.utils.DemoDataGenerator;
import com.example.expensewise.utils.FinancialUtils;
import com.example.expensewise.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DashboardFragment extends Fragment implements TransactionAdapter.OnTransactionClickListener {

    private TextView tvGreeting, tvUserName, tvAvatarInitials;
    private TextView tvCurrentBalance, tvTotalIncomeCard, tvTotalExpenseCard, tvThisMonthExpenses;
    private TextView tvBudgetStatusBadge, tvBudgetSpent, tvBudgetRemaining, tvBudgetTotal;
    private LinearProgressIndicator progressMonthlyBudget;
    private RecyclerView rvRecentTransactions;
    private View layoutEmptyState;
    private ExtendedFloatingActionButton fabAddIncome, fabAddExpense;

    private SessionManager sessionManager;
    private TransactionRepository transactionRepository;
    private BudgetRepository budgetRepository;
    private TransactionAdapter transactionAdapter;

    private long currentUserId = -1;
    private String currencySymbol = "₹";

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getContext() == null) return;

        sessionManager = new SessionManager(getContext());
        transactionRepository = new TransactionRepository(getContext());
        budgetRepository = new BudgetRepository(getContext());

        currentUserId = sessionManager.getCurrentUserId();
        currencySymbol = sessionManager.getCurrencySymbol();

        initViews(view);
        setupGreeting();
        setupRecyclerView();
        setupListeners();
        observeData();
    }

    private void initViews(View view) {
        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvAvatarInitials = view.findViewById(R.id.tvAvatarInitials);

        tvCurrentBalance = view.findViewById(R.id.tvCurrentBalance);
        tvTotalIncomeCard = view.findViewById(R.id.tvTotalIncomeCard);
        tvTotalExpenseCard = view.findViewById(R.id.tvTotalExpenseCard);
        tvThisMonthExpenses = view.findViewById(R.id.tvThisMonthExpenses);

        tvBudgetStatusBadge = view.findViewById(R.id.tvBudgetStatusBadge);
        tvBudgetSpent = view.findViewById(R.id.tvBudgetSpent);
        tvBudgetRemaining = view.findViewById(R.id.tvBudgetRemaining);
        tvBudgetTotal = view.findViewById(R.id.tvBudgetTotal);
        progressMonthlyBudget = view.findViewById(R.id.progressMonthlyBudget);

        rvRecentTransactions = view.findViewById(R.id.rvRecentTransactions);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        MaterialButton btnLoadDemoDataDash = view.findViewById(R.id.btnLoadDemoDataDash);
        TextView btnViewAllTransactions = view.findViewById(R.id.btnViewAllTransactions);

        fabAddIncome = view.findViewById(R.id.fabAddIncome);
        fabAddExpense = view.findViewById(R.id.fabAddExpense);

        btnLoadDemoDataDash.setOnClickListener(v -> loadDemoData());
        btnViewAllTransactions.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToTab(R.id.nav_transactions);
            }
        });
    }

    private void setupGreeting() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour < 12) {
            tvGreeting.setText(R.string.greeting_morning);
        } else if (hour < 17) {
            tvGreeting.setText(R.string.greeting_afternoon);
        } else {
            tvGreeting.setText(R.string.greeting_evening);
        }

        String userName = sessionManager.getCurrentUserName();
        tvUserName.setText(TextUtils.isEmpty(userName) ? "User" : userName);

        updateInitials(userName);
    }

    private void updateInitials(String name) {
        if (TextUtils.isEmpty(name)) {
            tvAvatarInitials.setText("EW");
            return;
        }
        String[] parts = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        if (parts.length > 0 && !parts[0].isEmpty()) {
            initials.append(parts[0].charAt(0));
        }
        if (parts.length > 1 && !parts[1].isEmpty()) {
            initials.append(parts[1].charAt(0));
        }
        tvAvatarInitials.setText(initials.toString().toUpperCase());
    }

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter(getContext(), this);
        rvRecentTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecentTransactions.setAdapter(transactionAdapter);
    }

    private void setupListeners() {
        fabAddExpense.setOnClickListener(v -> openAddTransaction("EXPENSE"));
        fabAddIncome.setOnClickListener(v -> openAddTransaction("INCOME"));
    }

    private void openAddTransaction(String type) {
        Intent intent = new Intent(getContext(), AddTransactionActivity.class);
        intent.putExtra("type", type);
        startActivity(intent);
    }

    private void observeData() {
        if (currentUserId == -1) return;

        Calendar now = Calendar.getInstance();
        int currentMonth = now.get(Calendar.MONTH) + 1; // 1-12
        int currentYear = now.get(Calendar.YEAR);
        String currentMonthPrefix = String.format(java.util.Locale.US, "%04d-%02d", currentYear, currentMonth);

        transactionRepository.getTransactionsForUser(currentUserId).observe(getViewLifecycleOwner(), transactions -> {
            if (transactions == null || transactions.isEmpty()) {
                rvRecentTransactions.setVisibility(View.GONE);
                layoutEmptyState.setVisibility(View.VISIBLE);

                tvCurrentBalance.setText(FinancialUtils.formatCurrency(0, currencySymbol));
                String incZero = "+ " + FinancialUtils.formatCurrency(0, currencySymbol);
                tvTotalIncomeCard.setText(incZero);
                String expZero = "- " + FinancialUtils.formatCurrency(0, currencySymbol);
                tvTotalExpenseCard.setText(expZero);
                tvThisMonthExpenses.setText(FinancialUtils.formatCurrency(0, currencySymbol));

                updateBudgetCard(0, 0);
            } else {
                rvRecentTransactions.setVisibility(View.VISIBLE);
                layoutEmptyState.setVisibility(View.GONE);

                double totalIncome = 0;
                double totalExpense = 0;
                double thisMonthExpense = 0;

                for (TransactionItem item : transactions) {
                    if ("INCOME".equalsIgnoreCase(item.getType())) {
                        totalIncome += item.getAmount();
                    } else if ("EXPENSE".equalsIgnoreCase(item.getType())) {
                        totalExpense += item.getAmount();
                        if (item.getDate() != null && item.getDate().startsWith(currentMonthPrefix)) {
                            thisMonthExpense += item.getAmount();
                        }
                    }
                }

                double netBalance = totalIncome - totalExpense;

                tvCurrentBalance.setText(FinancialUtils.formatCurrency(netBalance, currencySymbol));
                String incStr = "+ " + FinancialUtils.formatCurrency(totalIncome, currencySymbol);
                tvTotalIncomeCard.setText(incStr);
                String expStr = "- " + FinancialUtils.formatCurrency(totalExpense, currencySymbol);
                tvTotalExpenseCard.setText(expStr);
                tvThisMonthExpenses.setText(FinancialUtils.formatCurrency(thisMonthExpense, currencySymbol));

                // Top 5 latest transactions
                List<TransactionItem> recentList = new ArrayList<>();
                int maxCount = Math.min(5, transactions.size());
                for (int i = 0; i < maxCount; i++) {
                    recentList.add(transactions.get(i));
                }
                transactionAdapter.setTransactions(recentList);

                double finalThisMonthExpense = thisMonthExpense;
                budgetRepository.getBudgetsForMonth(currentUserId, currentMonth, currentYear).observe(getViewLifecycleOwner(), budgets -> {
                    double overallBudgetAmount = 0;
                    if (budgets != null && !budgets.isEmpty()) {
                        for (Budget b : budgets) {
                            if ("OVERALL".equalsIgnoreCase(b.getCategory())) {
                                overallBudgetAmount = b.getAmount();
                                break;
                            } else {
                                overallBudgetAmount += b.getAmount();
                            }
                        }
                    }
                    updateBudgetCard(finalThisMonthExpense, overallBudgetAmount);
                });
            }
        });
    }

    private void updateBudgetCard(double spent, double budgetTotal) {
        if (getContext() == null) return;

        if (budgetTotal <= 0) {
            tvBudgetTotal.setText(getString(R.string.budget_total, "Not Set"));
            tvBudgetSpent.setText(getString(R.string.budget_spent, FinancialUtils.formatCurrency(spent, currencySymbol)));
            tvBudgetRemaining.setText(getString(R.string.budget_remaining, "N/A"));
            progressMonthlyBudget.setProgress(0);

            tvBudgetStatusBadge.setText(R.string.budget_status_normal);
            tvBudgetStatusBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary));
            return;
        }

        double remaining = budgetTotal - spent;
        double percentage = (spent / budgetTotal) * 100.0;
        int progressInt = Math.min((int) percentage, 100);

        tvBudgetTotal.setText(getString(R.string.budget_total, FinancialUtils.formatCurrency(budgetTotal, currencySymbol)));
        tvBudgetSpent.setText(getString(R.string.budget_spent, FinancialUtils.formatCurrency(spent, currencySymbol)));
        tvBudgetRemaining.setText(getString(R.string.budget_remaining, FinancialUtils.formatCurrency(remaining, currencySymbol)));
        progressMonthlyBudget.setProgress(progressInt);

        if (percentage < 70) {
            tvBudgetStatusBadge.setText(R.string.budget_status_normal);
            tvBudgetStatusBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary));
            progressMonthlyBudget.setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.secondary));
        } else if (percentage <= 90) {
            tvBudgetStatusBadge.setText(R.string.budget_status_warning);
            tvBudgetStatusBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.warning));
            progressMonthlyBudget.setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.warning));
        } else {
            tvBudgetStatusBadge.setText(R.string.budget_status_critical);
            tvBudgetStatusBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.error));
            progressMonthlyBudget.setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.error));
        }
    }

    private void loadDemoData() {
        if (getContext() == null || currentUserId == -1) return;

        DemoDataGenerator.seedDemoData(getContext(), currentUserId, success -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(getContext(), R.string.demo_data_success, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), R.string.demo_data_failed, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public void onTransactionClick(TransactionItem transaction) {
        Intent intent = new Intent(getContext(), TransactionDetailsActivity.class);
        intent.putExtra("EXTRA_TRANSACTION_ID", transaction.getId());
        startActivity(intent);
    }

    @Override
    public void onTransactionLongClick(TransactionItem transaction) {
        onTransactionClick(transaction);
    }
}
