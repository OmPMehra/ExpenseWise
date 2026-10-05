package com.example.expensewise;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.expensewise.models.SavingsGoal;
import com.example.expensewise.repositories.SavingsGoalRepository;
import com.example.expensewise.utils.FinancialUtils;
import com.example.expensewise.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class SavingsGoalsActivity extends AppCompatActivity implements SavingsGoalAdapter.OnGoalActionListener {

    private RecyclerView rvSavingsGoals;
    private LinearLayout layoutEmptyGoals;
    private ExtendedFloatingActionButton fabAddGoal;
    private View btnAddGoalEmpty;
    private MaterialToolbar toolbar;

    private SavingsGoalRepository savingsGoalRepository;
    private SessionManager sessionManager;
    private SavingsGoalAdapter adapter;
    private long currentUserId;
    private String currencySymbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savings_goals);

        sessionManager = new SessionManager(this);
        currentUserId = sessionManager.getCurrentUserId();
        currencySymbol = sessionManager.getCurrencySymbol();

        savingsGoalRepository = new SavingsGoalRepository(this);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGoals();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        rvSavingsGoals = findViewById(R.id.rvSavingsGoals);
        layoutEmptyGoals = findViewById(R.id.layoutEmptyGoals);
        fabAddGoal = findViewById(R.id.fabAddGoal);
        btnAddGoalEmpty = findViewById(R.id.btnAddGoalEmpty);
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new SavingsGoalAdapter(this, this, currencySymbol);
        rvSavingsGoals.setLayoutManager(new LinearLayoutManager(this));
        rvSavingsGoals.setAdapter(adapter);
    }

    private void setupListeners() {
        fabAddGoal.setOnClickListener(v -> startActivity(new Intent(SavingsGoalsActivity.this, AddSavingsGoalActivity.class)));
        btnAddGoalEmpty.setOnClickListener(v -> startActivity(new Intent(SavingsGoalsActivity.this, AddSavingsGoalActivity.class)));
    }

    private void loadGoals() {
        savingsGoalRepository.getSavingsGoalsForUser(currentUserId).observe(this, goals -> {
            if (goals == null || goals.isEmpty()) {
                rvSavingsGoals.setVisibility(View.GONE);
                layoutEmptyGoals.setVisibility(View.VISIBLE);
            } else {
                rvSavingsGoals.setVisibility(View.VISIBLE);
                layoutEmptyGoals.setVisibility(View.GONE);
                adapter.setGoals(goals);
            }
        });
    }

    @Override
    public void onDepositClick(SavingsGoal goal) {
        showDepositDialog(goal);
    }

    @Override
    public void onEditClick(SavingsGoal goal) {
        // Option to edit goal if needed
    }

    @Override
    public void onDeleteClick(SavingsGoal goal) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.btn_delete)
                .setMessage("Are you sure you want to delete this goal?")
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    savingsGoalRepository.delete(goal);
                    Toast.makeText(this, "Savings goal deleted", Toast.LENGTH_SHORT).show();
                    loadGoals();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void showDepositDialog(SavingsGoal goal) {
        TextInputLayout textInputLayout = new TextInputLayout(this);
        textInputLayout.setHint("Deposit Amount");
        textInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        textInputLayout.setBoxStrokeColor(ContextCompat.getColor(this, R.color.primary));
        textInputLayout.setBoxStrokeWidth((int) (1.5 * getResources().getDisplayMetrics().density));
        textInputLayout.setBoxBackgroundColor(ContextCompat.getColor(this, R.color.input_bg_color));

        TextInputEditText etDepositAmount = new TextInputEditText(textInputLayout.getContext());
        etDepositAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        etDepositAmount.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
        etDepositAmount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        etDepositAmount.setMinHeight((int) (48 * getResources().getDisplayMetrics().density));
        textInputLayout.addView(etDepositAmount);

        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        FrameLayout container = new FrameLayout(this);
        container.setPadding(padding, padding / 2, padding, padding / 2);
        container.addView(textInputLayout);

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.deposit_dialog_title)
                .setMessage("Goal: " + goal.getName() + "\nCurrent: " + FinancialUtils.formatCurrency(goal.getCurrentAmount(), currencySymbol))
                .setView(container)
                .setPositiveButton("Deposit", (dialog, which) -> {
                    String str = etDepositAmount.getText() != null ? etDepositAmount.getText().toString().trim() : "";
                    if (!str.isEmpty()) {
                        try {
                            double amount = Double.parseDouble(str);
                            if (amount > 0) {
                                goal.setCurrentAmount(goal.getCurrentAmount() + amount);
                                savingsGoalRepository.update(goal);
                                Toast.makeText(this, "Successfully deposited " + FinancialUtils.formatCurrency(amount, currencySymbol), Toast.LENGTH_SHORT).show();
                                loadGoals();
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}