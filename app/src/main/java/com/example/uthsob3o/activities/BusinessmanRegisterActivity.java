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
import com.example.uthsob3o.models.UserModel;

public class BusinessmanRegisterActivity extends AppCompatActivity {

    EditText etName, etPhone, etLocation, etDinNumber, etNationalId, etPassword;
    Button btnSubmit;
    TextView btnBack;
    ProgressBar progressBar;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_businessman_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        etLocation = findViewById(R.id.et_location);
        etDinNumber = findViewById(R.id.et_din_number);
        etNationalId = findViewById(R.id.et_national_id);
        etPassword = findViewById(R.id.et_password);
        btnSubmit = findViewById(R.id.btn_submit);
        btnBack = findViewById(R.id.btn_back);
        progressBar = findViewById(R.id.progress_bar);

        btnBack.setOnClickListener(v -> finish());
        btnSubmit.setOnClickListener(v -> registerBusinessman());
    }

    private void registerBusinessman() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String dinNumber = etDinNumber.getText().toString().trim();
        String nationalId = etNationalId.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) { etName.setError("নাম দিন!"); return; }
        if (TextUtils.isEmpty(phone) || phone.length() < 11) {
            etPhone.setError("সঠিক ফোন নম্বর দিন!"); return; }
        if (TextUtils.isEmpty(location)) { etLocation.setError("অবস্থান দিন!"); return; }
        if (TextUtils.isEmpty(dinNumber)) { etDinNumber.setError("DIN নম্বর দিন!"); return; }
        if (TextUtils.isEmpty(nationalId)) { etNationalId.setError("NID নম্বর দিন!"); return; }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("পাসওয়ার্ড কমপক্ষে ৬ অক্ষর!"); return; }

        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        String fakeEmail = phone + "@uthsob.com";

        mAuth.createUserWithEmailAndPassword(fakeEmail, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();

                    UserModel user = new UserModel(uid, name, phone, location, "businessman");
                    user.setDinNumber(dinNumber);
                    user.setNationalId(nationalId);

                    db.collection("users").document(uid)
                            .set(user)
                            .addOnSuccessListener(unused -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(this,
                                        "নিবন্ধন সফল! স্বাগতম " + name,
                                        Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(this, HomeActivity.class);
                                intent.putExtra("role", "businessman");
                                intent.putExtra("uid", uid);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                btnSubmit.setEnabled(true);
                                Toast.makeText(this, "Error: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    String msg = e.getMessage();
                    if (msg != null && msg.contains("already in use")) {
                        Toast.makeText(this,
                                "এই ফোন নম্বর ইতিমধ্যে নিবন্ধিত!",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Error: " + msg,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}