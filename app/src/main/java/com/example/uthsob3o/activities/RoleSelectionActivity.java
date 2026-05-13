package com.example.uthsob3o.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.uthsob3o.R;

public class RoleSelectionActivity extends AppCompatActivity {

    CardView cardFarmer, cardBusinessman;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        cardFarmer = findViewById(R.id.card_farmer);
        cardBusinessman = findViewById(R.id.card_businessman);

        if (cardFarmer != null) {
            cardFarmer.setOnClickListener(v ->
                    startActivity(new Intent(this,
                            FarmerRegisterActivity.class))
            );
        }

        if (cardBusinessman != null) {
            cardBusinessman.setOnClickListener(v ->
                    startActivity(new Intent(this,
                            BusinessmanRegisterActivity.class))
            );
        }
    }
}