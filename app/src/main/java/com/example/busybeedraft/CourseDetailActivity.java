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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class CourseDetailActivity extends AppCompatActivity {
    private TextView tvCourseTitle, tvDetailInstructor, tvDetailSchedule;
    private LinearLayout todoContent, studySetContent, emptyTodoState;
    private Button tabTodo, tabStudySets;
    private RecyclerView rvStudySets, rvTaskList;
    private FlashcardAdapter flashcardAdapter;
    private TaskAdapter taskAdapter;
    private List<Flashcard> flashcardList = new ArrayList<>();
    private List<String> taskList = new ArrayList<>();
    private String courseName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        tvCourseTitle = findViewById(R.id.tvCourseTitle);
        tvDetailInstructor = findViewById(R.id.tvDetailInstructor);
        tvDetailSchedule = findViewById(R.id.tvDetailSchedule);
        todoContent = findViewById(R.id.todoContent);
        studySetContent = findViewById(R.id.studySetContent);
        emptyTodoState = findViewById(R.id.emptyTodoState);
        tabTodo = findViewById(R.id.tabTodo);
        tabStudySets = findViewById(R.id.tabStudySets);

        rvStudySets = findViewById(R.id.rvStudySets);
        rvStudySets.setLayoutManager(new LinearLayoutManager(this));
        flashcardAdapter = new FlashcardAdapter(flashcardList);
        rvStudySets.setAdapter(flashcardAdapter);

        rvTaskList = findViewById(R.id.rvTaskList);
        rvTaskList.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(taskList);
        rvTaskList.setAdapter(taskAdapter);

        courseName = getIntent().getStringExtra("COURSE_NAME");
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
        findViewById(R.id.btnAddStudySet).setOnClickListener(v -> showAddFlashcardDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCourseData();
    }

    private void toggleTabs(boolean isTodo) {
        todoContent.setVisibility(isTodo ? View.VISIBLE : View.GONE);
        studySetContent.setVisibility(isTodo ? View.GONE : View.VISIBLE);
        tabTodo.setBackgroundTintList(ColorStateList.valueOf(isTodo ? Color.parseColor("#FFD600") : Color.parseColor("#E0E0E0")));
        tabStudySets.setBackgroundTintList(ColorStateList.valueOf(isTodo ? Color.parseColor("#E0E0E0") : Color.parseColor("#FFD600")));
    }

    private void loadCourseData() {
        SharedPreferences prefs = getSharedPreferences("CourseData_" + courseName, MODE_PRIVATE);
        tvDetailInstructor.setText(prefs.getString("instructor", "Ms. Jennie Kim"));
        tvDetailSchedule.setText(prefs.getString("schedule", "MON THU"));

        try {
            JSONArray tArr = new JSONArray(prefs.getString("tasks", "[]"));
            taskList.clear();
            for (int i = 0; i < tArr.length(); i++) taskList.add(tArr.getString(i));
            taskAdapter.notifyDataSetChanged();

            emptyTodoState.setVisibility(taskList.isEmpty() ? View.VISIBLE : View.GONE);
            rvTaskList.setVisibility(taskList.isEmpty() ? View.GONE : View.VISIBLE);

            JSONArray fArr = new JSONArray(prefs.getString("flashcards", "[]"));
            flashcardList.clear();
            for (int i = 0; i < fArr.length(); i++) {
                JSONObject o = fArr.getJSONObject(i);
                flashcardList.add(new Flashcard(o.getString("q"), o.getString("a")));
            }
            flashcardAdapter.notifyDataSetChanged();
        } catch (Exception ignored) {}
    }

    private void saveCourseData() {
        SharedPreferences prefs = getSharedPreferences("CourseData_" + courseName, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("instructor", tvDetailInstructor.getText().toString());
        editor.putString("schedule", tvDetailSchedule.getText().toString());
        editor.apply();
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

    private void showAddFlashcardDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.dialog_add_flashcard, null);
        builder.setView(v);
        EditText etQ = v.findViewById(R.id.etNewTerm);
        EditText etA = v.findViewById(R.id.etNewDefinition);
        AlertDialog dialog = builder.create();
        v.findViewById(R.id.btnSaveFlashcard).setOnClickListener(view -> {
            String q = etQ.getText().toString().trim();
            String a = etA.getText().toString().trim();
            if (!q.isEmpty()) {
                flashcardList.add(new Flashcard(q, a));
                saveFlashcards();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void saveFlashcards() {
        SharedPreferences prefs = getSharedPreferences("CourseData_" + courseName, MODE_PRIVATE);
        JSONArray arr = new JSONArray();
        try {
            for (Flashcard f : flashcardList) {
                JSONObject o = new JSONObject();
                o.put("q", f.getTerm());
                o.put("a", f.getDefinition());
                arr.put(o);
            }
            prefs.edit().putString("flashcards", arr.toString()).apply();
            flashcardAdapter.notifyDataSetChanged();
        } catch (Exception ignored) {}
    }

    // FIXED: Added getters and setters to satisfy FlashcardAdapter
    public static class Flashcard {
        private String term, definition;
        public Flashcard(String t, String d) { this.term = t; this.definition = d; }
        public String getTerm() { return term; }
        public void setTerm(String t) { this.term = t; }
        public String getDefinition() { return definition; }
        public void setDefinition(String d) { this.definition = d; }
    }

    private class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.VH> {
        List<String> list;
        TaskAdapter(List<String> list) { this.list = list; }
        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_task_card, p, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            h.t.setText(list.get(pos));
            h.s.setBackgroundColor(Color.parseColor("#FFD600"));
        }
        @Override public int getItemCount() { return list.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView t; View s;
            VH(View v) { super(v); t = v.findViewById(R.id.tvTaskTitle); s = v.findViewById(R.id.prioritySideBar); }
        }
    }
}