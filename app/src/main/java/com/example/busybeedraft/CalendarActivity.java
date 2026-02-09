package com.example.busybeedraft;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

public class CalendarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        TextView tvMonthYear = findViewById(R.id.tvMonthYear);
        CalendarView calendarView = findViewById(R.id.calendarView);
        MaterialButton btnEvent = findViewById(R.id.btnEvent);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String date = dayOfMonth + "/" + (month + 1) + "/" + year;
            tvMonthYear.setText(date);
        });

        btnEvent.setOnClickListener(v -> {
            Intent intent = new Intent(this, NewEventActivity.class);
            intent.putExtra("SELECTED_DATE", tvMonthYear.getText().toString());
            startActivity(intent);
        });

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_calendar); //
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) startActivity(new Intent(this, TaskManagement.class));
            else if (id == R.id.nav_pomodoro) startActivity(new Intent(this, PomodoroActivity.class));
            else if (id == R.id.nav_folders) startActivity(new Intent(this, FolderActivity.class));
            else if (id == R.id.nav_id) startActivity(new Intent(this, DashboardActivity.class));
            return true;
        });
    }
}