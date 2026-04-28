package com.example.uthsob3o.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.uthsob3o.R;

public class FarmerRegisterActivity extends AppCompatActivity {

    EditText etName, etPhone, etEmail, etLocation;
    Button btnSubmit;
    TextView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_register);

        // Connect views
        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        etEmail = findViewById(R.id.et_email);
        etLocation = findViewById(R.id.et_location);
        btnSubmit = findViewById(R.id.btn_submit);
        btnBack = findViewById(R.id.btn_back);

        // Back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back
            }
        });

        // Submit button
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String location = etLocation.getText().toString().trim();

                // Validation
                if (TextUtils.isEmpty(name)) {
                    etName.setError("নাম দিন!");
                    return;
                }
                if (TextUtils.isEmpty(phone)) {
                    etPhone.setError("ফোন নম্বর দিন!");
                    return;
                }
                if (TextUtils.isEmpty(email)) {
                    etEmail.setError("ইমেইল দিন!");
                    return;
                }
                if (TextUtils.isEmpty(location)) {
                    etLocation.setError("অবস্থান দিন!");
                    return;
                }

                // All good — go to Home
                Toast.makeText(FarmerRegisterActivity.this,
                        "নিবন্ধন সফল! স্বাগতম " + name, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(FarmerRegisterActivity.this, HomeActivity.class);
                intent.putExtra("role", "farmer");
                intent.putExtra("name", name);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }
}