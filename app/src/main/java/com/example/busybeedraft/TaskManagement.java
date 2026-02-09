package com.example.busybeedraft;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CheckBox;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

public class TaskManagement extends AppCompatActivity {
    private String courseName;
    private ArrayList<String> taskList;
    private HashMap<String, Boolean> taskCompletionStatus;
    private CustomTaskAdapter adapter;
    private ListView lvTasks;
    private TextView tvNoTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_management);

        // Security Check: Redirect if no user is logged in
        if (getUserPrefix().equals("default_user")) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Get course name and apply .trim() to match the Master List logic
        courseName = getIntent().getStringExtra("COURSE_NAME");
        if (courseName == null) {
            courseName = "General";
        } else {
            courseName = courseName.trim();
        }

        lvTasks = findViewById(R.id.lvTasks);
        tvNoTasks = findViewById(R.id.tvNoTasks);
        taskList = new ArrayList<>();
        taskCompletionStatus = new HashMap<>();
        adapter = new CustomTaskAdapter(this, taskList);
        lvTasks.setAdapter(adapter);

        loadTasks();

        findViewById(R.id.btnOpenAddDialog).setOnClickListener(v -> showAddTaskDialog());

        findViewById(R.id.btnClearCompleted).setOnClickListener(v -> {
            ArrayList<String> tasksToRemove = new ArrayList<>();
            for (String task : taskList) {
                if (Boolean.TRUE.equals(taskCompletionStatus.get(task))) {
                    tasksToRemove.add(task);
                }
            }

            if (!tasksToRemove.isEmpty()) {
                for (String task : tasksToRemove) {
                    taskList.remove(task);
                    taskCompletionStatus.remove(task);
                }
                saveTasksToDisk();
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "Completed tasks cleared", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No completed tasks to clear", Toast.LENGTH_SHORT).show();
            }
        });

        setupBottomNavigation();
    }

    private String getUserPrefix() {
        SharedPreferences sessionPrefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        return sessionPrefs.getString("current_user_email", "default_user");
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        EditText etTaskName = dialogView.findViewById(R.id.edit_task_name);
        Button btnSave = dialogView.findViewById(R.id.btn_save_task);

        AlertDialog dialog = builder.create();
        btnSave.setOnClickListener(v -> {
            String task = etTaskName.getText().toString().trim();
            if (!task.isEmpty()) {
                taskList.add(task);
                saveTasksToDisk();
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void loadTasks() {
        String email = getUserPrefix();
        // Uses the same naming convention as CourseDetailActivity for seamless data sharing
        String prefName = email + "_CourseData_" + courseName;
        SharedPreferences prefs = getSharedPreferences(prefName, MODE_PRIVATE);

        try {
            // Use unique keys for strict isolation
            String tasksJson = prefs.getString(email + "_tasks", "[]");
            JSONArray arr = new JSONArray(tasksJson);

            taskList.clear();
            taskCompletionStatus.clear();

            for (int i = 0; i < arr.length(); i++) {
                String taskName = arr.getString(i);
                taskList.add(taskName);
                taskCompletionStatus.put(taskName, false);
            }

            String completionJson = prefs.getString(email + "_taskCompletion", "[]");
            JSONArray completionArr = new JSONArray(completionJson);
            for (int i = 0; i < completionArr.length(); i++) {
                JSONObject obj = completionArr.getJSONObject(i);
                String taskName = obj.getString("task");
                boolean isComplete = obj.getBoolean("completed");
                taskCompletionStatus.put(taskName, isComplete);
            }

            adapter.notifyDataSetChanged();
            updateEmptyState();
        } catch (Exception ignored) {}
    }

    private void saveTasksToDisk() {
        String email = getUserPrefix();
        String prefName = email + "_CourseData_" + courseName;
        SharedPreferences prefs = getSharedPreferences(prefName, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Save with user-specific keys to prevent data bleed
        editor.putString(email + "_tasks", new JSONArray(taskList).toString());

        JSONArray completionArr = new JSONArray();
        for (String task : taskList) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("task", task);
                obj.put("completed", taskCompletionStatus.getOrDefault(task, false));
                completionArr.put(obj);
            } catch (Exception ignored) {}
        }
        editor.putString(email + "_taskCompletion", completionArr.toString());
        editor.apply();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (taskList.isEmpty()) {
            lvTasks.setVisibility(View.GONE);
            tvNoTasks.setVisibility(View.VISIBLE);
        } else {
            lvTasks.setVisibility(View.VISIBLE);
            tvNoTasks.setVisibility(View.GONE);
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == nav.getSelectedItemId()) return true;

            Intent intent = null;
            if (id == R.id.nav_home) return true; // Already here
            else if (id == R.id.nav_folders) intent = new Intent(this, FolderActivity.class);
            else if (id == R.id.nav_calendar) intent = new Intent(this, CalendarActivity.class);
            else if (id == R.id.nav_pomodoro) intent = new Intent(this, PomodoroActivity.class);
            else if (id == R.id.nav_id) intent = new Intent(this, DashboardActivity.class);

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private class CustomTaskAdapter extends ArrayAdapter<String> {
        public CustomTaskAdapter(Context context, ArrayList<String> tasks) {
            super(context, 0, tasks);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_task_card, parent, false);
            }

            String task = getItem(position);
            TextView tvTaskTitle = convertView.findViewById(R.id.tvTaskTitle);
            CheckBox cbTaskStatus = convertView.findViewById(R.id.cbTaskStatus);
            ImageButton btnEditTask = convertView.findViewById(R.id.btnEditTask);

            tvTaskTitle.setText(task);

            // Priority bar color matches your app's yellow theme
            convertView.findViewById(R.id.prioritySideBar).setBackgroundColor(Color.parseColor("#FFD600"));

            cbTaskStatus.setOnCheckedChangeListener(null);
            boolean isCompleted = taskCompletionStatus.getOrDefault(task, false);
            cbTaskStatus.setChecked(isCompleted);
            cbTaskStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
                taskCompletionStatus.put(task, isChecked);
                saveTasksToDisk();
            });

            btnEditTask.setOnClickListener(v -> showEditTaskDialog(task, position));
            return convertView;
        }
    }

    private void showEditTaskDialog(String oldTaskName, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_task, null);
        builder.setView(dialogView);

        EditText etTaskName = dialogView.findViewById(R.id.edit_task_name);
        Button btnSave = dialogView.findViewById(R.id.btn_save_task);

        etTaskName.setText(oldTaskName);
        AlertDialog dialog = builder.create();

        btnSave.setOnClickListener(v -> {
            String newTaskName = etTaskName.getText().toString().trim();
            if (!newTaskName.isEmpty()) {
                boolean status = taskCompletionStatus.getOrDefault(oldTaskName, false);
                taskCompletionStatus.remove(oldTaskName);
                taskCompletionStatus.put(newTaskName, status);
                taskList.set(position, newTaskName);
                saveTasksToDisk();
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Task name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }
}