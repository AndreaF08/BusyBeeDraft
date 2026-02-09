package com.example.busybeedraft;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NewEventActivity extends AppCompatActivity {
    EditText etTitle;
    TextView tvStart, tvEnd, tvTime, tvHeaderDate;
    Spinner spinnerCourse;
    String selectedColor = "#42A5F5";
    int editIndex = -1;
    boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        etTitle = findViewById(R.id.etEventTitle);
        tvStart = findViewById(R.id.tvStartDate);
        tvEnd = findViewById(R.id.tvEndDate);
        tvTime = findViewById(R.id.tvEventTime);
        tvHeaderDate = findViewById(R.id.tvHeaderDate);
        spinnerCourse = findViewById(R.id.spinnerCourse);

        setupCourseSpinner();
        setupColorClicks();

        String passedDate = getIntent().getStringExtra("SELECTED_DATE");
        if (passedDate != null) {
            tvStart.setText(passedDate);
            tvHeaderDate.setText(passedDate);
        }

        isEditMode = getIntent().getBooleanExtra("EDIT_MODE", false);
        if (isEditMode) {
            CalendarEvent event = (CalendarEvent) getIntent().getSerializableExtra("EVENT_DATA");
            editIndex = getIntent().getIntExtra("EVENT_INDEX", -1);
            if (event != null) fillFields(event);
        }

        tvStart.setOnClickListener(v -> showDatePicker(tvStart));
        tvEnd.setOnClickListener(v -> showDatePicker(tvEnd));
        tvTime.setOnClickListener(v -> showScrollableTimePicker());
        findViewById(R.id.btnCancel).setOnClickListener(v -> finish());
        findViewById(R.id.btnCreateEvent).setOnClickListener(v -> validateAndSave());
    }

    private void fillFields(CalendarEvent e) {
        etTitle.setText(e.title);
        tvStart.setText(e.startDate);
        tvEnd.setText(e.endDate);
        tvTime.setText(e.time);
        selectedColor = e.color;
        ((Button)findViewById(R.id.btnCreateEvent)).setText("Update");
    }

    private void setupColorClicks() {
        int[] ids = {R.id.colorBlue, R.id.colorPink, R.id.colorYellow, R.id.colorGreen, R.id.colorPurple};
        String[] codes = {"#42A5F5", "#F48FB1", "#FFF59D", "#A5D6A7", "#CE93D8"};
        for (int i = 0; i < ids.length; i++) {
            final String code = codes[i];
            findViewById(ids[i]).setOnClickListener(v -> {
                selectedColor = code;
                Toast.makeText(this, "Color Tagged", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setupCourseSpinner() {
        List<String> courses = new ArrayList<>();
        SharedPreferences fPrefs = getSharedPreferences("FolderPrefs", MODE_PRIVATE);
        try {
            JSONArray arr = new JSONArray(fPrefs.getString("folders", "[]"));
            for (int i = 0; i < arr.length(); i++) courses.add(arr.getJSONObject(i).getString("name"));
        } catch (Exception ignored) {}
        if (courses.isEmpty()) courses.add("General");
        ArrayAdapter<String> adp = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourse.setAdapter(adp);
    }

    private void showScrollableTimePicker() {
        Calendar c = Calendar.getInstance();
        TimePickerDialog tpd = new TimePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                (view, h, m) -> {
                    String amPm = (h < 12) ? "AM" : "PM";
                    int hour = (h == 0 || h == 12) ? 12 : h % 12;
                    tvTime.setText(String.format("%02d:%02d %s", hour, m, amPm));
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
        tpd.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        tpd.show();
    }

    private void showDatePicker(TextView tv) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, y, m, d) -> tv.setText(d + "/" + (m+1) + "/" + y),
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void validateAndSave() {
        if (etTitle.getText().toString().trim().isEmpty() || tvTime.getText().toString().contains("Select")) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        saveEvent();
    }

    private void saveEvent() {
        SharedPreferences prefs = getSharedPreferences("CalendarPrefs", MODE_PRIVATE);
        try {
            JSONArray array = new JSONArray(prefs.getString("all_events", "[]"));
            JSONObject obj = new JSONObject();
            obj.put("title", etTitle.getText().toString());
            obj.put("date", tvStart.getText().toString());
            obj.put("endDate", tvEnd.getText().toString());
            obj.put("time", tvTime.getText().toString());
            obj.put("course", spinnerCourse.getSelectedItem().toString());
            obj.put("color", selectedColor);

            if (isEditMode && editIndex != -1) {
                // Find the original event in the full list to update it correctly
                // If editIndex is based on the filtered calendar list,
                // you might need to find by Title/Time instead.
                array.put(editIndex, obj);
            } else {
                array.put(obj);
            }

            prefs.edit().putString("all_events", array.toString()).apply();
            finish();
        } catch (Exception ignored) {}
    }
}