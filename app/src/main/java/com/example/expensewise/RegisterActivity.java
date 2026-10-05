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
import com.example.expensewise.utils.DemoDataGenerator;
import com.example.expensewise.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private ProgressBar pbRegisterLoading;
    private TextView tvLoginLink;

    private UserRepository userRepository;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        userRepository = new UserRepository(this);
        sessionManager = new SessionManager(this);

        initViews();
        setupListeners();
    }

    private void initViews() {
        tilName = findViewById(R.id.tilName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);
        pbRegisterLoading = findViewById(R.id.pbRegisterLoading);
        tvLoginLink = findViewById(R.id.tvLoginLink);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> performRegister());

        tvLoginLink.setOnClickListener(v -> {
            finish(); // Go back to LoginActivity
        });

        TextWatcher clearErrorWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilName.setError(null);
                tilEmail.setError(null);
                tilPassword.setError(null);
                tilConfirmPassword.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etName.addTextChangedListener(clearErrorWatcher);
        etEmail.addTextChangedListener(clearErrorWatcher);
        etPassword.addTextChangedListener(clearErrorWatcher);
        etConfirmPassword.addTextChangedListener(clearErrorWatcher);
    }

    private void performRegister() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        boolean isValid = true;

        if (TextUtils.isEmpty(name)) {
            tilName.setError(getString(R.string.err_empty_name));
            isValid = false;
        }

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.err_empty_email));
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.err_invalid_email));
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.err_empty_password));
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError(getString(R.string.err_short_password));
            isValid = false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.err_empty_password));
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.err_passwords_dont_match));
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        setLoading(true);

        userRepository.getUserByEmail(email, existingUser -> {
            if (existingUser != null) {
                runOnUiThread(() -> {
                    setLoading(false);
                    tilEmail.setError(getString(R.string.err_email_exists));
                });
            } else {
                User user = new User(name, email, password);
                userRepository.insert(user, userId -> runOnUiThread(() -> {
                    setLoading(false);
                    if (userId != null && userId > 0) {
                        onRegistrationSuccess(userId, name, email);
                    } else {
                        Toast.makeText(RegisterActivity.this, "Failed to create account. Try again.", Toast.LENGTH_SHORT).show();
                    }
                }));
            }
        });
    }

    private void onRegistrationSuccess(long userId, String name, String email) {
        sessionManager.createLoginSession(userId, name, email);

        DemoDataGenerator demoDataGenerator = new DemoDataGenerator(this);
        demoDataGenerator.generateDemoDataForUser(userId, false, null);

        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean isLoading) {
        pbRegisterLoading.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!isLoading);
        etName.setEnabled(!isLoading);
        etEmail.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);
        etConfirmPassword.setEnabled(!isLoading);
    }
}
