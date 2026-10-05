package com.example.expensewise;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expensewise.models.SavingsGoal;
import com.example.expensewise.repositories.SavingsGoalRepository;
import com.example.expensewise.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

public class AddSavingsGoalActivity extends AppCompatActivity {

    public static final String EXTRA_GOAL_ID = "extra_goal_id";

    private TextInputEditText etGoalName, etTargetAmount, etCurrentAmount, etTargetDate, etGoalDescription;
    private MaterialButton btnSaveGoal;
    private MaterialToolbar toolbar;

    private SavingsGoalRepository savingsGoalRepository;
    private SessionManager sessionManager;
    private long currentUserId = -1;
    private long goalId = -1;
    private SavingsGoal existingGoal = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_savings_goal);

        sessionManager = new SessionManager(this);
        savingsGoalRepository = new SavingsGoalRepository(this);
        currentUserId = sessionManager.getCurrentUserId();

        initViews();
        setupListeners();

        goalId = getIntent().getLongExtra(EXTRA_GOAL_ID, -1);
        if (goalId != -1) {
            toolbar.setTitle(R.string.edit_savings_goal);
            loadGoalData();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etGoalName = findViewById(R.id.etGoalName);
        etTargetAmount = findViewById(R.id.etTargetAmount);
        etCurrentAmount = findViewById(R.id.etCurrentAmount);
        etTargetDate = findViewById(R.id.etTargetDate);
        etGoalDescription = findViewById(R.id.etGoalDescription);
        btnSaveGoal = findViewById(R.id.btnSaveGoal);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupListeners() {
        etTargetDate.setOnClickListener(v -> showDatePickerDialog());

        btnSaveGoal.setOnClickListener(v -> saveSavingsGoal());
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            String dateStr = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
            etTargetDate.setText(dateStr);
        }, year, month, day);
        datePickerDialog.show();
    }

    private void loadGoalData() {
        savingsGoalRepository.getGoalById(goalId, goal -> {
            if (goal != null) {
                existingGoal = goal;
                runOnUiThread(() -> {
                    etGoalName.setText(goal.getName());
                    etTargetAmount.setText(String.valueOf(goal.getTargetAmount()));
                    etCurrentAmount.setText(String.valueOf(goal.getCurrentAmount()));
                    etTargetDate.setText(goal.getTargetDate());
                    etGoalDescription.setText(goal.getDescription());
                });
            }
        });
    }

    private void saveSavingsGoal() {
        String name = etGoalName.getText() != null ? etGoalName.getText().toString().trim() : "";
        String targetStr = etTargetAmount.getText() != null ? etTargetAmount.getText().toString().trim() : "";
        String currentStr = etCurrentAmount.getText() != null ? etCurrentAmount.getText().toString().trim() : "0";
        String targetDate = etTargetDate.getText() != null ? etTargetDate.getText().toString().trim() : "";
        String description = etGoalDescription.getText() != null ? etGoalDescription.getText().toString().trim() : "";

        if (TextUtils.isEmpty(name)) {
            etGoalName.setError("Please enter goal name");
            return;
        }
        if (TextUtils.isEmpty(targetStr)) {
            etTargetAmount.setError("Please enter target amount");
            return;
        }

        double targetAmount = Double.parseDouble(targetStr);
        double currentAmount = TextUtils.isEmpty(currentStr) ? 0.0 : Double.parseDouble(currentStr);

        if (targetAmount <= 0) {
            etTargetAmount.setError("Target amount must be greater than 0");
            return;
        }

        if (existingGoal != null) {
            existingGoal.setName(name);
            existingGoal.setTargetAmount(targetAmount);
            existingGoal.setCurrentAmount(currentAmount);
            existingGoal.setTargetDate(targetDate);
            existingGoal.setDescription(description);

            savingsGoalRepository.update(existingGoal);
            Toast.makeText(this, R.string.goal_saved_success, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            SavingsGoal newGoal = new SavingsGoal(currentUserId, name, targetAmount, currentAmount, targetDate, description);
            savingsGoalRepository.insert(newGoal, id -> {
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.goal_saved_success, Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        }
    }
}
