package com.example.busybeedraft;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.Locale;

public class StopwatchActivity extends AppCompatActivity {
    private TextView tvTime;
    private long startTime = 0L, timeInMs = 0L, swapBuff = 0L, updateTime = 0L;
    private Handler handler = new Handler();
    private boolean isRunning = false;

    private Runnable updateTimer = new Runnable() {
        public void run() {
            timeInMs = SystemClock.uptimeMillis() - startTime;
            updateTime = swapBuff + timeInMs;
            int secs = (int) (updateTime / 1000);
            int mins = secs / 60;
            secs %= 60;
            int ms = (int) (updateTime % 1000) / 10;
            tvTime.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", mins, secs, ms));
            handler.postDelayed(this, 10);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stopwatch);

        // View Binding
        tvTime = findViewById(R.id.tvStopwatchTime);
        ImageButton btnStart = findViewById(R.id.btnStopwatchStart);
        ImageButton btnPause = findViewById(R.id.btnStopwatchPause);
        ImageButton btnReset = findViewById(R.id.btnStopwatchReset);
        Button btnTabPomodoro = findViewById(R.id.btnTabPomodoro);

        // Tab Switching - returns to Pomodoro
        btnTabPomodoro.setOnClickListener(v -> {
            startActivity(new Intent(this, PomodoroActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            finish();
        });

        // Stopwatch Controls
        btnStart.setOnClickListener(v -> {
            if (!isRunning) {
                startTime = SystemClock.uptimeMillis();
                handler.postDelayed(updateTimer, 0);
                isRunning = true;
            }
        });
        btnPause.setOnClickListener(v -> {
            if (isRunning) {
                swapBuff += timeInMs;
                handler.removeCallbacks(updateTimer);
                isRunning = false;
            }
        });
        btnReset.setOnClickListener(v -> {
            startTime = 0L; timeInMs = 0L; swapBuff = 0L; updateTime = 0L;
            tvTime.setText("00:00:00");
            if (isRunning) {
                handler.removeCallbacks(updateTimer);
                isRunning = false;
            }
        });

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);

        // Ensure the Timer icon is highlighted even in Stopwatch mode
        nav.setSelectedItemId(R.id.nav_pomodoro);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, TaskManagement.class));
            } else if (id == R.id.nav_calendar) {
                startActivity(new Intent(this, CalendarActivity.class));
            } else if (id == R.id.nav_id) {
                startActivity(new Intent(this, DashboardActivity.class));
            } else if (id == R.id.nav_folders) {
                startActivity(new Intent(this, FolderActivity.class));
            }
            return true;
        });
    }
}