package com.example.expensewise;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expensewise.models.Budget;
import com.example.expensewise.repositories.BudgetRepository;
import com.example.expensewise.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddBudgetActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY = "EXTRA_CATEGORY";
    public static final String EXTRA_MONTH = "EXTRA_MONTH";
    public static final String EXTRA_YEAR = "EXTRA_YEAR";
    public static final String EXTRA_AMOUNT = "EXTRA_AMOUNT";

    private MaterialToolbar toolbarAddBudget;
    private TextInputLayout layoutBudgetCategory, layoutBudgetAmount;
    private AutoCompleteTextView spinnerBudgetCategory;
    private TextInputEditText etBudgetMonth, etBudgetAmount;
    private MaterialButton btnSaveBudget;

    private SessionManager sessionManager;
    private BudgetRepository budgetRepository;

    private long currentUserId = -1;
    private int selectedMonth; // 1 - 12
    private int selectedYear;  // e.g. 2026

    private final String[] budgetCategories = {"OVERALL", "Food", "Transport", "Shopping", "Bills", "Entertainment", "Healthcare", "Education", "Rent", "Travel", "Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_budget);

        sessionManager = new SessionManager(this);
        budgetRepository = new BudgetRepository(this);

        currentUserId = sessionManager.getCurrentUserId();

        Calendar now = Calendar.getInstance();
        selectedMonth = now.get(Calendar.MONTH) + 1;
        selectedYear = now.get(Calendar.YEAR);

        initViews();
        setupToolbar();
        setupCategoryDropdown();
        setupMonthPicker();

        // Check intent extras
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(EXTRA_CATEGORY)) {
                String catExtra = intent.getStringExtra(EXTRA_CATEGORY);
                if (!TextUtils.isEmpty(catExtra)) {
                    spinnerBudgetCategory.setText(catExtra, false);
                }
            }
            if (intent.hasExtra(EXTRA_MONTH)) {
                selectedMonth = intent.getIntExtra(EXTRA_MONTH, selectedMonth);
            }
            if (intent.hasExtra(EXTRA_YEAR)) {
                selectedYear = intent.getIntExtra(EXTRA_YEAR, selectedYear);
            }
            if (intent.hasExtra(EXTRA_AMOUNT)) {
                double amt = intent.getDoubleExtra(EXTRA_AMOUNT, 0);
                if (amt > 0) {
                    etBudgetAmount.setText(String.valueOf(amt));
                }
            }
        }

        updateMonthEditText();
        setupListeners();
    }

    private void initViews() {
        toolbarAddBudget = findViewById(R.id.toolbarAddBudget);
        layoutBudgetCategory = findViewById(R.id.layoutBudgetCategory);
        layoutBudgetAmount = findViewById(R.id.layoutBudgetAmount);

        spinnerBudgetCategory = findViewById(R.id.spinnerBudgetCategory);
        etBudgetMonth = findViewById(R.id.etBudgetMonth);
        etBudgetAmount = findViewById(R.id.etBudgetAmount);
        btnSaveBudget = findViewById(R.id.btnSaveBudget);

        layoutBudgetAmount.setPrefixText(sessionManager.getCurrencySymbol() + " ");
    }

    private void setupToolbar() {
        toolbarAddBudget.setNavigationOnClickListener(v -> finish());
    }

    private void setupCategoryDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, budgetCategories);
        spinnerBudgetCategory.setAdapter(adapter);
        if (TextUtils.isEmpty(spinnerBudgetCategory.getText())) {
            spinnerBudgetCategory.setText(budgetCategories[0], false);
        }
    }

    private void updateMonthEditText() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, selectedYear);
        cal.set(Calendar.MONTH, selectedMonth - 1);
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        etBudgetMonth.setText(sdf.format(cal.getTime()));
    }

    private void setupMonthPicker() {
        etBudgetMonth.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, selectedYear);
            cal.set(Calendar.MONTH, selectedMonth - 1);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedYear = year;
                selectedMonth = month + 1;
                updateMonthEditText();
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 1);

            datePickerDialog.show();
        });
    }

    private void setupListeners() {
        btnSaveBudget.setOnClickListener(v -> saveBudget());
    }

    private void saveBudget() {
        String category = spinnerBudgetCategory.getText() != null ? spinnerBudgetCategory.getText().toString().trim() : "";
        String amountStr = etBudgetAmount.getText() != null ? etBudgetAmount.getText().toString().trim() : "";

        layoutBudgetAmount.setError(null);
        layoutBudgetCategory.setError(null);

        if (TextUtils.isEmpty(category)) {
            layoutBudgetCategory.setError(getString(R.string.err_empty_category));
            return;
        }

        if (TextUtils.isEmpty(amountStr)) {
            layoutBudgetAmount.setError(getString(R.string.err_invalid_amount));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                layoutBudgetAmount.setError(getString(R.string.err_invalid_amount));
                return;
            }
        } catch (NumberFormatException e) {
            layoutBudgetAmount.setError(getString(R.string.err_invalid_amount));
            return;
        }

        budgetRepository.getBudgetForCategory(currentUserId, category, selectedMonth, selectedYear, existingBudget -> {
            if (existingBudget != null) {
                existingBudget.setAmount(amount);
                budgetRepository.update(existingBudget);
            } else {
                Budget newBudget = new Budget(currentUserId, category, amount, selectedMonth, selectedYear);
                budgetRepository.insert(newBudget, null);
            }

            runOnUiThread(() -> {
                Toast.makeText(AddBudgetActivity.this, R.string.budget_saved_success, Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
}
