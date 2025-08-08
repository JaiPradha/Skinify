package com.example.skinify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class RegistrationActivity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize UI components from the XML layout
        nameEditText = findViewById(R.id.et_name);
        emailEditText = findViewById(R.id.et_email);
        passwordEditText = findViewById(R.id.et_password);
        registerButton = findViewById(R.id.btn_register);

        // Set up the listener for the Register button
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // For now, we'll simply navigate to the main skin analysis screen.
                // We will add actual registration logic later (e.g., validation and database storage).
                Intent intent = new Intent(RegistrationActivity.this, SkinAnalysisActivity.class);
                startActivity(intent);
                finish(); // Close this activity so the user can't go back to the registration screen.
            }
        });
    }
}