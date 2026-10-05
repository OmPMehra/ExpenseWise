package com.example.expensewise;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expensewise.repositories.UserRepository;
import com.example.expensewise.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

public class PinLockActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "EXTRA_MODE";
    public static final String MODE_VERIFY = "VERIFY";
    public static final String MODE_CREATE = "CREATE";

    private TextView tvPinTitle, tvPinSubtitle, tvPinError;
    private View dot1, dot2, dot3, dot4;
    private View[] dots;

    private SessionManager sessionManager;
    private UserRepository userRepository;

    private String currentMode = MODE_VERIFY;
    private StringBuilder enteredPin = new StringBuilder();
    private String firstPinAttempt = null; // For CREATE mode confirmation step

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_lock);

        sessionManager = new SessionManager(this);
        userRepository = new UserRepository(this);

        if (getIntent() != null && getIntent().hasExtra(EXTRA_MODE)) {
            currentMode = getIntent().getStringExtra(EXTRA_MODE);
        }

        initViews();
        setupModeUI();
        setupKeypad();
    }

    private void initViews() {
        tvPinTitle = findViewById(R.id.tvPinTitle);
        tvPinSubtitle = findViewById(R.id.tvPinSubtitle);
        tvPinError = findViewById(R.id.tvPinError);

        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);
        dot4 = findViewById(R.id.dot4);

        dots = new View[]{dot1, dot2, dot3, dot4};
    }

    private void setupModeUI() {
        enteredPin.setLength(0);
        firstPinAttempt = null;
        updateDotsUI();
        tvPinError.setVisibility(View.INVISIBLE);

        if (MODE_CREATE.equals(currentMode)) {
            tvPinTitle.setText(R.string.pin_title_create);
            tvPinSubtitle.setText(R.string.pin_subtitle_create);
        } else {
            tvPinTitle.setText(R.string.pin_title_verify);
            tvPinSubtitle.setText(R.string.pin_subtitle_verify);
        }
    }

    private void setupKeypad() {
        int[] numKeyIds = {
                R.id.btnKey0, R.id.btnKey1, R.id.btnKey2, R.id.btnKey3, R.id.btnKey4,
                R.id.btnKey5, R.id.btnKey6, R.id.btnKey7, R.id.btnKey8, R.id.btnKey9
        };

        for (int id : numKeyIds) {
            MaterialButton btn = findViewById(id);
            btn.setOnClickListener(v -> {
                String digit = btn.getText().toString();
                onDigitPressed(digit);
            });
        }

        MaterialButton btnClear = findViewById(R.id.btnKeyClear);
        btnClear.setOnClickListener(v -> clearPinInput());

        MaterialButton btnBackspace = findViewById(R.id.btnKeyBackspace);
        btnBackspace.setOnClickListener(v -> onBackspacePressed());
    }

    private void onDigitPressed(String digit) {
        if (enteredPin.length() < 4) {
            enteredPin.append(digit);
            updateDotsUI();
            tvPinError.setVisibility(View.INVISIBLE);

            if (enteredPin.length() == 4) {
                processCompletedPin(enteredPin.toString());
            }
        }
    }

    private void onBackspacePressed() {
        if (enteredPin.length() > 0) {
            enteredPin.deleteCharAt(enteredPin.length() - 1);
            updateDotsUI();
            tvPinError.setVisibility(View.INVISIBLE);
        }
    }

    private void clearPinInput() {
        enteredPin.setLength(0);
        updateDotsUI();
        tvPinError.setVisibility(View.INVISIBLE);
    }

    private void updateDotsUI() {
        int count = enteredPin.length();
        for (int i = 0; i < 4; i++) {
            if (i < count) {
                dots[i].setBackgroundResource(R.drawable.pin_dot_filled);
            } else {
                dots[i].setBackgroundResource(R.drawable.pin_dot_bg);
            }
        }
    }

    private void processCompletedPin(String pin) {
        if (MODE_VERIFY.equals(currentMode)) {
            String savedPin = sessionManager.getAppPin();
            if (pin.equals(savedPin)) {
                Toast.makeText(this, R.string.pin_verified_success, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                if (!isCallingActivityMain()) {
                    Intent intent = new Intent(PinLockActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                finish();
            } else {
                showError(getString(R.string.err_wrong_pin));
            }
        } else if (MODE_CREATE.equals(currentMode)) {
            if (firstPinAttempt == null) {
                // First entry in CREATE mode completed, now prompt for confirmation
                firstPinAttempt = pin;
                enteredPin.setLength(0);
                updateDotsUI();

                tvPinTitle.setText(R.string.pin_title_confirm);
                tvPinSubtitle.setText(R.string.pin_subtitle_confirm);
            } else {
                // Confirmation entry completed
                if (pin.equals(firstPinAttempt)) {
                    // PIN matched!
                    saveNewPin(pin);
                } else {
                    showError(getString(R.string.err_pin_mismatch));
                    firstPinAttempt = null;
                    tvPinTitle.setText(R.string.pin_title_create);
                    tvPinSubtitle.setText(R.string.pin_subtitle_create);
                }
            }
        }
    }

    private boolean isCallingActivityMain() {
        return getCallingActivity() != null &&
                MainActivity.class.getName().equals(getCallingActivity().getClassName());
    }

    private void saveNewPin(String pin) {
        sessionManager.setAppPin(pin);
        sessionManager.setPinEnabled(true);

        long userId = sessionManager.getCurrentUserId();
        if (userId != -1) {
            userRepository.getUserById(userId, user -> {
                if (user != null) {
                    user.setPin(pin);
                    user.setPinEnabled(true);
                    userRepository.update(user);
                }
            });
        }

        Toast.makeText(this, R.string.pin_set_success, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void showError(String message) {
        tvPinError.setText(message);
        tvPinError.setVisibility(View.VISIBLE);
        enteredPin.setLength(0);
        updateDotsUI();
    }
}
