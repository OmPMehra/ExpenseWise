package com.example.expensewise;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.expensewise.models.Category;
import com.example.expensewise.models.TransactionItem;
import com.example.expensewise.repositories.CategoryRepository;
import com.example.expensewise.repositories.TransactionRepository;
import com.example.expensewise.utils.FinancialUtils;
import com.example.expensewise.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {

    public static final String EXTRA_TRANSACTION_ID = "EXTRA_TRANSACTION_ID";
    public static final String EXTRA_TYPE = "type";

    private MaterialToolbar toolbarAddTransaction;
    private MaterialButtonToggleGroup toggleGroupType;
    private TextInputLayout layoutAmount, layoutCategory;
    private TextInputEditText etAmount, etDescription, etDate, etTime, etNotes;
    private AutoCompleteTextView spinnerCategory, spinnerPaymentMethod;
    private TextView btnAddCustomCategory;
    private MaterialButton btnSaveTransaction;

    private SessionManager sessionManager;
    private TransactionRepository transactionRepository;
    private CategoryRepository categoryRepository;

    private long currentUserId = -1;
    private long editTransactionId = -1;
    private TransactionItem existingTransaction = null;
    private String selectedType = "EXPENSE"; // EXPENSE or INCOME

    private final String[] defaultPaymentMethods = {"Cash", "UPI", "Debit Card", "Credit Card", "Bank Transfer", "Other"};

    private final String[] expenseCategories = {"Food", "Transport", "Shopping", "Bills", "Entertainment", "Education", "Healthcare", "Rent", "Travel", "Other"};
    private final String[] incomeCategories = {"Salary", "Freelance", "Business", "Allowance", "Interest", "Gift", "Other"};

    private final List<String> currentCategoriesList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        sessionManager = new SessionManager(this);
        transactionRepository = new TransactionRepository(this);
        categoryRepository = new CategoryRepository(this);

        currentUserId = sessionManager.getCurrentUserId();

        initViews();
        setupToolbar();
        setupPaymentMethods();
        setupDateAndTimePickers();

        // Check Intent extras
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(EXTRA_TRANSACTION_ID)) {
                editTransactionId = intent.getLongExtra(EXTRA_TRANSACTION_ID, -1);
            }
            if (intent.hasExtra(EXTRA_TYPE)) {
                String typeExtra = intent.getStringExtra(EXTRA_TYPE);
                if (typeExtra != null && !typeExtra.isEmpty()) {
                    selectedType = typeExtra.toUpperCase();
                }
            }
        }

        setupTypeToggle();

        if (editTransactionId != -1) {
            toolbarAddTransaction.setTitle(R.string.title_edit_transaction);
            btnSaveTransaction.setText(R.string.btn_save_transaction);
            loadExistingTransactionData(editTransactionId);
        } else {
            toolbarAddTransaction.setTitle(R.string.title_add_transaction);
            // Default date and time
            etDate.setText(FinancialUtils.getCurrentDateFormatted());
            etTime.setText(FinancialUtils.getCurrentTimeFormatted());
            updateCategoryDropdown();
        }

        setupListeners();
    }

    private void initViews() {
        toolbarAddTransaction = findViewById(R.id.toolbarAddTransaction);
        toggleGroupType = findViewById(R.id.toggleGroupType);

        layoutAmount = findViewById(R.id.layoutAmount);
        layoutCategory = findViewById(R.id.layoutCategory);

        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        etNotes = findViewById(R.id.etNotes);

        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerPaymentMethod = findViewById(R.id.spinnerPaymentMethod);
        btnAddCustomCategory = findViewById(R.id.btnAddCustomCategory);
        btnSaveTransaction = findViewById(R.id.btnSaveTransaction);

        layoutAmount.setPrefixText(sessionManager.getCurrencySymbol() + " ");
    }

    private void setupToolbar() {
        toolbarAddTransaction.setNavigationOnClickListener(v -> finish());
    }

    private void setupPaymentMethods() {
        ArrayAdapter<String> pmAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, defaultPaymentMethods);
        spinnerPaymentMethod.setAdapter(pmAdapter);
        if (defaultPaymentMethods.length > 0) {
            spinnerPaymentMethod.setText(defaultPaymentMethods[0], false);
        }
    }

    private void setupTypeToggle() {
        MaterialButton btnToggleExpense = findViewById(R.id.btnToggleExpense);
        MaterialButton btnToggleIncome = findViewById(R.id.btnToggleIncome);

        Runnable updateToggleVisuals = () -> {
            if ("INCOME".equalsIgnoreCase(selectedType)) {
                if (btnToggleIncome != null) {
                    btnToggleIncome.setBackgroundColor(ContextCompat.getColor(this, R.color.secondary));
                    btnToggleIncome.setTextColor(ContextCompat.getColor(this, R.color.white));
                    btnToggleIncome.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
                }
                if (btnToggleExpense != null) {
                    btnToggleExpense.setBackgroundColor(Color.TRANSPARENT);
                    btnToggleExpense.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                    btnToggleExpense.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.error)));
                }
            } else {
                if (btnToggleExpense != null) {
                    btnToggleExpense.setBackgroundColor(ContextCompat.getColor(this, R.color.error));
                    btnToggleExpense.setTextColor(ContextCompat.getColor(this, R.color.white));
                    btnToggleExpense.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white)));
                }
                if (btnToggleIncome != null) {
                    btnToggleIncome.setBackgroundColor(Color.TRANSPARENT);
                    btnToggleIncome.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                    btnToggleIncome.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.secondary)));
                }
            }
        };

        if ("INCOME".equalsIgnoreCase(selectedType)) {
            toggleGroupType.check(R.id.btnToggleIncome);
        } else {
            toggleGroupType.check(R.id.btnToggleExpense);
            selectedType = "EXPENSE";
        }
        updateToggleVisuals.run();

        toggleGroupType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnToggleIncome) {
                    selectedType = "INCOME";
                } else {
                    selectedType = "EXPENSE";
                }
                updateToggleVisuals.run();
                updateCategoryDropdown();
            }
        });
    }

    private void updateCategoryDropdown() {
        currentCategoriesList.clear();
        if ("INCOME".equalsIgnoreCase(selectedType)) {
            currentCategoriesList.addAll(Arrays.asList(incomeCategories));
        } else {
            currentCategoriesList.addAll(Arrays.asList(expenseCategories));
        }

        categoryRepository.getAllCategoriesSync(categories -> {
            if (categories != null) {
                for (Category cat : categories) {
                    if (selectedType.equalsIgnoreCase(cat.getType()) && !currentCategoriesList.contains(cat.getName())) {
                        currentCategoriesList.add(cat.getName());
                    }
                }
            }
            runOnUiThread(() -> {
                ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(AddTransactionActivity.this, android.R.layout.simple_dropdown_item_1line, currentCategoriesList);
                spinnerCategory.setAdapter(categoryAdapter);
                if (!currentCategoriesList.isEmpty() && TextUtils.isEmpty(spinnerCategory.getText())) {
                    spinnerCategory.setText(currentCategoriesList.get(0), false);
                }
            });
        });
    }

    private void setupDateAndTimePickers() {
        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            String selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year1, month1 + 1, dayOfMonth);
            etDate.setText(selectedDate);
        }, year, month, day);

        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            Calendar timeCal = Calendar.getInstance();
            timeCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
            timeCal.set(Calendar.MINUTE, minute1);
            etTime.setText(FinancialUtils.formatTime(timeCal.getTimeInMillis()));
        }, hour, minute, false);

        timePickerDialog.show();
    }

    private void setupListeners() {
        btnAddCustomCategory.setOnClickListener(v -> showAddCustomCategoryDialog());
        btnSaveTransaction.setOnClickListener(v -> saveTransaction());
    }

    private void showAddCustomCategoryDialog() {
        TextInputLayout textInputLayout = new TextInputLayout(this);
        textInputLayout.setHint("Category Name");
        textInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        textInputLayout.setBoxStrokeColor(ContextCompat.getColor(this, R.color.primary));
        textInputLayout.setBoxStrokeWidth((int) (1.5 * getResources().getDisplayMetrics().density));
        textInputLayout.setBoxBackgroundColor(ContextCompat.getColor(this, R.color.input_bg_color));

        TextInputEditText input = new TextInputEditText(textInputLayout.getContext());
        input.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        input.setMinHeight((int) (48 * getResources().getDisplayMetrics().density));
        textInputLayout.addView(input);

        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        FrameLayout container = new FrameLayout(this);
        container.setPadding(padding, padding / 2, padding, padding / 2);
        container.addView(textInputLayout);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Add Custom Category")
                .setView(container)
                .setPositiveButton("Add", (dialog, which) -> {
                    String categoryName = input.getText() != null ? input.getText().toString().trim() : "";
                    if (!categoryName.isEmpty()) {
                        Category customCat = new Category(categoryName, selectedType, "ic_other", "#4F46E5", true);
                        categoryRepository.insert(customCat, id -> runOnUiThread(() -> {
                            Toast.makeText(AddTransactionActivity.this, "Category added!", Toast.LENGTH_SHORT).show();
                            updateCategoryDropdown();
                            spinnerCategory.setText(categoryName, false);
                        }));
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void loadExistingTransactionData(long transactionId) {
        transactionRepository.getTransactionById(transactionId, transaction -> {
            if (transaction != null) {
                existingTransaction = transaction;
                runOnUiThread(() -> {
                    selectedType = transaction.getType();
                    if ("INCOME".equalsIgnoreCase(selectedType)) {
                        toggleGroupType.check(R.id.btnToggleIncome);
                    } else {
                        toggleGroupType.check(R.id.btnToggleExpense);
                    }

                    etAmount.setText(String.valueOf(transaction.getAmount()));
                    etDescription.setText(transaction.getDescription());
                    etDate.setText(transaction.getDate());
                    etTime.setText(transaction.getTime());
                    etNotes.setText(transaction.getNotes());

                    if (!TextUtils.isEmpty(transaction.getPaymentMethod())) {
                        spinnerPaymentMethod.setText(transaction.getPaymentMethod(), false);
                    }

                    updateCategoryDropdown();
                    spinnerCategory.setText(transaction.getCategory(), false);
                });
            }
        });
    }

    private void saveTransaction() {
        String amountStr = etAmount.getText() != null ? etAmount.getText().toString().trim() : "";
        String category = spinnerCategory.getText() != null ? spinnerCategory.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String dateVal = etDate.getText() != null ? etDate.getText().toString().trim() : "";
        String timeVal = etTime.getText() != null ? etTime.getText().toString().trim() : "";
        String paymentMethod = spinnerPaymentMethod.getText() != null ? spinnerPaymentMethod.getText().toString().trim() : "";
        String notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";

        layoutAmount.setError(null);
        layoutCategory.setError(null);

        if (TextUtils.isEmpty(amountStr)) {
            layoutAmount.setError(getString(R.string.err_invalid_amount));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                layoutAmount.setError(getString(R.string.err_invalid_amount));
                return;
            }
        } catch (NumberFormatException e) {
            layoutAmount.setError(getString(R.string.err_invalid_amount));
            return;
        }

        if (TextUtils.isEmpty(category)) {
            layoutCategory.setError(getString(R.string.err_empty_category));
            return;
        }

        if (TextUtils.isEmpty(dateVal)) {
            dateVal = FinancialUtils.getCurrentDateFormatted();
        }

        if (TextUtils.isEmpty(timeVal)) {
            timeVal = FinancialUtils.getCurrentTimeFormatted();
        }

        if (existingTransaction != null) {
            // Update mode
            existingTransaction.setType(selectedType);
            existingTransaction.setAmount(amount);
            existingTransaction.setCategory(category);
            existingTransaction.setDescription(description);
            existingTransaction.setDate(dateVal);
            existingTransaction.setTime(timeVal);
            existingTransaction.setPaymentMethod(paymentMethod);
            existingTransaction.setNotes(notes);

            transactionRepository.update(existingTransaction);
            Toast.makeText(this, "Transaction updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            // Insert mode
            TransactionItem newTransaction = new TransactionItem(
                    currentUserId, selectedType, amount, category, description, dateVal, timeVal, paymentMethod, notes
            );

            transactionRepository.insert(newTransaction, id -> runOnUiThread(() -> {
                Toast.makeText(AddTransactionActivity.this, "Transaction saved successfully", Toast.LENGTH_SHORT).show();
                finish();
            }));
        }
    }
}