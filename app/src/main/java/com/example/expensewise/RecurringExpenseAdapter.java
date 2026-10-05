package com.example.expensewise;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensewise.models.RecurringExpense;
import com.example.expensewise.utils.FinancialUtils;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class RecurringExpenseAdapter extends RecyclerView.Adapter<RecurringExpenseAdapter.RecurringViewHolder> {

    public interface OnRecurringActionListener {
        void onPostClick(RecurringExpense expense);
        void onEditClick(RecurringExpense expense);
        void onDeleteClick(RecurringExpense expense);
    }

    private final Context context;
    private final OnRecurringActionListener listener;
    private List<RecurringExpense> expenses = new ArrayList<>();
    private String currencySymbol = "₹";

    public RecurringExpenseAdapter(Context context, OnRecurringActionListener listener, String currencySymbol) {
        this.context = context;
        this.listener = listener;
        this.currencySymbol = currencySymbol != null ? currencySymbol : "₹";
    }

    public void setExpenses(List<RecurringExpense> expenses) {
        this.expenses = expenses != null ? expenses : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecurringViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recurring_expense, parent, false);
        return new RecurringViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecurringViewHolder holder, int position) {
        RecurringExpense expense = expenses.get(position);

        holder.tvRecurringName.setText(expense.getName());
        holder.tvRecurringCategory.setText("Category: " + (expense.getCategory() != null ? expense.getCategory() : "Bills"));
        holder.tvRecurringAmount.setText(FinancialUtils.formatCurrency(expense.getAmount(), currencySymbol));
        holder.tvRecurringNextDue.setText(expense.getNextDueDate() != null ? expense.getNextDueDate() : "N/A");
        holder.tvRecurringFrequency.setText(expense.getFrequency() != null ? expense.getFrequency() : "Monthly");

        holder.btnPostExpense.setOnClickListener(v -> {
            if (listener != null) listener.onPostClick(expense);
        });

        holder.btnEditRecurring.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(expense);
        });

        holder.btnDeleteRecurring.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(expense);
        });
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    static class RecurringViewHolder extends RecyclerView.ViewHolder {
        TextView tvRecurringName, tvRecurringCategory, tvRecurringAmount, tvRecurringNextDue, tvRecurringFrequency;
        MaterialButton btnPostExpense;
        ImageButton btnEditRecurring, btnDeleteRecurring;

        public RecurringViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRecurringName = itemView.findViewById(R.id.tvRecurringName);
            tvRecurringCategory = itemView.findViewById(R.id.tvRecurringCategory);
            tvRecurringAmount = itemView.findViewById(R.id.tvRecurringAmount);
            tvRecurringNextDue = itemView.findViewById(R.id.tvRecurringNextDue);
            tvRecurringFrequency = itemView.findViewById(R.id.tvRecurringFrequency);
            btnPostExpense = itemView.findViewById(R.id.btnPostExpense);
            btnEditRecurring = itemView.findViewById(R.id.btnEditRecurring);
            btnDeleteRecurring = itemView.findViewById(R.id.btnDeleteRecurring);
        }
    }
}
