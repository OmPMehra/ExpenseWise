package com.example.expensewise;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expensewise.models.RecurringExpense;
import com.example.expensewise.repositories.RecurringExpenseRepository;
import com.example.expensewise.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

public class AddRecurringExpenseActivity extends AppCompatActivity {

    public static final String EXTRA_RECURRING_ID = "extra_recurring_id";

    private TextInputEditText etRecurringName, etRecurringAmount, etNextDueDate;
    private AutoCompleteTextView autoCompleteCategory, autoCompleteFrequency;
    private SwitchMaterial switchRecurringNotification;
    private MaterialButton btnSaveRecurring;
    private MaterialToolbar toolbar;

    private RecurringExpenseRepository recurringExpenseRepository;
    private long currentUserId = -1;
    private long recurringId = -1;
    private RecurringExpense existingExpense = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recurring_expense);

        SessionManager sessionManager = new SessionManager(this);
        recurringExpenseRepository = new RecurringExpenseRepository(this);
        currentUserId = sessionManager.getCurrentUserId();

        initViews();
        setupDropdowns();
        setupListeners();

        recurringId = getIntent().getLongExtra(EXTRA_RECURRING_ID, -1);
        if (recurringId != -1) {
            toolbar.setTitle(R.string.edit_recurring_expense);
            loadRecurringData();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etRecurringName = findViewById(R.id.etRecurringName);
        etRecurringAmount = findViewById(R.id.etRecurringAmount);
        autoCompleteCategory = findViewById(R.id.autoCompleteCategory);
        autoCompleteFrequency = findViewById(R.id.autoCompleteFrequency);
        etNextDueDate = findViewById(R.id.etNextDueDate);
        switchRecurringNotification = findViewById(R.id.switchRecurringNotification);
        btnSaveRecurring = findViewById(R.id.btnSaveRecurring);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupDropdowns() {
        String[] categories = {"Bills & Utilities", "Subscription", "Rent", "Insurance", "Education", "Other"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
        autoCompleteCategory.setAdapter(catAdapter);
        autoCompleteCategory.setText(categories[0], false);

        String[] frequencies = {"Daily", "Weekly", "Monthly", "Yearly"};
        ArrayAdapter<String> freqAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, frequencies);
        autoCompleteFrequency.setAdapter(freqAdapter);
        autoCompleteFrequency.setText(frequencies[2], false); // Default Monthly
    }

    private void setupListeners() {
        etNextDueDate.setOnClickListener(v -> showDatePickerDialog());
        btnSaveRecurring.setOnClickListener(v -> saveRecurringExpense());
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String dateStr = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
            etNextDueDate.setText(dateStr);
        }, year, month, day);
        datePickerDialog.show();
    }

    private void loadRecurringData() {
        // Since DAO doesn't have direct getById, let's observe or query via live data or add helper method.
        // Wait, let's check RecurringExpenseDao or query live data / repository.
        // Or we can add getRecurringById in repository/dao if needed, or query list.
        recurringExpenseRepository.getRecurringExpensesForUser(currentUserId).observe(this, list -> {
            if (list != null) {
                for (RecurringExpense e : list) {
                    if (e.getId() == recurringId) {
                        existingExpense = e;
                        etRecurringName.setText(e.getName());
                        etRecurringAmount.setText(String.valueOf(e.getAmount()));
                        autoCompleteCategory.setText(e.getCategory(), false);
                        autoCompleteFrequency.setText(e.getFrequency(), false);
                        etNextDueDate.setText(e.getNextDueDate());
                        switchRecurringNotification.setChecked(e.isNotificationEnabled());
                        break;
                    }
                }
            }
        });
    }

    private void saveRecurringExpense() {
        String name = etRecurringName.getText() != null ? etRecurringName.getText().toString().trim() : "";
        String amountStr = etRecurringAmount.getText() != null ? etRecurringAmount.getText().toString().trim() : "";
        String category = autoCompleteCategory.getText() != null ? autoCompleteCategory.getText().toString().trim() : "Bills";
        String frequency = autoCompleteFrequency.getText() != null ? autoCompleteFrequency.getText().toString().trim() : "Monthly";
        String nextDueDate = etNextDueDate.getText() != null ? etNextDueDate.getText().toString().trim() : "";
        boolean isNotificationEnabled = switchRecurringNotification.isChecked();

        if (TextUtils.isEmpty(name)) {
            etRecurringName.setError("Please enter name");
            return;
        }
        if (TextUtils.isEmpty(amountStr)) {
            etRecurringAmount.setError("Please enter amount");
            return;
        }

        double amount = Double.parseDouble(amountStr);
        if (amount <= 0) {
            etRecurringAmount.setError("Amount must be greater than 0");
            return;
        }

        if (existingExpense != null) {
            existingExpense.setName(name);
            existingExpense.setAmount(amount);
            existingExpense.setCategory(category);
            existingExpense.setFrequency(frequency);
            existingExpense.setNextDueDate(nextDueDate);
            existingExpense.setNotificationEnabled(isNotificationEnabled);

            recurringExpenseRepository.update(existingExpense);
            Toast.makeText(this, R.string.recurring_saved_success, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            RecurringExpense newExpense = new RecurringExpense(currentUserId, name, amount, category, frequency, nextDueDate, isNotificationEnabled);
            recurringExpenseRepository.insert(newExpense, id -> runOnUiThread(() -> {
                Toast.makeText(this, R.string.recurring_saved_success, Toast.LENGTH_SHORT).show();
                finish();
            }));
        }
    }
}
