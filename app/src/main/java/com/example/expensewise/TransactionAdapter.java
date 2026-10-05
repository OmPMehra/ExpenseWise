package com.example.expensewise;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensewise.models.TransactionItem;
import com.example.expensewise.utils.CategoryUtils;
import com.example.expensewise.utils.FinancialUtils;
import com.example.expensewise.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    public interface OnTransactionClickListener {
        void onTransactionClick(TransactionItem transaction);
        void onTransactionLongClick(TransactionItem transaction);
    }

    private final Context context;
    private List<TransactionItem> transactionsList;
    private final OnTransactionClickListener listener;
    private String currencySymbol;

    public TransactionAdapter(Context context, OnTransactionClickListener listener) {
        this.context = context;
        this.transactionsList = new ArrayList<>();
        this.listener = listener;
        SessionManager sessionManager = new SessionManager(context);
        this.currencySymbol = sessionManager.getCurrencySymbol();
    }

    public void setCurrencySymbol(String symbol) {
        this.currencySymbol = symbol;
        notifyDataSetChanged();
    }

    public void setTransactions(List<TransactionItem> transactions) {
        this.transactionsList = transactions != null ? transactions : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionItem item = transactionsList.get(position);
        holder.bind(item, listener, currencySymbol);
    }

    @Override
    public int getItemCount() {
        return transactionsList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvCategoryInitials;
        private final TextView tvCategoryName;
        private final TextView tvDescription;
        private final TextView tvDateTime;
        private final TextView tvPaymentMethodTag;
        private final TextView tvAmount;
        private final TextView tvTypeTag;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryInitials = itemView.findViewById(R.id.tvCategoryInitials);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvPaymentMethodTag = itemView.findViewById(R.id.tvPaymentMethodTag);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvTypeTag = itemView.findViewById(R.id.tvTypeTag);
        }

        public void bind(TransactionItem item, OnTransactionClickListener listener, String currencySymbol) {
            tvCategoryName.setText(item.getCategory() != null ? item.getCategory() : "");
            String initials = CategoryUtils.getCategoryInitials(item.getCategory());
            tvCategoryInitials.setText(initials);
            tvCategoryInitials.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12f);
            tvCategoryInitials.setTypeface(null, android.graphics.Typeface.BOLD);
            tvCategoryInitials.setSingleLine(true);
            tvCategoryInitials.setMaxLines(1);
            tvCategoryInitials.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            tvCategoryInitials.setSelected(true);
            tvCategoryInitials.setGravity(android.view.Gravity.CENTER);
            tvCategoryInitials.setIncludeFontPadding(false);

            if (TextUtils.isEmpty(item.getDescription())) {
                tvDescription.setVisibility(View.GONE);
            } else {
                tvDescription.setVisibility(View.VISIBLE);
                tvDescription.setText(item.getDescription());
            }

            String dateStr = item.getDate() != null ? item.getDate() : "";
            String timeStr = item.getTime() != null ? item.getTime() : "";
            String dateTimeCombined = (dateStr + " " + timeStr).trim();
            tvDateTime.setText(dateTimeCombined.isEmpty() ? "N/A" : dateTimeCombined);

            if (!TextUtils.isEmpty(item.getPaymentMethod())) {
                tvPaymentMethodTag.setVisibility(View.VISIBLE);
                tvPaymentMethodTag.setText(item.getPaymentMethod());
            } else {
                tvPaymentMethodTag.setVisibility(View.GONE);
            }

            boolean isIncome = "INCOME".equalsIgnoreCase(item.getType());
            double amount = Math.abs(item.getAmount());
            String formattedAmount = FinancialUtils.formatCurrency(amount, currencySymbol);

            if (isIncome) {
                String displayText = "+ " + formattedAmount;
                tvAmount.setText(displayText);
                tvAmount.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.success_green));
                tvTypeTag.setText(R.string.filter_income);
                tvCategoryInitials.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.success_green));
            } else {
                String displayText = "- " + formattedAmount;
                tvAmount.setText(displayText);
                tvAmount.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.error_red));
                tvTypeTag.setText(R.string.filter_expense);
                tvCategoryInitials.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.error_red));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTransactionClick(item);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onTransactionLongClick(item);
                    return true;
                }
                return false;
            });
        }
    }
}
