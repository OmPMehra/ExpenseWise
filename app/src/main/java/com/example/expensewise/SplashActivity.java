package com.example.expensewise;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expensewise.utils.DemoDataGenerator;
import com.example.expensewise.utils.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY_MS = 1500;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sessionManager = new SessionManager(this);

        // Populate default categories if missing
        DemoDataGenerator demoDataGenerator = new DemoDataGenerator(this);
        demoDataGenerator.populateDefaultCategoriesIfEmpty(null);

        new Handler(Looper.getMainLooper()).postDelayed(this::checkNavigation, SPLASH_DELAY_MS);
    }

    private void checkNavigation() {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        if (sessionManager.isLoggedIn()) {
            if (sessionManager.isPinEnabled()) {
                Intent intent = new Intent(SplashActivity.this, PinLockActivity.class);
                intent.putExtra(PinLockActivity.EXTRA_MODE, PinLockActivity.MODE_VERIFY);
                startActivity(intent);
            } else {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
            }
        } else {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
        }
        finish();
    }
}
