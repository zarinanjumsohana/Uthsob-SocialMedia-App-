package com.example.uthsob3o.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.uthsob3o.R;

public class LoginActivity extends AppCompatActivity {

    EditText etPhone, etPassword;
    Button btnLogin;
    TextView btnBack, btnForgot;
    ProgressBar progressBar;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnBack = findViewById(R.id.btn_back);
        progressBar = findViewById(R.id.progress_bar);

        btnBack.setOnClickListener(v -> finish());

        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("ফোন নম্বর দিন!");
            return;
        }
        if (phone.length() < 11) {
            etPhone.setError("সঠিক ফোন নম্বর দিন!");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("পাসওয়ার্ড দিন!");
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("পাসওয়ার্ড কমপক্ষে ৬ অক্ষর হতে হবে!");
            return;
        }

        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        btnLogin.setEnabled(false);

        // Convert phone to fake email format for Firebase Auth
        String fakeEmail = phone + "@uthsob.com";

        mAuth.signInWithEmailAndPassword(fakeEmail, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    // Get user role from Firestore
                    db.collection("users").document(uid).get()
                            .addOnSuccessListener(doc -> {
                                progressBar.setVisibility(View.GONE);
                                if (doc.exists()) {
                                    String role = doc.getString("role");
                                    // Go to Home with role
                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                    intent.putExtra("role", role);
                                    intent.putExtra("uid", uid);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                            | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                btnLogin.setEnabled(true);
                                Toast.makeText(this, "Error: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    Toast.makeText(this,
                            "লগইন ব্যর্থ! ফোন নম্বর বা পাসওয়ার্ড ভুল।",
                            Toast.LENGTH_SHORT).show();
                });
    }
}