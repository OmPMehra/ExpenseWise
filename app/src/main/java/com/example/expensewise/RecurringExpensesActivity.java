package com.example.expensewise;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensewise.models.RecurringExpense;
import com.example.expensewise.models.TransactionItem;
import com.example.expensewise.repositories.RecurringExpenseRepository;
import com.example.expensewise.repositories.TransactionRepository;
import com.example.expensewise.utils.FinancialUtils;
import com.example.expensewise.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class RecurringExpensesActivity extends AppCompatActivity implements RecurringExpenseAdapter.OnRecurringActionListener {

    private RecyclerView rvRecurringExpenses;
    private LinearLayout layoutEmptyRecurring;
    private ExtendedFloatingActionButton fabAddRecurring;
    private View btnRecurringEmpty;
    private MaterialToolbar toolbar;

    private RecurringExpenseRepository recurringExpenseRepository;
    private TransactionRepository transactionRepository;
    private RecurringExpenseAdapter recurringExpenseAdapter;

    private long currentUserId = -1;
    private String currencySymbol = "₹";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recurring_expenses);

        SessionManager sessionManager = new SessionManager(this);
        recurringExpenseRepository = new RecurringExpenseRepository(this);
        transactionRepository = new TransactionRepository(this);
        currentUserId = sessionManager.getCurrentUserId();
        currencySymbol = sessionManager.getCurrencySymbol();

        initViews();
        setupRecyclerView();
        setupListeners();
        observeRecurringExpenses();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvRecurringExpenses = findViewById(R.id.rvRecurringExpenses);
        layoutEmptyRecurring = findViewById(R.id.layoutEmptyRecurring);
        fabAddRecurring = findViewById(R.id.fabAddRecurring);
        btnRecurringEmpty = findViewById(R.id.btnRecurringEmpty);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        recurringExpenseAdapter = new RecurringExpenseAdapter(this, this, currencySymbol);
        rvRecurringExpenses.setLayoutManager(new LinearLayoutManager(this));
        rvRecurringExpenses.setAdapter(recurringExpenseAdapter);
    }

    private void setupListeners() {
        View.OnClickListener openAdd = v -> {
            Intent intent = new Intent(RecurringExpensesActivity.this, AddRecurringExpenseActivity.class);
            startActivity(intent);
        };

        fabAddRecurring.setOnClickListener(openAdd);
        btnRecurringEmpty.setOnClickListener(openAdd);
    }

    private void observeRecurringExpenses() {
        if (currentUserId == -1) return;

        recurringExpenseRepository.getRecurringExpensesForUser(currentUserId).observe(this, list -> {
            if (list == null || list.isEmpty()) {
                rvRecurringExpenses.setVisibility(View.GONE);
                layoutEmptyRecurring.setVisibility(View.VISIBLE);
            } else {
                rvRecurringExpenses.setVisibility(View.VISIBLE);
                layoutEmptyRecurring.setVisibility(View.GONE);
                recurringExpenseAdapter.setExpenses(list);
            }
        });
    }

    @Override
    public void onPostClick(RecurringExpense expense) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Post Transaction")
                .setMessage("Post \"" + expense.getName() + "\" (" + FinancialUtils.formatCurrency(expense.getAmount(), currencySymbol) + ") as an expense transaction now?")
                .setPositiveButton("Post", (dialog, which) -> {
                    TransactionItem transactionItem = new TransactionItem(
                            currentUserId,
                            "EXPENSE",
                            expense.getAmount(),
                            expense.getCategory(),
                            expense.getName(),
                            FinancialUtils.getCurrentDateFormatted(),
                            FinancialUtils.getCurrentTimeFormatted(),
                            "Online / Auto",
                            "Recurring Expense Payment"
                    );
                    transactionRepository.insert(transactionItem, id -> runOnUiThread(() ->
                            Toast.makeText(this, R.string.posted_success, Toast.LENGTH_SHORT).show()
                    ));
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onEditClick(RecurringExpense expense) {
        Intent intent = new Intent(RecurringExpensesActivity.this, AddRecurringExpenseActivity.class);
        intent.putExtra(AddRecurringExpenseActivity.EXTRA_RECURRING_ID, expense.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(RecurringExpense expense) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Recurring Expense")
                .setMessage("Are you sure you want to delete \"" + expense.getName() + "\"?")
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    recurringExpenseRepository.delete(expense);
                    Toast.makeText(this, R.string.recurring_deleted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}
