package com.example.busybeedraft;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Bind Views - Types must be declared to fix "Cannot resolve symbol"
        EditText etFullName = findViewById(R.id.etFullName);
        EditText etEmail = findViewById(R.id.etEmailSignup);
        EditText etPassword = findViewById(R.id.etPasswordSignup);
        AppCompatButton btnSignup = findViewById(R.id.btnSignup);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnSignup.setOnClickListener(v -> {
            String name = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            } else if (password.length() < 8) {
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            } else {
                // Save to SharedPreferences
                SharedPreferences.Editor editor = getSharedPreferences("UserDB", MODE_PRIVATE).edit();
                editor.putString("email", email);
                editor.putString("password", password);
                editor.putString("name", name);
                editor.apply();

                Toast.makeText(this, "Account Created!", Toast.LENGTH_SHORT).show();

                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                finish();
            }
        });

        tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }
}