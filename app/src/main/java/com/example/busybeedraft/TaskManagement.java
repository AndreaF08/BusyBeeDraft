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

        courseName = getIntent().getStringExtra("COURSE_NAME");
        if (courseName == null) courseName = "General";

        lvTasks = findViewById(R.id.lvTasks);
        tvNoTasks = findViewById(R.id.tvNoTasks);
        taskList = new ArrayList<>();
        taskCompletionStatus = new HashMap<>();
        adapter = new CustomTaskAdapter(this, taskList);
        lvTasks.setAdapter(adapter);

        loadTasks();

        findViewById(R.id.btnOpenAddDialog).setOnClickListener(v -> showAddTaskDialog());

        findViewById(R.id.btnClearCompleted).setOnClickListener(v -> {
            // 1. Identify which tasks are completed
            ArrayList<String> tasksToRemove = new ArrayList<>();
            for (String task : taskList) {
                if (Boolean.TRUE.equals(taskCompletionStatus.get(task))) {
                    tasksToRemove.add(task);
                }
            }

            // 2. Remove those specific tasks from the main list and the status map
            if (!tasksToRemove.isEmpty()) {
                for (String task : tasksToRemove) {
                    taskList.remove(task);
                    taskCompletionStatus.remove(task);
                }

                // 3. Save the updated state and refresh the UI
                saveTasksToDisk();
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "Completed tasks cleared", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No completed tasks to clear", Toast.LENGTH_SHORT).show();
            }
        });

        setupBottomNavigation();
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
        SharedPreferences prefs = getSharedPreferences("CourseData_" + courseName, MODE_PRIVATE);
        try {
            JSONArray arr = new JSONArray(prefs.getString("tasks", "[]"));
            taskList.clear();
            taskCompletionStatus.clear();
            for (int i = 0; i < arr.length(); i++) {
                taskList.add(arr.getString(i));
                taskCompletionStatus.put(arr.getString(i), false);
            }

            JSONArray completionArr = new JSONArray(prefs.getString("taskCompletion", "[]"));
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
    private void showEditTaskDialog(String oldTaskName, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Use the correct edit dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_task, null);
        builder.setView(dialogView);

        EditText etTaskName = dialogView.findViewById(R.id.edit_task_name);
        Button btnSave = dialogView.findViewById(R.id.btn_save_task);

        // Pre-fill with the existing task name
        etTaskName.setText(oldTaskName);

        AlertDialog dialog = builder.create();

        // Ensure the background is transparent if you have rounded corners in your pop_card drawable
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnSave.setOnClickListener(v -> {
            String newTaskName = etTaskName.getText().toString().trim();
            if (!newTaskName.isEmpty()) {
                // 1. Update the status map (transfer the completion state to the new name)
                boolean status = taskCompletionStatus.getOrDefault(oldTaskName, false);
                taskCompletionStatus.remove(oldTaskName);
                taskCompletionStatus.put(newTaskName, status);

                // 2. Update the list at the specific position
                taskList.set(position, newTaskName);

                // 3. Persist and refresh
                saveTasksToDisk();
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Task name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }
    private void saveTasksToDisk() {
        SharedPreferences prefs = getSharedPreferences("CourseData_" + courseName, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("tasks", new JSONArray(taskList).toString());

        JSONArray completionArr = new JSONArray();
        for (String task : taskList) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("task", task);
                obj.put("completed", taskCompletionStatus.getOrDefault(task, false));
                completionArr.put(obj);
            } catch (Exception ignored) {}
        }
        editor.putString("taskCompletion", completionArr.toString());
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

            // 1. Prevent reloading the current activity if already active
            if (id == nav.getSelectedItemId()) {
                return true;
            }

            Intent intent = null;
            if (id == R.id.nav_home) {
                intent = new Intent(this, MainActivity.class);
            } else if (id == R.id.nav_folders) {
                intent = new Intent(this, FolderActivity.class);
            } else if (id == R.id.nav_calendar) {
                intent = new Intent(this, CalendarActivity.class);
            } else if (id == R.id.nav_pomodoro) {
                intent = new Intent(this, PomodoroActivity.class);
            } else if (id == R.id.nav_id) {
                intent = new Intent(this, DashboardActivity.class);
            }

            if (intent != null) {
                // 2. FLAG_ACTIVITY_CLEAR_TOP is key here.
                // It clears the stack between the current activity and the target.
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                // 3. Optional: Finish this activity so it doesn't linger in the stack
                finish();
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
            ImageButton btnEditTask = convertView.findViewById(R.id.btnEditTask); // Reference the edit button

            tvTaskTitle.setText(task);
            convertView.findViewById(R.id.prioritySideBar).setBackgroundColor(Color.parseColor("#FFD600"));

            // Setup Checkbox
            cbTaskStatus.setOnCheckedChangeListener(null); // Clear listener before setting checked state
            boolean isCompleted = taskCompletionStatus.getOrDefault(task, false);
            cbTaskStatus.setChecked(isCompleted);
            cbTaskStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
                taskCompletionStatus.put(task, isChecked);
                saveTasksToDisk();
            });

            // Setup Edit Button Click
            btnEditTask.setOnClickListener(v -> {
                showEditTaskDialog(task, position);
            });

            return convertView;
        }
    }}