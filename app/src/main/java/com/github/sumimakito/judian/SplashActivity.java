package com.github.sumimakito.judian;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    private long lastTs = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void onResume() {
        super.onResume();
        lastTs = System.currentTimeMillis();
        final long ts = lastTs;
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (ts != lastTs) return;
            startActivity(new Intent(this, LoginActivity.class));
            this.finish();
        }, 2000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        lastTs = -1;
    }
}
