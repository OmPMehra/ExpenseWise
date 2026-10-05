package com.example.expensewise;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.expensewise.models.TransactionItem;
import com.example.expensewise.repositories.TransactionRepository;
import com.example.expensewise.utils.FinancialUtils;
import com.example.expensewise.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class TransactionDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_TRANSACTION_ID = "EXTRA_TRANSACTION_ID";

    private MaterialToolbar toolbarDetails;
    private ImageView ivTypeIconDetails;
    private TextView tvAmountDetails, tvCategoryDetails, tvTypeTagDetails;
    private TextView tvDescriptionDetails, tvDateTimeDetails, tvPaymentMethodDetails, tvNotesDetails;
    private MaterialButton btnEditTransaction, btnDeleteTransaction;

    private TransactionRepository transactionRepository;

    private long transactionId = -1;
    private TransactionItem currentTransaction = null;
    private String currencySymbol = "₹";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_details);

        transactionRepository = new TransactionRepository(this);
        SessionManager sessionManager = new SessionManager(this);
        currencySymbol = sessionManager.getCurrencySymbol();

        initViews();
        setupToolbar();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_TRANSACTION_ID)) {
            transactionId = intent.getLongExtra(EXTRA_TRANSACTION_ID, -1);
        }

        if (transactionId == -1) {
            Toast.makeText(this, "Transaction not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (transactionId != -1) {
            loadTransactionDetails();
        }
    }

    private void initViews() {
        toolbarDetails = findViewById(R.id.toolbarDetails);
        ivTypeIconDetails = findViewById(R.id.ivTypeIconDetails);
        tvAmountDetails = findViewById(R.id.tvAmountDetails);
        tvCategoryDetails = findViewById(R.id.tvCategoryDetails);
        tvTypeTagDetails = findViewById(R.id.tvTypeTagDetails);

        tvDescriptionDetails = findViewById(R.id.tvDescriptionDetails);
        tvDateTimeDetails = findViewById(R.id.tvDateTimeDetails);
        tvPaymentMethodDetails = findViewById(R.id.tvPaymentMethodDetails);
        tvNotesDetails = findViewById(R.id.tvNotesDetails);

        btnEditTransaction = findViewById(R.id.btnEditTransaction);
        btnDeleteTransaction = findViewById(R.id.btnDeleteTransaction);
    }

    private void setupToolbar() {
        toolbarDetails.setNavigationOnClickListener(v -> finish());
    }

    private void loadTransactionDetails() {
        transactionRepository.getTransactionById(transactionId, item -> {
            if (item != null) {
                currentTransaction = item;
                runOnUiThread(() -> populateUI(item));
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(TransactionDetailsActivity.this, "Transaction no longer exists", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        });
    }

    private void populateUI(TransactionItem item) {
        boolean isIncome = "INCOME".equalsIgnoreCase(item.getType());
        String formattedAmount = FinancialUtils.formatCurrency(item.getAmount(), currencySymbol);

        if (isIncome) {
            String displayText = "+ " + formattedAmount;
            tvAmountDetails.setText(displayText);
            tvAmountDetails.setTextColor(ContextCompat.getColor(this, R.color.success));
            tvTypeTagDetails.setText(R.string.filter_income);
            ivTypeIconDetails.setImageResource(R.drawable.ic_income);
            ivTypeIconDetails.setColorFilter(ContextCompat.getColor(this, R.color.success));
        } else {
            String displayText = "- " + formattedAmount;
            tvAmountDetails.setText(displayText);
            tvAmountDetails.setTextColor(ContextCompat.getColor(this, R.color.error));
            tvTypeTagDetails.setText(R.string.filter_expense);
            ivTypeIconDetails.setImageResource(R.drawable.ic_expense);
            ivTypeIconDetails.setColorFilter(ContextCompat.getColor(this, R.color.error));
        }

        tvCategoryDetails.setText(item.getCategory());

        if (TextUtils.isEmpty(item.getDescription())) {
            tvDescriptionDetails.setText("N/A");
        } else {
            tvDescriptionDetails.setText(item.getDescription());
        }

        String dateStr = item.getDate() != null ? item.getDate() : "";
        String timeStr = item.getTime() != null ? item.getTime() : "";
        String dateTimeCombined = (dateStr + " " + timeStr).trim();
        tvDateTimeDetails.setText(dateTimeCombined.isEmpty() ? "N/A" : dateTimeCombined);

        if (TextUtils.isEmpty(item.getPaymentMethod())) {
            tvPaymentMethodDetails.setText("N/A");
        } else {
            tvPaymentMethodDetails.setText(item.getPaymentMethod());
        }

        if (TextUtils.isEmpty(item.getNotes())) {
            tvNotesDetails.setText("N/A");
        } else {
            tvNotesDetails.setText(item.getNotes());
        }
    }

    private void setupListeners() {
        btnEditTransaction.setOnClickListener(v -> {
            Intent intent = new Intent(TransactionDetailsActivity.this, AddTransactionActivity.class);
            intent.putExtra(AddTransactionActivity.EXTRA_TRANSACTION_ID, transactionId);
            startActivity(intent);
        });

        btnDeleteTransaction.setOnClickListener(v -> confirmDelete());
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_confirm_title)
                .setMessage(R.string.delete_confirm_msg)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    if (currentTransaction != null) {
                        transactionRepository.delete(currentTransaction);
                        Toast.makeText(TransactionDetailsActivity.this, "Transaction deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}
