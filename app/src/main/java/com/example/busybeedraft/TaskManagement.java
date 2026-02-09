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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.json.JSONArray;
import java.util.ArrayList;

public class TaskManagement extends AppCompatActivity {
    private String courseName;
    private ArrayList<String> taskList;
    private CustomTaskAdapter adapter;
    private ListView lvTasks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_management);

        courseName = getIntent().getStringExtra("COURSE_NAME");
        if (courseName == null) courseName = "General";

        lvTasks = findViewById(R.id.lvTasks);
        taskList = new ArrayList<>();
        adapter = new CustomTaskAdapter(this, taskList);
        lvTasks.setAdapter(adapter);

        loadTasks();

        findViewById(R.id.btnOpenAddDialog).setOnClickListener(v -> showAddTaskDialog());

        findViewById(R.id.btnClearCompleted).setOnClickListener(v -> {
            taskList.clear();
            saveTasksToDisk();
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "All tasks cleared", Toast.LENGTH_SHORT).show();
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
            for (int i = 0; i < arr.length(); i++) taskList.add(arr.getString(i));
            adapter.notifyDataSetChanged();
        } catch (Exception ignored) {}
    }

    private void saveTasksToDisk() {
        SharedPreferences prefs = getSharedPreferences("CourseData_" + courseName, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("tasks", new JSONArray(taskList).toString());
        editor.apply();
    }

    private void setupBottomNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigation);
        nav.setSelectedItemId(R.id.nav_home);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_id) finish();
            return true;
        });
    }

    private class CustomTaskAdapter extends ArrayAdapter<String> {
        public CustomTaskAdapter(Context context, ArrayList<String> tasks) { super(context, 0, tasks); }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_task_card, parent, false);
            }
            ((TextView)convertView.findViewById(R.id.tvTaskTitle)).setText(getItem(position));
            convertView.findViewById(R.id.prioritySideBar).setBackgroundColor(Color.parseColor("#FFD600"));
            return convertView;
        }
    }
}