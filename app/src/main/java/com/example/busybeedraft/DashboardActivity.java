package com.example.busybeedraft;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.Calendar;

public class DashboardActivity extends AppCompatActivity {

    private final String[] colors = {"#F6F0D7", "#C5D89D", "#FFCDC9", "#F9DFDF", "#FFF2C6", "#FFE1AF", "#F5FAE1", "#FBF3B9"};
    private final int[] avatarList = {R.drawable.avatar1, R.drawable.avatar2, R.drawable.avatar3, R.drawable.avatar4};
    private final String[] yearLevels = {"1st Year", "2nd Year", "3rd Year", "4th Year", "Graduate"};

    private EditText etInputName, etInputBday;
    private TextView tvLabelName, tvLabelBday, tvGreeting, tvLabelYear;
    private Spinner spnYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        tvGreeting = findViewById(R.id.tvGreeting);
        ConstraintLayout idCard = findViewById(R.id.idCard);
        ImageView ivDisplayAvatar = findViewById(R.id.ivDisplayAvatar);
        tvLabelName = findViewById(R.id.tvLabelName);
        tvLabelBday = findViewById(R.id.tvLabelBday);
        tvLabelYear = findViewById(R.id.tvLabelYear);
        etInputName = findViewById(R.id.etInputName);
        etInputBday = findViewById(R.id.etInputBday);
        spnYear = findViewById(R.id.spnYear);

        ImageButton btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, yearLevels);
        spnYear.setAdapter(adapter);
        etInputBday.setOnClickListener(v -> showDatePicker(etInputBday));

        setupTextListeners();
        populateTrays(findViewById(R.id.avatarTray), findViewById(R.id.stickerTray), findViewById(R.id.colorPalette), ivDisplayAvatar, idCard);

        findViewById(R.id.btnSaveID).setOnClickListener(v -> {
            SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
            editor.putString("user_name", etInputName.getText().toString());
            editor.putString("user_bday", etInputBday.getText().toString());
            editor.apply();
            Toast.makeText(this, "ID Saved!", Toast.LENGTH_SHORT).show();
        });

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_id);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                Intent intent = new Intent(this, TaskManagement.class);
                intent.putExtra("COURSE_NAME", "General");
                startActivity(intent);
            }
            else if (id == R.id.nav_calendar) startActivity(new Intent(this, CalendarActivity.class));
            else if (id == R.id.nav_pomodoro) startActivity(new Intent(this, PomodoroActivity.class));
            else if (id == R.id.nav_folders) startActivity(new Intent(this, FolderActivity.class));
            return true;
        });
    }

    private void setupTextListeners() {
        etInputName.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();
                tvGreeting.setText("Hello, " + (input.isEmpty() ? "Ethan" : input) + "...");
                tvLabelName.setText("NAME\n" + (input.isEmpty() ? "ETHAN" : input.toUpperCase()));
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        etInputBday.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvLabelBday.setText("BIRTHDAY\n" + (s.length() == 0 ? "00/00/0000" : s.toString()));
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        spnYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                tvLabelYear.setText("YEAR LEVEL\n" + yearLevels[pos].toUpperCase());
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    private void showDatePicker(EditText bdayIn) {
        final Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) -> {
            bdayIn.setText(String.format("%02d/%02d/%04d", (m + 1), d, y));
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void populateTrays(LinearLayout aT, LinearLayout sT, LinearLayout cP, ImageView ivA, View card) {
        for (int res : avatarList) {
            ImageView img = new ImageView(this);
            img.setLayoutParams(new LinearLayout.LayoutParams(150, 150));
            img.setImageResource(res);
            img.setOnClickListener(v -> ivA.setImageResource(res));
            aT.addView(img);
        }
        for (String color : colors) {
            View circle = new View(this);
            circle.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
            circle.setBackgroundResource(R.drawable.rounded_outline);
            circle.getBackground().setColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_ATOP);
            circle.setOnClickListener(v -> card.getBackground().setColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_ATOP));
            cP.addView(circle);
        }
    }
}