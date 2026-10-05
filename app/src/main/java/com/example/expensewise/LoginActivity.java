package com.example.expensewise;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expensewise.models.User;
import com.example.expensewise.repositories.UserRepository;
import com.example.expensewise.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private ProgressBar pbLoginLoading;
    private TextView tvRegisterLink;

    private UserRepository userRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userRepository = new UserRepository(this);
        sessionManager = new SessionManager(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        pbLoginLoading = findViewById(R.id.pbLoginLoading);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> performLogin());

        tvRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        TextWatcher clearErrorWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilEmail.setError(null);
                tilPassword.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etEmail.addTextChangedListener(clearErrorWatcher);
        etPassword.addTextChangedListener(clearErrorWatcher);
    }

    private void performLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        boolean isValid = true;

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.err_empty_email));
            isValid = false;
        } else if (email.contains("@") && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.err_invalid_email));
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.err_empty_password));
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        setLoading(true);

        userRepository.login(email, password, user -> {
            runOnUiThread(() -> {
                setLoading(false);
                if (user != null) {
                    onLoginSuccess(user);
                } else {
                    tilPassword.setError(getString(R.string.err_invalid_credentials));
                    Toast.makeText(LoginActivity.this, R.string.err_invalid_credentials, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void onLoginSuccess(User user) {
        sessionManager.createLoginSession(user.getId(), user.getName(), user.getEmail());
        sessionManager.setPinEnabled(user.isPinEnabled());
        sessionManager.setAppPin(user.getPin() != null ? user.getPin() : "");
        sessionManager.setCurrencySymbol(user.getCurrencySymbol() != null ? user.getCurrencySymbol() : "₹");

        Toast.makeText(this, "Welcome back, " + user.getName() + "!", Toast.LENGTH_SHORT).show();

        Intent intent;
        if (sessionManager.isPinEnabled() && !TextUtils.isEmpty(sessionManager.getAppPin())) {
            intent = new Intent(LoginActivity.this, PinLockActivity.class);
            intent.putExtra(PinLockActivity.EXTRA_MODE, PinLockActivity.MODE_VERIFY);
        } else {
            intent = new Intent(LoginActivity.this, MainActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean isLoading) {
        pbLoginLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!isLoading);
        etEmail.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);
    }
}
