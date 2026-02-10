package com.example.busybeedraft;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {
    private RecyclerView rvEvents;
    private EventAdapter adapter;
    private List<CalendarEvent> eventList = new ArrayList<>();
    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        TextView tvMonthYear = findViewById(R.id.tvMonthYear);
        CalendarView calendarView = findViewById(R.id.calendarView);
        MaterialButton btnEvent = findViewById(R.id.btnEvent);
        rvEvents = findViewById(R.id.rvEvents);
        rvEvents.setLayoutManager(new LinearLayoutManager(this));

        Calendar c = Calendar.getInstance();
        selectedDate = c.get(Calendar.DAY_OF_MONTH) + "/" + (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.YEAR);
        tvMonthYear.setText(selectedDate);

        adapter = new EventAdapter(eventList, new EventAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(CalendarEvent event, int position) {
                Intent intent = new Intent(CalendarActivity.this, NewEventActivity.class);
                intent.putExtra("EDIT_MODE", true);
                intent.putExtra("EVENT_DATA", event);
                intent.putExtra("EVENT_INDEX", position);
                startActivity(intent);
            }
            @Override
            public void onItemLongClick(CalendarEvent event, int position) {}
        });
        rvEvents.setAdapter(adapter);

        // SWIPE TO DELETE LOGIC
        ItemTouchHelper.SimpleCallback touchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView rv, RecyclerView.ViewHolder vh, RecyclerView.ViewHolder target) { return false; }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                CalendarEvent eventToDelete = eventList.get(position);

                new AlertDialog.Builder(CalendarActivity.this)
                        .setTitle("Delete Event")
                        .setMessage("Delete this event?")
                        .setPositiveButton("Delete", (dialog, which) -> deleteEvent(eventToDelete))
                        .setNegativeButton("Cancel", (dialog, which) -> adapter.notifyItemChanged(position))
                        .show();
            }
        };
        new ItemTouchHelper(touchHelperCallback).attachToRecyclerView(rvEvents);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            tvMonthYear.setText(selectedDate);
            loadEventsForDate(selectedDate);
        });

        btnEvent.setOnClickListener(v -> {
            Intent intent = new Intent(this, NewEventActivity.class);
            intent.putExtra("SELECTED_DATE", selectedDate);
            startActivity(intent);
        });

        setupBottomNavigation();
        loadEventsForDate(selectedDate);
    }

    private void deleteEvent(CalendarEvent eventToDelete) {
        SharedPreferences prefs = getSharedPreferences("CalendarPrefs", MODE_PRIVATE);
        try {
            JSONArray allEvents = new JSONArray(prefs.getString("all_events", "[]"));
            JSONArray updatedList = new JSONArray();
            for (int i = 0; i < allEvents.length(); i++) {
                JSONObject obj = allEvents.getJSONObject(i);
                if (!(obj.getString("title").equals(eventToDelete.title) &&
                        obj.getString("time").equals(eventToDelete.time))) {
                    updatedList.put(obj);
                }
            }
            prefs.edit().putString("all_events", updatedList.toString()).apply();
            loadEventsForDate(selectedDate);
        } catch (Exception ignored) {}
    }

    private void loadEventsForDate(String date) {
        eventList.clear();
        SharedPreferences prefs = getSharedPreferences("CalendarPrefs", MODE_PRIVATE);
        try {
            JSONArray array = new JSONArray(prefs.getString("all_events", "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                if (obj.getString("date").equals(date)) {
                    eventList.add(new CalendarEvent(obj.getString("title"), obj.getString("date"),
                            obj.optString("endDate", ""), obj.getString("time"),
                            obj.optString("course", "General"), obj.optString("color", "#42A5F5")));
                }
            }
            adapter.notifyDataSetChanged();
        } catch (Exception ignored) {}
    }

    private void setupBottomNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);

        // Match selection and text visibility logic from TaskManagement
        nav.setSelectedItemId(R.id.nav_calendar);

        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // If the user clicks the icon for the activity they are already on, do nothing
            if (id == R.id.nav_calendar) {
                return true;
            }

            Intent intent = null;
            if (id == R.id.nav_home) {
                intent = new Intent(this, TaskManagement.class);
            } else if (id == R.id.nav_folders) {
                intent = new Intent(this, FolderActivity.class);
            } else if (id == R.id.nav_pomodoro) {
                intent = new Intent(this, PomodoroActivity.class);
            } else if (id == R.id.nav_id) {
                intent = new Intent(this, DashboardActivity.class);
            }

            if (intent != null) {
                // Use flags from TaskManagement to prevent state loss/reset
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                // Remove animation to prevent the "shifting" look
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEventsForDate(selectedDate);

        // Re-sync selection state on resume to ensure text remains labeled correctly
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_calendar);
    }
}