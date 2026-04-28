package com.example.uthsob3o.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.uthsob3o.R;

public class RoleSelectionActivity extends AppCompatActivity {

    CardView cardFarmer, cardBusinessman;
    String mode; // "login" or "register"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        // Get mode passed from previous screen
        mode = getIntent().getStringExtra("mode");

        cardFarmer = findViewById(R.id.card_farmer);
        cardBusinessman = findViewById(R.id.card_businessman);

        // Farmer card clicked
        cardFarmer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode.equals("register")) {
                    // Go to Farmer Registration
                    Intent intent = new Intent(RoleSelectionActivity.this, FarmerRegisterActivity.class);
                    startActivity(intent);
                } else {
                    // Go to Home (login as farmer)
                    Intent intent = new Intent(RoleSelectionActivity.this, HomeActivity.class);
                    intent.putExtra("role", "farmer");
                    startActivity(intent);
                }
            }
        });

        // Businessman card clicked
        cardBusinessman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode.equals("register")) {
                    // Go to Businessman Registration
                    Intent intent = new Intent(RoleSelectionActivity.this, BusinessmanRegisterActivity.class);
                    startActivity(intent);
                } else {
                    // Go to Home (login as businessman)
                    Intent intent = new Intent(RoleSelectionActivity.this, HomeActivity.class);
                    intent.putExtra("role", "businessman");
                    startActivity(intent);
                }
            }
        });
    }
}