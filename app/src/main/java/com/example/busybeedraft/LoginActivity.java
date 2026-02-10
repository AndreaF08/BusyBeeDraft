package com.example.busybeedraft;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class LoginActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 102;
    private static final String PERMISSION_POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText etEmail = findViewById(R.id.etEmailLogin);
        EditText etPassword = findViewById(R.id.etPasswordLogin);
        AppCompatButton btnLogin = findViewById(R.id.btnLogin);
        TextView tvCreateAccount = findViewById(R.id.tvCreateAccount);

        btnLogin.setOnClickListener(v -> {
            String emailInput = etEmail.getText().toString().trim();
            String passInput = etPassword.getText().toString().trim();

            if (emailInput.isEmpty() || passInput.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("UserDB", MODE_PRIVATE);
            String storedPass = prefs.getString(emailInput + "_password", null);

            if (storedPass != null && storedPass.equals(passInput)) {
                // SAVE SESSION: This is required to separate course data by user
                SharedPreferences sessionPrefs = getSharedPreferences("UserSession", MODE_PRIVATE);
                sessionPrefs.edit().putString("current_user_email", emailInput).apply();

                String userName = prefs.getString(emailInput + "_name", "User");
                Toast.makeText(this, "Welcome back, " + userName + "!", Toast.LENGTH_SHORT).show();
                showLoginNotification();

                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    startActivity(new Intent(LoginActivity.this, TaskManagement.class));
                    finish();
                }, 500);
            } else {
                Toast.makeText(this, "Invalid email or password.", Toast.LENGTH_LONG).show();
            }
        });

        tvCreateAccount.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });
    }

    private void showLoginNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, PERMISSION_POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{PERMISSION_POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE);
                return;
            }
        }
        displayLoginNotification();
    }

    private void displayLoginNotification() {
        try {
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            String channelId = "login_channel";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel existingChannel = notificationManager.getNotificationChannel(channelId);
                if (existingChannel == null) {
                    NotificationChannel channel = new NotificationChannel(
                            channelId, "Login Notifications", NotificationManager.IMPORTANCE_HIGH);
                    notificationManager.createNotificationChannel(channel);
                }
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Welcome Back!")
                    .setContentText("You have successfully logged in.")
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setAutoCancel(true);

            notificationManager.notify(2, builder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            displayLoginNotification();
        }
    }
}