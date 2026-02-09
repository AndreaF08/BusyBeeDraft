package com.example.busybeedraft;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.Locale;

public class PomodoroActivity extends AppCompatActivity {
    private TextView tvTimer;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 1500000; // 25 mins
    private boolean timerRunning;

    // Notification Constants
    private static final String CHANNEL_ID = "pomodoro_channel";
    private static final int NOTIFICATION_ID = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pomodoro);

        // Initialize the notification channel for Android O+
        createNotificationChannel();

        tvTimer = findViewById(R.id.tvPomodoroTimer);
        ImageButton btnStart = findViewById(R.id.btnPomodoroStart);
        ImageButton btnPause = findViewById(R.id.btnPomodoroPAuse);
        ImageButton btnReset = findViewById(R.id.btnPomodoroReset);
        Button btnTabStopwatch = findViewById(R.id.btnTabStopwatch);
        Button btnKillSwitch = findViewById(R.id.kill_switch);

        btnTabStopwatch.setOnClickListener(v -> {
            startActivity(new Intent(this, StopwatchActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            finish();
        });

        btnStart.setOnClickListener(v -> startTimer());
        btnPause.setOnClickListener(v -> pauseTimer());
        btnReset.setOnClickListener(v -> resetTimer());

        btnKillSwitch.setOnClickListener(v -> {
            pauseTimer();
            timeLeftInMillis = 1000; // 1 second
            updateText();
            startTimer();
        });

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
                    sendHeadsUpNotification(); // Trigger Notification
                }
            }.start();
            timerRunning = true;
        }
    }

    private void sendHeadsUpNotification() {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (defaultSoundUri == null) {
            defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // System icon for now
                .setContentTitle("Time's Up!")
                .setContentText("Your Pomodoro session has finished. Take a break!")
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Required for Heads-up
                .setDefaults(NotificationCompat.DEFAULT_ALL)   // Sound/Vibrate/Lights
                .setSound(defaultSoundUri)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // Check for POST_NOTIFICATIONS permission (Android 13+)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // If permission is missing, you can request it here or fail gracefully
            return;
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Pomodoro Timer Alerts",
                    NotificationManager.IMPORTANCE_HIGH // Required for Heads-up
            );
            channel.setDescription("Alerts when your focus session ends");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void pauseTimer() {
        if(countDownTimer != null) countDownTimer.cancel();
        timerRunning = false;
    }

    private void resetTimer() {
        pauseTimer();
        timeLeftInMillis = 1500000;
        updateText();
    }

    private void updateText() {
        int m = (int) (timeLeftInMillis / 1000) / 60;
        int s = (int) (timeLeftInMillis / 1000) % 60;
        tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", m, s));
    }

    private void setupBottomNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_pomodoro);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) startActivity(new Intent(this, TaskManagement.class));
            else if (id == R.id.nav_calendar) startActivity(new Intent(this, CalendarActivity.class));
            else if (id == R.id.nav_id) startActivity(new Intent(this, DashboardActivity.class));
            else if (id == R.id.nav_folders) startActivity(new Intent(this, FolderActivity.class));
            return true;
        });
    }
}