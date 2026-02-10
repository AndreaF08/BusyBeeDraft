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
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class CourseDetailActivity extends AppCompatActivity {
    private TextView tvCourseTitle, tvDetailInstructor, tvDetailSchedule;
    private LinearLayout todoContent, studySetContent, emptyTodoState;
    private Button tabTodo, tabStudySets;
    private RecyclerView rvTaskList, rvStudySets;
    private TaskAdapter taskAdapter;
    private FlashcardAdapter flashcardAdapter;
    private List<String> taskList = new ArrayList<>();
    private List<FlashcardAdapter.Flashcard> flashcardList = new ArrayList<>();
    private String courseName;
    private int selectedFolderColor = Color.parseColor("#FFD600");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        if (getUserPrefix().equals("default_user")) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // UI Binding
        tvCourseTitle = findViewById(R.id.tvCourseTitle);
        tvDetailInstructor = findViewById(R.id.tvDetailInstructor);
        tvDetailSchedule = findViewById(R.id.tvDetailSchedule);
        todoContent = findViewById(R.id.todoContent);
        studySetContent = findViewById(R.id.studySetContent);
        emptyTodoState = findViewById(R.id.emptyTodoState);
        tabTodo = findViewById(R.id.tabTodo);
        tabStudySets = findViewById(R.id.tabStudySets);
        courseName = getIntent().getStringExtra("COURSE_NAME");

        // Tasks Recycler
        rvTaskList = findViewById(R.id.rvTaskList);
        rvTaskList.setLayoutManager(new LinearLayoutManager(this));
        taskAdapter = new TaskAdapter(taskList);
        rvTaskList.setAdapter(taskAdapter);

        // Flashcards Recycler (Matches your XML ID: rvStudySets)
        rvStudySets = findViewById(R.id.rvStudySets);
        rvStudySets.setLayoutManager(new LinearLayoutManager(this));
        flashcardAdapter = new FlashcardAdapter(flashcardList);
        rvStudySets.setAdapter(flashcardAdapter);

        if (courseName != null) {
            tvCourseTitle.setText(courseName);
            loadCourseData();
            loadFlashcards();
        }

        tabTodo.setOnClickListener(v -> toggleTabs(true));
        tabStudySets.setOnClickListener(v -> toggleTabs(false));
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnEditCourse).setOnClickListener(v -> showEditDialog());

        // Add Flashcard Button (Matches your XML ID: btnAddStudySet)
        findViewById(R.id.btnAddStudySet).setOnClickListener(v -> showFlashcardDialog(null, -1));

        findViewById(R.id.btnAddTodo).setOnClickListener(v -> {
            Intent intent = new Intent(this, TaskManagement.class);
            intent.putExtra("COURSE_NAME", courseName);
            startActivity(intent);
        });
    }

    // Changed to public so FlashcardAdapter can call it for editing
    public void showFlashcardDialog(FlashcardAdapter.Flashcard existingCard, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.dialog_add_flashcard, null);

        EditText etTerm = v.findViewById(R.id.etNewTerm);
        EditText etDef = v.findViewById(R.id.etNewDefinition);
        Button btnSave = v.findViewById(R.id.btnSaveFlashcard);

        builder.setView(v);
        AlertDialog dialog = builder.create();

        if (existingCard != null) {
            etTerm.setText(existingCard.getTerm());
            etDef.setText(existingCard.getDefinition());
        }

        btnSave.setOnClickListener(view -> {
            String t = etTerm.getText().toString().trim();
            String d = etDef.getText().toString().trim();

            if (t.isEmpty()) return;

            if (existingCard == null) {
                flashcardList.add(new FlashcardAdapter.Flashcard(t, d));
            } else {
                existingCard.setTerm(t);
                existingCard.setDefinition(d);
            }
            saveFlashcards();
            flashcardAdapter.notifyDataSetChanged();
            dialog.dismiss();
        });
        dialog.show();
    }

    private void saveFlashcards() {
        String email = getUserPrefix();
        SharedPreferences.Editor ed = getSharedPreferences(email + "_Flashcards_" + courseName, MODE_PRIVATE).edit();
        JSONArray arr = new JSONArray();
        try {
            for (FlashcardAdapter.Flashcard f : flashcardList) {
                JSONObject obj = new JSONObject();
                obj.put("term", f.getTerm());
                obj.put("def", f.getDefinition());
                arr.put(obj);
            }
        } catch (Exception ignored) {}
        ed.putString("cards", arr.toString()).apply();
    }

    private void loadFlashcards() {
        String email = getUserPrefix();
        SharedPreferences prefs = getSharedPreferences(email + "_Flashcards_" + courseName, MODE_PRIVATE);
        try {
            JSONArray arr = new JSONArray(prefs.getString("cards", "[]"));
            flashcardList.clear();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                flashcardList.add(new FlashcardAdapter.Flashcard(o.getString("term"), o.getString("def")));
            }
            flashcardAdapter.notifyDataSetChanged();
        } catch (Exception ignored) {}
    }

    // --- UTILITY & ORIGINAL METHODS ---
    private String getUserPrefix() {
        return getSharedPreferences("UserSession", MODE_PRIVATE).getString("current_user_email", "default_user");
    }

    private void toggleTabs(boolean isTodo) {
        todoContent.setVisibility(isTodo ? View.VISIBLE : View.GONE);
        studySetContent.setVisibility(isTodo ? View.GONE : View.VISIBLE);
        tabTodo.setBackgroundTintList(ColorStateList.valueOf(isTodo ? selectedFolderColor : Color.parseColor("#E0E0E0")));
        tabStudySets.setBackgroundTintList(ColorStateList.valueOf(isTodo ? Color.parseColor("#E0E0E0") : selectedFolderColor));
    }

    private void loadCourseData() {
        String email = getUserPrefix();
        String prefName = email + "_CourseData_" + courseName.trim();
        SharedPreferences prefs = getSharedPreferences(prefName, MODE_PRIVATE);
        tvDetailInstructor.setText(prefs.getString("instructor", "Not Assigned"));
        tvDetailSchedule.setText(prefs.getString("schedule", "TBA"));
        selectedFolderColor = prefs.getInt("folder_color", Color.parseColor("#FFD600"));
    }

    private void saveCourseData() {
        String email = getUserPrefix();
        SharedPreferences.Editor ed = getSharedPreferences(email + "_CourseData_" + courseName.trim(), MODE_PRIVATE).edit();
        ed.putString("instructor", tvDetailInstructor.getText().toString());
        ed.putString("schedule", tvDetailSchedule.getText().toString());
        ed.apply();
    }

    private void showEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.dialog_edit_course, null);
        EditText etInst = v.findViewById(R.id.etEditInstructor);
        EditText etSched = v.findViewById(R.id.etEditSchedule);
        etInst.setText(tvDetailInstructor.getText());
        etSched.setText(tvDetailSchedule.getText());
        builder.setView(v);
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
            VH(View v) { super(v); t = v.findViewById(R.id.tvTaskTitle); s = v.findViewById(R.id.prioritySideBar); }
        }
    }
}