package com.example.busybeedraft;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.Locale;

public class PomodoroActivity extends AppCompatActivity {
    private TextView tvTimer;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 1500000; // 25 mins
    private boolean timerRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomodoro);

        // Initialize Views from your XML
        tvTimer = findViewById(R.id.tvPomodoroTimer);
        ImageButton btnStart = findViewById(R.id.btnPomodoroStart);
        ImageButton btnPause = findViewById(R.id.btnPomodoroPAuse); // Kept your exact XML ID
        ImageButton btnReset = findViewById(R.id.btnPomodoroReset);
        Button btnTabStopwatch = findViewById(R.id.btnTabStopwatch);

        // Tab Switching logic
        btnTabStopwatch.setOnClickListener(v -> {
            startActivity(new Intent(this, StopwatchActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            finish();
        });

        // Timer Control Listeners
        btnStart.setOnClickListener(v -> startTimer());
        btnPause.setOnClickListener(v -> pauseTimer());
        btnReset.setOnClickListener(v -> resetTimer());

        // Initialize Bottom Navigation
        setupBottomNavigation();
    }

    private void startTimer() {
        if (!timerRunning) {
            countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
                @Override
                public void onTick(long ms) {
                    timeLeftInMillis = ms;
                    updateText();
                }
                @Override
                public void onFinish() {
                    timerRunning = false;
                }
            }.start();
            timerRunning = true;
        }
    }

    private void pauseTimer() {
        if(countDownTimer != null) countDownTimer.cancel();
        timerRunning = false;
    }

    private void resetTimer() {
        timeLeftInMillis = 1500000;
        updateText();
        pauseTimer();
    }

    private void updateText() {
        int m = (int) (timeLeftInMillis / 1000) / 60;
        int s = (int) (timeLeftInMillis / 1000) % 60;
        tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", m, s));
    }

    private void setupBottomNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);

        // Highlight the Pomodoro/Timer icon
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