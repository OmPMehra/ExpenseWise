package com.example.expensewise;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensewise.models.Budget;
import com.example.expensewise.utils.FinancialUtils;
import com.example.expensewise.utils.SessionManager;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    public interface OnBudgetClickListener {
        void onBudgetClick(Budget budget);
    }

    private final Context context;
    private List<Budget> budgetList;
    private Map<String, Double> categorySpentMap;
    private final OnBudgetClickListener listener;
    private String currencySymbol;

    public BudgetAdapter(Context context, OnBudgetClickListener listener) {
        this.context = context;
        this.budgetList = new ArrayList<>();
        this.categorySpentMap = new HashMap<>();
        this.listener = listener;
        SessionManager sessionManager = new SessionManager(context);
        this.currencySymbol = sessionManager.getCurrencySymbol();
    }

    public void setCurrencySymbol(String symbol) {
        this.currencySymbol = symbol;
        notifyDataSetChanged();
    }

    public void setData(List<Budget> budgets, Map<String, Double> spentMap) {
        this.budgetList = budgets != null ? budgets : new ArrayList<>();
        this.categorySpentMap = spentMap != null ? spentMap : new HashMap<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget budget = budgetList.get(position);
        double spent = categorySpentMap.getOrDefault(budget.getCategory(), 0.0);
        holder.bind(budget, spent, listener, currencySymbol);
    }

    @Override
    public int getItemCount() {
        return budgetList.size();
    }

    public static class BudgetViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvBudgetItemCategory;
        private final TextView tvBudgetItemStatusBadge;
        private final LinearProgressIndicator progressBudgetItem;
        private final TextView tvBudgetItemSpent;
        private final TextView tvBudgetItemRemaining;
        private final TextView tvBudgetItemTotal;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBudgetItemCategory = itemView.findViewById(R.id.tvBudgetItemCategory);
            tvBudgetItemStatusBadge = itemView.findViewById(R.id.tvBudgetItemStatusBadge);
            progressBudgetItem = itemView.findViewById(R.id.progressBudgetItem);
            tvBudgetItemSpent = itemView.findViewById(R.id.tvBudgetItemSpent);
            tvBudgetItemRemaining = itemView.findViewById(R.id.tvBudgetItemRemaining);
            tvBudgetItemTotal = itemView.findViewById(R.id.tvBudgetItemTotal);
        }

        public void bind(Budget budget, double spent, OnBudgetClickListener listener, String currencySymbol) {
            tvBudgetItemCategory.setText(budget.getCategory());

            double budgetAmount = budget.getAmount();
            double remaining = budgetAmount - spent;
            double percentage = budgetAmount > 0 ? (spent / budgetAmount) * 100.0 : 0.0;
            int progressInt = Math.min((int) percentage, 100);

            progressBudgetItem.setProgress(progressInt);

            Context context = itemView.getContext();
            tvBudgetItemSpent.setText(context.getString(R.string.budget_spent, FinancialUtils.formatCurrency(spent, currencySymbol)));
            tvBudgetItemRemaining.setText(context.getString(R.string.budget_remaining, FinancialUtils.formatCurrency(remaining, currencySymbol)));
            tvBudgetItemTotal.setText(context.getString(R.string.budget_total, FinancialUtils.formatCurrency(budgetAmount, currencySymbol)));

            if (percentage < 75) {
                tvBudgetItemStatusBadge.setText(R.string.budget_status_normal);
                tvBudgetItemStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.secondary));
                progressBudgetItem.setIndicatorColor(ContextCompat.getColor(context, R.color.secondary));
            } else if (percentage <= 90) {
                tvBudgetItemStatusBadge.setText(R.string.budget_status_warning);
                tvBudgetItemStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.warning));
                progressBudgetItem.setIndicatorColor(ContextCompat.getColor(context, R.color.warning));
            } else {
                tvBudgetItemStatusBadge.setText(R.string.budget_status_critical);
                tvBudgetItemStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.error));
                progressBudgetItem.setIndicatorColor(ContextCompat.getColor(context, R.color.error));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBudgetClick(budget);
                }
            });
        }
    }
}
