package com.matmax.signage;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Simulate loading and check pairing status
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isDevicePaired()) {
                startActivity(new Intent(this, MainActivity.class));
            } else {
                startActivity(new Intent(this, PairingActivity.class));
            }
            finish();
        }, 1500); // 1.5s splash
    }

    private boolean isDevicePaired() {
        return DeviceManager.getInstance(this).isPaired();
    }
} 