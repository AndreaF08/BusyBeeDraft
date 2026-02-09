package com.example.busybeedraft;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // This ID now matches the ImageButton we added to your XML
        ImageButton btnBack = findViewById(R.id.btnBackSettings);
        Button btnResetCard = findViewById(R.id.btnResetCard);
        Button btnSignOut = findViewById(R.id.btnSignOut);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        btnResetCard.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Reset ID Card")
                    .setMessage("This will clear your saved name and birthday. Proceed?")
                    .setPositiveButton("Reset", (dialog, which) -> {
                        getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().clear().apply();
                        Toast.makeText(this, "ID Card Reset", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        btnSignOut.setOnClickListener(v -> {
            // FIX C: Explicitly clear the user session to prevent account data bleed
            android.content.SharedPreferences sessionPrefs = getSharedPreferences("UserSession", MODE_PRIVATE);
            sessionPrefs.edit().clear().apply();

            // Redirect to Login and clear the activity backstack
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}