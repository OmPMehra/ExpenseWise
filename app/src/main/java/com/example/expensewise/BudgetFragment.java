package com.example.expensewise;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.example.expensewise.utils.FinancialUtils;
import com.example.expensewise.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BudgetFragment extends Fragment implements BudgetAdapter.OnBudgetClickListener {

    private TextView tvOverallBudgetBadge, tvOverallSpent, tvOverallRemaining, tvOverallTotal;
    private LinearProgressIndicator progressOverallBudget;
    private MaterialButton btnSetOverallBudget, btnAddBudgetHeader, btnAddBudgetEmpty;
    private RecyclerView rvCategoryBudgets;
    private View layoutEmptyBudgets;

    private SessionManager sessionManager;
    private BudgetRepository budgetRepository;
    private TransactionRepository transactionRepository;
    private BudgetAdapter budgetAdapter;

    private long currentUserId = -1;
    private String currencySymbol = "₹";

    public BudgetFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_budget, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getContext() == null) return;

        sessionManager = new SessionManager(getContext());
        budgetRepository = new BudgetRepository(getContext());
        transactionRepository = new TransactionRepository(getContext());

        currentUserId = sessionManager.getCurrentUserId();
        currencySymbol = sessionManager.getCurrencySymbol();

        initViews(view);
        setupRecyclerView();
        setupListeners();
        observeBudgetsAndExpenses();
    }

    private void initViews(View view) {
        tvOverallBudgetBadge = view.findViewById(R.id.tvOverallBudgetBadge);
        tvOverallSpent = view.findViewById(R.id.tvOverallSpent);
        tvOverallRemaining = view.findViewById(R.id.tvOverallRemaining);
        tvOverallTotal = view.findViewById(R.id.tvOverallTotal);
        progressOverallBudget = view.findViewById(R.id.progressOverallBudget);

        btnSetOverallBudget = view.findViewById(R.id.btnSetOverallBudget);
        btnAddBudgetHeader = view.findViewById(R.id.btnAddBudgetHeader);
        btnAddBudgetEmpty = view.findViewById(R.id.btnAddBudgetEmpty);

        rvCategoryBudgets = view.findViewById(R.id.rvCategoryBudgets);
        layoutEmptyBudgets = view.findViewById(R.id.layoutEmptyBudgets);
    }

    private void setupRecyclerView() {
        budgetAdapter = new BudgetAdapter(getContext(), this);
        rvCategoryBudgets.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCategoryBudgets.setAdapter(budgetAdapter);
    }

    private void setupListeners() {
        View.OnClickListener openAddOverall = v -> {
            Intent intent = new Intent(getContext(), AddBudgetActivity.class);
            intent.putExtra(AddBudgetActivity.EXTRA_CATEGORY, "OVERALL");
            startActivity(intent);
        };

        btnSetOverallBudget.setOnClickListener(openAddOverall);

        View.OnClickListener openAddGeneral = v -> {
            Intent intent = new Intent(getContext(), AddBudgetActivity.class);
            startActivity(intent);
        };

        btnAddBudgetHeader.setOnClickListener(openAddGeneral);
        btnAddBudgetEmpty.setOnClickListener(openAddGeneral);
    }

    private void observeBudgetsAndExpenses() {
        if (currentUserId == -1) return;

        Calendar now = Calendar.getInstance();
        int currentMonth = now.get(Calendar.MONTH) + 1; // 1-12
        int currentYear = now.get(Calendar.YEAR);
        String currentMonthPrefix = String.format(Locale.US, "%04d-%02d", currentYear, currentMonth);

        // Fetch transactions for category spending
        transactionRepository.getTransactionsForUser(currentUserId).observe(getViewLifecycleOwner(), transactions -> {
            Map<String, Double> spentMap = new HashMap<>();
            double totalSpentMonth = 0;

            if (transactions != null) {
                for (TransactionItem item : transactions) {
                    if ("EXPENSE".equalsIgnoreCase(item.getType()) && item.getDate() != null && item.getDate().startsWith(currentMonthPrefix)) {
                        totalSpentMonth += item.getAmount();
                        String cat = item.getCategory() != null ? item.getCategory() : "Other";
                        double prev = spentMap.getOrDefault(cat, 0.0);
                        spentMap.put(cat, prev + item.getAmount());
                    }
                }
            }

            double finalTotalSpentMonth = totalSpentMonth;

            // Fetch budgets for current month
            budgetRepository.getBudgetsForMonth(currentUserId, currentMonth, currentYear).observe(getViewLifecycleOwner(), budgets -> {
                double overallBudgetAmt = 0;
                List<Budget> categoryBudgetList = new ArrayList<>();

                if (budgets != null) {
                    for (Budget b : budgets) {
                        if ("OVERALL".equalsIgnoreCase(b.getCategory())) {
                            overallBudgetAmt = b.getAmount();
                        } else {
                            categoryBudgetList.add(b);
                        }
                    }

                    if (overallBudgetAmt <= 0 && !categoryBudgetList.isEmpty()) {
                        for (Budget b : categoryBudgetList) {
                            overallBudgetAmt += b.getAmount();
                        }
                    }
                }

                updateOverallBudgetCard(finalTotalSpentMonth, overallBudgetAmt);

                if (categoryBudgetList.isEmpty()) {
                    rvCategoryBudgets.setVisibility(View.GONE);
                    layoutEmptyBudgets.setVisibility(View.VISIBLE);
                } else {
                    rvCategoryBudgets.setVisibility(View.VISIBLE);
                    layoutEmptyBudgets.setVisibility(View.GONE);
                    budgetAdapter.setData(categoryBudgetList, spentMap);
                }
            });
        });
    }

    private void updateOverallBudgetCard(double spent, double budgetTotal) {
        if (getContext() == null) return;

        if (budgetTotal <= 0) {
            tvOverallTotal.setText(getString(R.string.budget_total, "Not Set"));
            tvOverallSpent.setText(getString(R.string.budget_spent, FinancialUtils.formatCurrency(spent, currencySymbol)));
            tvOverallRemaining.setText(getString(R.string.budget_remaining, "N/A"));
            progressOverallBudget.setProgress(0);

            tvOverallBudgetBadge.setText(R.string.budget_status_normal);
            tvOverallBudgetBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary));
            return;
        }

        double remaining = budgetTotal - spent;
        double percentage = (spent / budgetTotal) * 100.0;
        int progressInt = Math.min((int) percentage, 100);

        tvOverallTotal.setText(getString(R.string.budget_total, FinancialUtils.formatCurrency(budgetTotal, currencySymbol)));
        tvOverallSpent.setText(getString(R.string.budget_spent, FinancialUtils.formatCurrency(spent, currencySymbol)));
        tvOverallRemaining.setText(getString(R.string.budget_remaining, FinancialUtils.formatCurrency(remaining, currencySymbol)));
        progressOverallBudget.setProgress(progressInt);

        if (percentage < 75) {
            tvOverallBudgetBadge.setText(R.string.budget_status_normal);
            tvOverallBudgetBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.secondary));
            progressOverallBudget.setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.secondary));
        } else if (percentage <= 90) {
            tvOverallBudgetBadge.setText(R.string.budget_status_warning);
            tvOverallBudgetBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.warning));
            progressOverallBudget.setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.warning));
        } else {
            tvOverallBudgetBadge.setText(R.string.budget_status_critical);
            tvOverallBudgetBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.error));
            progressOverallBudget.setIndicatorColor(ContextCompat.getColor(requireContext(), R.color.error));
        }
    }

    @Override
    public void onBudgetClick(Budget budget) {
        Intent intent = new Intent(getContext(), AddBudgetActivity.class);
        intent.putExtra(AddBudgetActivity.EXTRA_CATEGORY, budget.getCategory());
        intent.putExtra(AddBudgetActivity.EXTRA_MONTH, budget.getMonth());
        intent.putExtra(AddBudgetActivity.EXTRA_YEAR, budget.getYear());
        intent.putExtra(AddBudgetActivity.EXTRA_AMOUNT, budget.getAmount());
        startActivity(intent);
    }
}
