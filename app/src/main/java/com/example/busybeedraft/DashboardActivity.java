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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity {

    private final String[] colors = {"#F6F0D7", "#C5D89D", "#FFCDC9", "#F9DFDF", "#FFF2C6", "#FFE1AF", "#F5FAE1", "#FBF3B9"};
    private final int[] avatarList = {R.drawable.avatar1, R.drawable.avatar2, R.drawable.avatar3, R.drawable.avatar4};
    private final String[] yearLevels = {"Elementary", "High School", "Senior High School", "College", "Graduate"};
    private final int[] stickerList = {R.drawable.stickers1, R.drawable.stickers2, R.drawable.stickers3, R.drawable.stickers4, R.drawable.stickers5, R.drawable.stickers6, R.drawable.stickers7, R.drawable.stickers8, R.drawable.stickers9, R.drawable.stickers10};

    private EditText etInputName, etInputBday, etInputSchool;
    private TextView tvLabelName, tvLabelBday, tvGreeting, tvLabelYear, tvCurrentDate, tvClubName, tvLabelSchool;
    private Spinner spnYear;
    private ImageView ivSelectedSticker, ivDisplayAvatar;
    private ConstraintLayout idCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Link UI
        tvGreeting = findViewById(R.id.tvGreeting);
        tvCurrentDate = findViewById(R.id.tvCurrentDate);
        idCard = findViewById(R.id.idCard);
        ivDisplayAvatar = findViewById(R.id.ivDisplayAvatar);
        ivSelectedSticker = findViewById(R.id.ivSelectedSticker);
        tvLabelName = findViewById(R.id.tvLabelName);
        tvLabelBday = findViewById(R.id.tvLabelBday);
        tvLabelYear = findViewById(R.id.tvLabelYear);
        tvClubName = findViewById(R.id.tvClubName);
        tvLabelSchool = findViewById(R.id.tvLabelSchool);
        etInputName = findViewById(R.id.etInputName);
        etInputBday = findViewById(R.id.etInputBday);
        etInputSchool = findViewById(R.id.etInputSchool);
        spnYear = findViewById(R.id.spnYear);

        // Set Dynamic Date
        tvCurrentDate.setText(new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(new Date()));

        // Setup Spinner
        spnYear.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, yearLevels));

        // Listeners
        etInputBday.setOnClickListener(v -> showDatePicker(etInputBday));
        findViewById(R.id.btnClearSticker).setOnClickListener(v -> ivSelectedSticker.setVisibility(View.GONE));
        findViewById(R.id.btnSettings).setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        // FIX: SAVE ID BUTTON LOGIC
        findViewById(R.id.btnSaveID).setOnClickListener(v -> {
            String name = etInputName.getText().toString();
            String school = etInputSchool.getText().toString();
            String bday = etInputBday.getText().toString();

            if (name.isEmpty() || school.isEmpty() || bday.isEmpty()) {
                Toast.makeText(this, "Please fill in all details!", Toast.LENGTH_SHORT).show();
            } else {
                SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", MODE_PRIVATE).edit();
                editor.putString("user_name", name);
                editor.putString("user_school", school);
                editor.putString("user_bday", bday);
                editor.apply();
                Toast.makeText(this, "ID Details Saved!", Toast.LENGTH_SHORT).show();
            }
        });

        setupTextListeners();
        populateTrays(findViewById(R.id.avatarTray), findViewById(R.id.stickerTray), findViewById(R.id.colorPalette));
        setupBottomNavigation();
    }

    private void setupTextListeners() {
        etInputName.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int b, int c) {
                String input = s.toString().trim();
                if (!input.isEmpty()) {
                    tvGreeting.setText("Hello, " + input.split(" ")[0] + "...");
                    tvLabelName.setText("NAME\n" + input.toUpperCase());
                } else {
                    tvGreeting.setText("Hello...");
                    tvLabelName.setText("NAME\n");
                }
            }
            @Override public void beforeTextChanged(CharSequence s, int x, int y, int z) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        etInputSchool.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int b, int c) {
                String input = s.toString().trim();
                String display = input.isEmpty() ? "SCHOOL NAME" : input.toUpperCase();
                tvClubName.setText(display);
                tvLabelSchool.setText("SCHOOL\n" + display);
            }
            @Override public void beforeTextChanged(CharSequence s, int x, int y, int z) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        etInputBday.addTextChangedListener(new TextWatcher() {
            @Override public void onTextChanged(CharSequence s, int start, int b, int c) {
                tvLabelBday.setText("BIRTHDAY\n" + (s.length() == 0 ? "00/00/0000" : s.toString()));
            }
            @Override public void beforeTextChanged(CharSequence s, int x, int y, int z) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        spnYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                tvLabelYear.setText("YEAR LEVEL\n" + yearLevels[pos].toUpperCase());
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    private void populateTrays(LinearLayout aT, LinearLayout sT, LinearLayout cP) {
        for (int res : avatarList) {
            ImageView img = new ImageView(this);
            img.setLayoutParams(new LinearLayout.LayoutParams(150, 150));
            img.setImageResource(res);
            img.setOnClickListener(v -> ivDisplayAvatar.setImageResource(res));
            aT.addView(img);
        }
        for (String color : colors) {
            View circle = new View(this);
            circle.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
            circle.setBackgroundResource(R.drawable.rounded_outline);
            circle.getBackground().setColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_ATOP);
            circle.setOnClickListener(v -> idCard.getBackground().setColorFilter(Color.parseColor(color), PorterDuff.Mode.SRC_ATOP));
            cP.addView(circle);
        }
        for (int res : stickerList) {
            ImageView img = new ImageView(this);
            img.setLayoutParams(new LinearLayout.LayoutParams(150, 150));
            img.setImageResource(res);
            img.setOnClickListener(v -> {
                ivSelectedSticker.setImageResource(res);
                ivSelectedSticker.setVisibility(View.VISIBLE);
            });
            sT.addView(img);
        }
    }

    private void showDatePicker(EditText bdayIn) {
        final Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) -> bdayIn.setText(String.format("%02d/%02d/%04d", (m + 1), d, y)), c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setupBottomNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                Intent i = new Intent(this, TaskManagement.class);
                i.putExtra("COURSE_NAME", "General");
                startActivity(i);
            }
            return true;
        });
    }
}