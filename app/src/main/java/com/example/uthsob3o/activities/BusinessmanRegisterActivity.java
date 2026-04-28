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

public class BusinessmanRegisterActivity extends AppCompatActivity {

    EditText etName, etPhone, etEmail, etTin;
    Button btnSubmit;
    TextView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_businessman_register);

        etName = findViewById(R.id.et_name);
        etPhone = findViewById(R.id.et_phone);
        etEmail = findViewById(R.id.et_email);
        etTin = findViewById(R.id.et_tin);
        btnSubmit = findViewById(R.id.btn_submit);
        btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> finish());

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etName.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String tin = etTin.getText().toString().trim();

                if (TextUtils.isEmpty(name)) { etName.setError("নাম দিন!"); return; }
                if (TextUtils.isEmpty(phone)) { etPhone.setError("ফোন নম্বর দিন!"); return; }
                if (TextUtils.isEmpty(email)) { etEmail.setError("ইমেইল দিন!"); return; }
                if (TextUtils.isEmpty(tin)) { etTin.setError("TIN নম্বর দিন!"); return; }

                Toast.makeText(BusinessmanRegisterActivity.this,
                        "নিবন্ধন সফল! স্বাগতম " + name, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(BusinessmanRegisterActivity.this, HomeActivity.class);
                intent.putExtra("role", "businessman");
                intent.putExtra("name", name);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }
}