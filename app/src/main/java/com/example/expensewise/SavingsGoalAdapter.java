package com.example.expensewise;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensewise.models.SavingsGoal;
import com.example.expensewise.utils.FinancialUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SavingsGoalAdapter extends RecyclerView.Adapter<SavingsGoalAdapter.SavingsGoalViewHolder> {

    public interface OnGoalActionListener {
        void onDepositClick(SavingsGoal goal);
        void onEditClick(SavingsGoal goal);
        void onDeleteClick(SavingsGoal goal);
    }

    private final Context context;
    private final OnGoalActionListener listener;
    private List<SavingsGoal> goals = new ArrayList<>();
    private String currencySymbol = "₹";

    public SavingsGoalAdapter(Context context, OnGoalActionListener listener, String currencySymbol) {
        this.context = context;
        this.listener = listener;
        this.currencySymbol = currencySymbol != null ? currencySymbol : "₹";
    }

    public void setGoals(List<SavingsGoal> goals) {
        this.goals = goals != null ? goals : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SavingsGoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_savings_goal, parent, false);
        return new SavingsGoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SavingsGoalViewHolder holder, int position) {
        SavingsGoal goal = goals.get(position);

        holder.tvGoalName.setText(goal.getName());
        holder.tvTargetDate.setText("Target: " + (goal.getTargetDate() != null ? goal.getTargetDate() : "N/A"));

        holder.tvCurrentAmount.setText(FinancialUtils.formatCurrency(goal.getCurrentAmount(), currencySymbol));
        holder.tvTargetAmount.setText(FinancialUtils.formatCurrency(goal.getTargetAmount(), currencySymbol));

        double progressPct = FinancialUtils.calculateSavingsProgress(goal.getCurrentAmount(), goal.getTargetAmount());
        holder.progressGoal.setProgress((int) Math.min(progressPct, 100));
        holder.tvPercentage.setText(String.format(Locale.US, "%.2f%%", progressPct));

        holder.btnDeposit.setOnClickListener(v -> {
            if (listener != null) listener.onDepositClick(goal);
        });

        holder.btnEditGoal.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(goal);
        });

        holder.btnDeleteGoal.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(goal);
        });
    }

    @Override
    public int getItemCount() {
        return goals.size();
    }

    static class SavingsGoalViewHolder extends RecyclerView.ViewHolder {
        TextView tvGoalName, tvTargetDate, tvCurrentAmount, tvTargetAmount, tvPercentage;
        LinearProgressIndicator progressGoal;
        MaterialButton btnDeposit;
        ImageButton btnEditGoal, btnDeleteGoal;

        public SavingsGoalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGoalName = itemView.findViewById(R.id.tvGoalName);
            tvTargetDate = itemView.findViewById(R.id.tvTargetDate);
            tvCurrentAmount = itemView.findViewById(R.id.tvCurrentAmount);
            tvTargetAmount = itemView.findViewById(R.id.tvTargetAmount);
            tvPercentage = itemView.findViewById(R.id.tvPercentage);
            progressGoal = itemView.findViewById(R.id.progressGoal);
            btnDeposit = itemView.findViewById(R.id.btnDeposit);
            btnEditGoal = itemView.findViewById(R.id.btnEditGoal);
            btnDeleteGoal = itemView.findViewById(R.id.btnDeleteGoal);
        }
    }
}
