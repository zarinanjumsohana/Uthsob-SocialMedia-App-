package com.example.uthsob3o.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.uthsob3o.R;

public class LoginRegisterActivity extends AppCompatActivity {

    Button btnLogin, btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);

        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);

        // LOGIN button → goes to Role Selection
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginRegisterActivity.this, RoleSelectionActivity.class);
                intent.putExtra("mode", "login");
                startActivity(intent);
            }
        });

        // REGISTER button → goes to Role Selection
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginRegisterActivity.this, RoleSelectionActivity.class);
                intent.putExtra("mode", "register");
                startActivity(intent);
            }
        });
    }
}