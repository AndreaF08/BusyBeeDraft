package com.example.busybeedraft;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NewEventActivity extends AppCompatActivity {
    LinearLayout courseSelectorLayout;
    Button btnCourseEvent, btnCreateEvent;
    EditText etEventTitle, etStartTime, etEndTime;
    TextView tvDateDisplay;
    Spinner spinnerCourse;
    boolean isCourseEvent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);

        courseSelectorLayout = findViewById(R.id.courseSelectorLayout);
        btnCourseEvent = findViewById(R.id.btnCourseEvent);
        btnCreateEvent = findViewById(R.id.btnCreateEvent);
        etEventTitle = findViewById(R.id.etEventTitle);
        etStartTime = findViewById(R.id.etStartTime);
        etEndTime = findViewById(R.id.etEndTime);
        tvDateDisplay = findViewById(R.id.tvDateDisplay);
        spinnerCourse = findViewById(R.id.spinnerCourse);

        String date = getIntent().getStringExtra("SELECTED_DATE");
        if (date != null) tvDateDisplay.setText(date);

        setupCourseSpinner();

        btnCourseEvent.setOnClickListener(v -> {
            isCourseEvent = !isCourseEvent;
            courseSelectorLayout.setVisibility(isCourseEvent ? View.VISIBLE : View.GONE);
            btnCourseEvent.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(isCourseEvent ? "#FFD600" : "#D3D3D3")));
        });

        etStartTime.setOnClickListener(v -> showTimePicker(etStartTime));
        etEndTime.setOnClickListener(v -> showTimePicker(etEndTime));

        etEventTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int b, int c) {
                boolean hasText = s.toString().trim().length() > 0;
                btnCreateEvent.setEnabled(hasText);
                btnCreateEvent.setBackgroundTintList(ColorStateList.valueOf(hasText ? Color.parseColor("#FFD600") : Color.LTGRAY));
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
        });

        btnCreateEvent.setOnClickListener(v -> {
            String title = etEventTitle.getText().toString();
            String time = etStartTime.getText().toString();
            String eventDate = tvDateDisplay.getText().toString();
            String course = isCourseEvent ? spinnerCourse.getSelectedItem().toString() : "Personal";

            // Assuming CalendarEvent implements Serializable or Parcelable
            CalendarEvent newEvent = new CalendarEvent(time, title, course, eventDate);
            Intent resultIntent = new Intent();
            resultIntent.putExtra("NEW_EVENT", newEvent);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private void setupCourseSpinner() {
        List<String> courses = new ArrayList<>();
        courses.add("Biology"); courses.add("Comp org"); courses.add("App dev");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, courses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourse.setAdapter(adapter);
    }

    private void showTimePicker(EditText target) {
        Calendar c = Calendar.getInstance();
        new TimePickerDialog(this, (view, hr, min) -> {
            String amPm = (hr < 12) ? "AM" : "PM";
            int hour = (hr == 0 || hr == 12) ? 12 : hr % 12;
            target.setText(String.format("%02d:%02d %s", hour, min, amPm));
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show();
    }
}