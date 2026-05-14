package com.example.uthsob3o.activities;

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
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    EditText etName, etLocation, etPhone;
    Button btnSave;
    TextView btnBack;
    ProgressBar progressBar;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            currentUid = mAuth.getCurrentUser().getUid();
        }

        etName = findViewById(R.id.et_name);
        etLocation = findViewById(R.id.et_location);
        etPhone = findViewById(R.id.et_phone);
        btnSave = findViewById(R.id.btn_save);
        btnBack = findViewById(R.id.btn_back);
        progressBar = findViewById(R.id.progress_bar);

        btnBack.setOnClickListener(v -> finish());

        // Load current data
        loadCurrentProfile();

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void loadCurrentProfile() {
        if (currentUid == null) return;
        db.collection("users").document(currentUid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        etName.setText(doc.getString("name"));
                        etLocation.setText(doc.getString("location"));
                        etPhone.setText(doc.getString("phone"));
                    }
                });
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("নাম দিন!"); return; }
        if (TextUtils.isEmpty(location)) {
            etLocation.setError("অবস্থান দিন!"); return; }

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("location", location);

        db.collection("users").document(currentUid)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this,
                            "প্রোফাইল আপডেট হয়েছে! ✅",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}