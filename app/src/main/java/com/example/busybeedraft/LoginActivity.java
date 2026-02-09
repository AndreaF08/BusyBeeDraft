package com.example.busybeedraft;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Bind Views with explicit types
        EditText etEmail = findViewById(R.id.etEmailLogin);
        EditText etPassword = findViewById(R.id.etPasswordLogin);
        AppCompatButton btnLogin = findViewById(R.id.btnLogin);
        TextView tvCreateAccount = findViewById(R.id.tvCreateAccount);

        btnLogin.setOnClickListener(v -> {
            String emailInput = etEmail.getText().toString().trim();
            String passInput = etPassword.getText().toString().trim();

            if (emailInput.isEmpty() || passInput.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Retrieve registered user data
            SharedPreferences prefs = getSharedPreferences("UserDB", MODE_PRIVATE);
            String registeredEmail = prefs.getString("email", null);
            String registeredPass = prefs.getString("password", null);

            if (emailInput.equals(registeredEmail) && passInput.equals(registeredPass)) {
                Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, TaskManagement.class));
                finish();
            } else {
                Toast.makeText(this, "Invalid email or password.", Toast.LENGTH_LONG).show();
            }
        });

        tvCreateAccount.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });
    }
}