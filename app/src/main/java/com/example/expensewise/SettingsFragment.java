package com.example.expensewise;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.expensewise.models.User;
import com.example.expensewise.repositories.UserRepository;
import com.example.expensewise.utils.DemoDataGenerator;
import com.example.expensewise.utils.FinancialUtils;
import com.example.expensewise.utils.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsFragment extends Fragment {

    private TextView tvAvatarInitials, tvProfileName, tvProfileEmail, tvProfileCreatedDate, tvSelectedCurrency;
    private SwitchMaterial switchPinLock, switchNotifications;
    private View btnChangePin, layoutCurrencySelection, layoutSavingsGoals, layoutRecurringExpenses, btnLoadDemoData, btnLogout;

    private SessionManager sessionManager;
    private UserRepository userRepository;
    private User currentUser;

    private boolean isSwitchProgrammaticChange = false;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getContext() == null) return;

        sessionManager = new SessionManager(getContext());
        userRepository = new UserRepository(getContext());

        initViews(view);
        loadUserData();
        setupListeners();
    }

    private void initViews(View view) {
        tvAvatarInitials = view.findViewById(R.id.tvAvatarInitials);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvProfileCreatedDate = view.findViewById(R.id.tvProfileCreatedDate);
        tvSelectedCurrency = view.findViewById(R.id.tvSelectedCurrency);

        switchPinLock = view.findViewById(R.id.switchPinLock);
        switchNotifications = view.findViewById(R.id.switchNotifications);

        btnChangePin = view.findViewById(R.id.btnChangePin);
        layoutCurrencySelection = view.findViewById(R.id.layoutCurrencySelection);
        layoutSavingsGoals = view.findViewById(R.id.layoutSavingsGoals);
        layoutRecurringExpenses = view.findViewById(R.id.layoutRecurringExpenses);
        btnLoadDemoData = view.findViewById(R.id.btnLoadDemoData);
        btnLogout = view.findViewById(R.id.btnLogout);
    }

    private void loadUserData() {
        long userId = sessionManager.getCurrentUserId();

        // Set session fallback data
        tvProfileName.setText(sessionManager.getCurrentUserName());
        tvProfileEmail.setText(sessionManager.getCurrentUserEmail());
        updateInitials(sessionManager.getCurrentUserName());

        updateCurrencyDisplay(sessionManager.getCurrencySymbol());

        isSwitchProgrammaticChange = true;
        switchPinLock.setChecked(sessionManager.isPinEnabled());
        switchNotifications.setChecked(sessionManager.isNotificationsEnabled());
        isSwitchProgrammaticChange = false;

        if (userId != -1) {
            userRepository.getUserByIdLiveData(userId).observe(getViewLifecycleOwner(), user -> {
                if (user != null) {
                    currentUser = user;
                    tvProfileName.setText(user.getName());
                    tvProfileEmail.setText(user.getEmail());
                    updateInitials(user.getName());

                    String dateStr = FinancialUtils.formatDate(user.getCreatedAt());
                    tvProfileCreatedDate.setText(getString(R.string.account_created, dateStr));

                    updateCurrencyDisplay(user.getCurrencySymbol());

                    isSwitchProgrammaticChange = true;
                    switchPinLock.setChecked(user.isPinEnabled());
                    isSwitchProgrammaticChange = false;
                }
            });
        }
    }

    private void updateInitials(String name) {
        if (TextUtils.isEmpty(name)) {
            tvAvatarInitials.setText("EW");
            return;
        }
        String[] parts = name.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        if (parts.length > 0 && !parts[0].isEmpty()) {
            initials.append(parts[0].charAt(0));
        }
        if (parts.length > 1 && !parts[1].isEmpty()) {
            initials.append(parts[1].charAt(0));
        }
        tvAvatarInitials.setText(initials.toString().toUpperCase());
    }

    private void updateCurrencyDisplay(String symbol) {
        if (symbol == null || symbol.trim().isEmpty()) {
            symbol = "₹";
        }
        String displayText;
        switch (symbol) {
            case "$":
                displayText = "$ (USD)";
                break;
            case "€":
                displayText = "€ (EUR)";
                break;
            case "£":
                displayText = "£ (GBP)";
                break;
            case "¥":
                displayText = "¥ (JPY)";
                break;
            case "A$":
                displayText = "A$ (AUD)";
                break;
            case "C$":
                displayText = "C$ (CAD)";
                break;
            case "₹":
            default:
                displayText = "₹ (INR)";
                break;
        }
        tvSelectedCurrency.setText(displayText);
    }

    private void setupListeners() {
        switchPinLock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isSwitchProgrammaticChange) return;

            if (isChecked) {
                if (TextUtils.isEmpty(sessionManager.getAppPin())) {
                    // Prompt user to set a PIN
                    isSwitchProgrammaticChange = true;
                    switchPinLock.setChecked(false);
                    isSwitchProgrammaticChange = false;

                    Intent intent = new Intent(getContext(), PinLockActivity.class);
                    intent.putExtra(PinLockActivity.EXTRA_MODE, PinLockActivity.MODE_CREATE);
                    startActivity(intent);
                } else {
                    updatePinLockState(true);
                }
            } else {
                updatePinLockState(false);
            }
        });

        btnChangePin.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), PinLockActivity.class);
            intent.putExtra(PinLockActivity.EXTRA_MODE, PinLockActivity.MODE_CREATE);
            startActivity(intent);
        });

        layoutCurrencySelection.setOnClickListener(v -> showCurrencySelectionDialog());

        layoutSavingsGoals.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SavingsGoalsActivity.class);
            startActivity(intent);
        });

        layoutRecurringExpenses.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), RecurringExpensesActivity.class);
            startActivity(intent);
        });

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isSwitchProgrammaticChange) return;
            sessionManager.setNotificationsEnabled(isChecked);
        });

        btnLoadDemoData.setOnClickListener(v -> loadDemoData());

        btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());
    }

    private void updatePinLockState(boolean enabled) {
        sessionManager.setPinEnabled(enabled);
        if (currentUser != null) {
            currentUser.setPinEnabled(enabled);
            userRepository.update(currentUser);
        }
        Toast.makeText(getContext(), enabled ? "PIN Lock Enabled" : "PIN Lock Disabled", Toast.LENGTH_SHORT).show();
    }

    private void showCurrencySelectionDialog() {
        if (getContext() == null) return;

        final String[] currencySymbols = {"₹", "$", "€", "£", "¥", "A$", "C$"};
        final String[] currencyNames = {
                "₹ - INR (Indian Rupee)",
                "$ - USD (US Dollar)",
                "€ - EUR (Euro)",
                "£ - GBP (British Pound)",
                "¥ - JPY (Japanese Yen)",
                "A$ - AUD (Australian Dollar)",
                "C$ - CAD (Canadian Dollar)"
        };

        String currentSymbol = sessionManager.getCurrencySymbol();
        int selectedIndex = 0;
        for (int i = 0; i < currencySymbols.length; i++) {
            if (currencySymbols[i].equals(currentSymbol)) {
                selectedIndex = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(getContext())
                .setTitle(R.string.select_currency)
                .setSingleChoiceItems(currencyNames, selectedIndex, (dialog, which) -> {
                    String symbol = currencySymbols[which];
                    sessionManager.setCurrencySymbol(symbol);
                    if (currentUser != null) {
                        currentUser.setCurrencySymbol(symbol);
                        userRepository.update(currentUser);
                    }
                    updateCurrencyDisplay(symbol);
                    Toast.makeText(getContext(), "Currency updated to " + symbol, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void loadDemoData() {
        if (getContext() == null) return;

        long userId = sessionManager.getCurrentUserId();
        if (userId == -1) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        DemoDataGenerator generator = new DemoDataGenerator(getContext());
        generator.seedDemoData(userId, success -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(getContext(), R.string.demo_data_success, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), R.string.demo_data_failed, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void showLogoutConfirmationDialog() {
        if (getContext() == null) return;

        new MaterialAlertDialogBuilder(getContext())
                .setTitle(R.string.logout_confirm_title)
                .setMessage(R.string.logout_confirm_msg)
                .setPositiveButton(R.string.yes, (dialog, which) -> performLogout())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void performLogout() {
        sessionManager.logoutUser();

        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
