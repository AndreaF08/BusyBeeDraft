package com.example.busybeedraft;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;

public class CourseDetailActivity extends AppCompatActivity {
    private TextView tvCourseTitle, tvDetailInstructor, tvDetailSchedule;
    private LinearLayout todoContent, studySetContent, emptyTodoState;
    private Button tabTodo, tabStudySets;
    private RecyclerView rvTaskList;
    private TaskAdapter taskAdapter;
    private List<String> taskList = new ArrayList<>();
    private String courseName;
    private int selectedFolderColor = Color.parseColor("#FFD600");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        // Security Check: Redirect if no user is logged in
        if (getUserPrefix().equals("default_user")) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        tvCourseTitle = findViewById(R.id.tvCourseTitle);
        tvDetailInstructor = findViewById(R.id.tvDetailInstructor);
        tvDetailSchedule = findViewById(R.id.tvDetailSchedule);
        todoContent = findViewById(R.id.todoContent);
        studySetContent = findViewById(R.id.studySetContent);
        emptyTodoState = findViewById(R.id.emptyTodoState);
        tabTodo = findViewById(R.id.tabTodo);
        tabStudySets = findViewById(R.id.tabStudySets);

        courseName = getIntent().getStringExtra("COURSE_NAME");

        rvTaskList = findViewById(R.id.rvTaskList);
        rvTaskList.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(taskList);
        rvTaskList.setAdapter(taskAdapter);

        if (courseName != null) {
            tvCourseTitle.setText(courseName);
            loadCourseData();
        }

        tabTodo.setOnClickListener(v -> toggleTabs(true));
        tabStudySets.setOnClickListener(v -> toggleTabs(false));
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnEditCourse).setOnClickListener(v -> showEditDialog());
        findViewById(R.id.btnAddTodo).setOnClickListener(v -> {
            Intent intent = new Intent(this, TaskManagement.class);
            intent.putExtra("COURSE_NAME", courseName);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCourseData();
    }

    private String getUserPrefix() {
        SharedPreferences sessionPrefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        return sessionPrefs.getString("current_user_email", "default_user");
    }

    private void toggleTabs(boolean isTodo) {
        todoContent.setVisibility(isTodo ? View.VISIBLE : View.GONE);
        studySetContent.setVisibility(isTodo ? View.GONE : View.VISIBLE);
        tabTodo.setBackgroundTintList(ColorStateList.valueOf(isTodo ? selectedFolderColor : Color.parseColor("#E0E0E0")));
        tabStudySets.setBackgroundTintList(ColorStateList.valueOf(isTodo ? Color.parseColor("#E0E0E0") : selectedFolderColor));
    }

    private void loadCourseData() {
        String email = getUserPrefix();
        List<String> userCourses = loadMasterCourseList();

        // FIX: Ensure both lists exist and perform a trimmed comparison
        if (courseName == null || userCourses == null || !userCourses.contains(courseName.trim())) {
            Toast.makeText(this, "Access Denied: Course not linked to " + email, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Data is unique to User + Course Name
        String prefName = email + "_CourseData_" + courseName.trim();
        SharedPreferences prefs = getSharedPreferences(prefName, MODE_PRIVATE);

        tvDetailInstructor.setText(prefs.getString("instructor", "Not Assigned"));
        tvDetailSchedule.setText(prefs.getString("schedule", "TBA"));
        selectedFolderColor = prefs.getInt("folder_color", Color.parseColor("#FFD600"));

        try {
            JSONArray tArr = new JSONArray(prefs.getString("tasks", "[]"));
            taskList.clear();
            for (int i = 0; i < tArr.length(); i++) {
                taskList.add(tArr.getString(i));
            }
            taskAdapter.notifyDataSetChanged();

            emptyTodoState.setVisibility(taskList.isEmpty() ? View.VISIBLE : View.GONE);
            rvTaskList.setVisibility(taskList.isEmpty() ? View.GONE : View.VISIBLE);
        } catch (Exception ignored) {}
    }

    private void saveCourseData() {
        String email = getUserPrefix();
        String prefName = email + "_CourseData_" + courseName.trim();
        SharedPreferences prefs = getSharedPreferences(prefName, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("instructor", tvDetailInstructor.getText().toString());
        editor.putString("schedule", tvDetailSchedule.getText().toString());
        editor.putInt("folder_color", selectedFolderColor);
        editor.apply();
    }

    /**
     * FIX: Pulls from the central UserCourses file using the unique user email key.
     */
    public List<String> loadMasterCourseList() {
        String email = getUserPrefix();
        SharedPreferences prefs = getSharedPreferences("UserCourses", MODE_PRIVATE);

        // This key MUST match FolderActivity.updateMasterCourseList()
        String json = prefs.getString(email + "_MASTER_COURSES", "[]");

        List<String> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                list.add(arr.getString(i).trim());
            }
        } catch (Exception ignored) {}
        return list;
    }

    private void showEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.dialog_edit_course, null);
        builder.setView(v);

        EditText etInst = v.findViewById(R.id.etEditInstructor);
        EditText etSched = v.findViewById(R.id.etEditSchedule);
        etInst.setText(tvDetailInstructor.getText());
        etSched.setText(tvDetailSchedule.getText());

        AlertDialog dialog = builder.create();
        v.findViewById(R.id.btnUpdateCourse).setOnClickListener(view -> {
            tvDetailInstructor.setText(etInst.getText().toString());
            tvDetailSchedule.setText(etSched.getText().toString());
            saveCourseData();
            dialog.dismiss();
        });
        dialog.show();
    }

    private class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.VH> {
        List<String> list;
        TaskAdapter(List<String> list) { this.list = list; }
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_task_card, p, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            h.t.setText(list.get(pos));
            h.s.setBackgroundColor(selectedFolderColor);
        }
        @Override public int getItemCount() { return list.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView t; View s;
            VH(View v) {
                super(v);
                t = v.findViewById(R.id.tvTaskTitle);
                s = v.findViewById(R.id.prioritySideBar);
            }
        }
    }
}