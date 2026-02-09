package com.example.busybeedraft;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class FolderActivity extends AppCompatActivity {
    private GridView gvFolders;
    private List<Subject> folderList = new ArrayList<>();
    private FolderAdapter adapter;
    private String selectedColor = "folder_pink_full";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);

        gvFolders = findViewById(R.id.gvFolders);
        loadFolders(); // Load saved data first

        adapter = new FolderAdapter(folderList);
        gvFolders.setAdapter(adapter);

        setupBottomNavigation();

        // Plus button to add subjects
        findViewById(R.id.btnAddFolder).setOnClickListener(v -> showAddFolderDialog());
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setSelectedItemId(R.id.nav_folders); // Highlights "Courses/Folders"

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) startActivity(new Intent(this, TaskManagement.class));
            else if (id == R.id.nav_calendar) startActivity(new Intent(this, CalendarActivity.class));
            else if (id == R.id.nav_pomodoro) startActivity(new Intent(this, PomodoroActivity.class));
            else if (id == R.id.nav_id) startActivity(new Intent(this, DashboardActivity.class));
            return true;
        });
    }

    private void showAddFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.dialog_add_subject, null);
        builder.setView(v);

        EditText etName = v.findViewById(R.id.etSubjectName);
        AlertDialog dialog = builder.create();

        // Color clicks update the 'selectedColor' string
        v.findViewById(R.id.color_pink).setOnClickListener(view -> selectedColor = "folder_pink_full");
        v.findViewById(R.id.color_blue).setOnClickListener(view -> selectedColor = "folder_blue_full");
        v.findViewById(R.id.color_green).setOnClickListener(view -> selectedColor = "folder_green_full");
        v.findViewById(R.id.color_yellow).setOnClickListener(view -> selectedColor = "folder_yellow_full");
        v.findViewById(R.id.color_orange).setOnClickListener(view -> selectedColor = "folder_orange_full");

        v.findViewById(R.id.btnSaveSubject).setOnClickListener(view -> {
            String name = etName.getText().toString().trim();
            if (!name.isEmpty()) {
                folderList.add(new Subject(name, selectedColor));
                saveFolders(); // Persist to SharedPreferences
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void saveFolders() {
        SharedPreferences prefs = getSharedPreferences("FolderPrefs", MODE_PRIVATE);
        JSONArray arr = new JSONArray();
        try {
            for (Subject s : folderList) {
                JSONObject obj = new JSONObject();
                obj.put("name", s.subjectName);
                obj.put("color", s.colorDrawable);
                arr.put(obj);
            }
        } catch (Exception ignored) {}
        prefs.edit().putString("folders", arr.toString()).apply();
    }

    private void loadFolders() {
        SharedPreferences prefs = getSharedPreferences("FolderPrefs", MODE_PRIVATE);
        folderList.clear();
        try {
            JSONArray arr = new JSONArray(prefs.getString("folders", "[]"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                folderList.add(new Subject(o.getString("name"), o.getString("color")));
            }
        } catch (Exception ignored) {}
    }

    private class FolderAdapter extends ArrayAdapter<Subject> {
        FolderAdapter(List<Subject> list) {
            super(FolderActivity.this, R.layout.item_subject_card, list);
        }

        @NonNull @Override
        public View getView(int position, @Nullable View v, @NonNull ViewGroup p) {
            if (v == null) v = LayoutInflater.from(getContext()).inflate(R.layout.item_subject_card, p, false);

            Subject s = getItem(position);
            TextView tvName = v.findViewById(R.id.text_subject_name);
            View folderContainer = v.findViewById(R.id.folderContainer);

            tvName.setText(s.subjectName);

            // Mapping color strings to actual color resources
            int colorRes = R.color.folder_pink; // Default
            if (s.colorDrawable.equals("folder_blue_full")) colorRes = R.color.folder_blue;
            else if (s.colorDrawable.equals("folder_green_full")) colorRes = R.color.folder_green;
            else if (s.colorDrawable.equals("folder_orange_full")) colorRes = R.color.folder_orange;
            else if (s.colorDrawable.equals("folder_yellow_full")) colorRes = R.color.folder_yellow;

            folderContainer.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(colorRes)));

            // Clicking a folder goes to its detail page
            v.setOnClickListener(view -> {
                Intent intent = new Intent(FolderActivity.this, CourseDetailActivity.class);
                intent.putExtra("COURSE_NAME", s.subjectName);
                startActivity(intent);
            });

            return v;
        }
    }

    static class Subject {
        String subjectName, colorDrawable;
        Subject(String n, String c) { this.subjectName = n; this.colorDrawable = c; }
    }
}