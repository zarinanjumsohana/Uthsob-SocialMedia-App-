package com.example.uthsob3o.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.example.uthsob3o.R;

public class SettingsActivity extends AppCompatActivity {

    TextView btnBack;
    LinearLayout btnChangePassword, btnPrivacy,
            btnAbout, btnLogout;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();

        btnBack = findViewById(R.id.btn_back);
        btnChangePassword = findViewById(R.id.btn_change_password);
        btnPrivacy = findViewById(R.id.btn_privacy);
        btnAbout = findViewById(R.id.btn_about);
        btnLogout = findViewById(R.id.btn_logout);

        btnBack.setOnClickListener(v -> finish());

        btnChangePassword.setOnClickListener(v ->
                Toast.makeText(this,
                        "পাসওয়ার্ড পরিবর্তন - Coming Soon!",
                        Toast.LENGTH_SHORT).show()
        );

        btnPrivacy.setOnClickListener(v ->
                Toast.makeText(this,
                        "গোপনীয়তা নীতি - Coming Soon!",
                        Toast.LENGTH_SHORT).show()
        );

        btnAbout.setOnClickListener(v ->
                Toast.makeText(this,
                        "UTH SOB v1.0 - Agricultural Marketplace",
                        Toast.LENGTH_SHORT).show()
        );

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(this,
                    LoginRegisterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}