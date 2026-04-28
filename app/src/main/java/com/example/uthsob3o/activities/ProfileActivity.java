package com.example.uthsob3o.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.uthsob3o.R;

public class ProfileActivity extends AppCompatActivity {

    // Views
    TextView tvName, tvRole, tvLocation, tvEmail, tvPhone;
    TextView tvTotalListings, tvActiveBids, tvCompleted;
    LinearLayout btnEditProfile, btnMyListings, btnMyBids,
            btnSettings, btnLogout;
    LinearLayout navHome, navSearch, navAdd, navAlerts, navProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Connect views
        tvName = findViewById(R.id.tv_name);
        tvRole = findViewById(R.id.tv_role);
        tvLocation = findViewById(R.id.tv_location);
        tvEmail = findViewById(R.id.tv_email);
        tvPhone = findViewById(R.id.tv_phone);
        tvTotalListings = findViewById(R.id.tv_total_listings);
        tvActiveBids = findViewById(R.id.tv_active_bids);
        tvCompleted = findViewById(R.id.tv_completed);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnMyListings = findViewById(R.id.btn_my_listings);
        btnMyBids = findViewById(R.id.btn_my_bids);
        btnSettings = findViewById(R.id.btn_settings);
        btnLogout = findViewById(R.id.btn_logout);

        // Navigation
        navHome = findViewById(R.id.nav_home);
        navSearch = findViewById(R.id.nav_search);
        navAdd = findViewById(R.id.nav_add);
        navAlerts = findViewById(R.id.nav_alerts);
        navProfile = findViewById(R.id.nav_profile);

        // Get data from intent if passed
        String name = getIntent().getStringExtra("name");
        String role = getIntent().getStringExtra("role");

        // Set dummy profile data
        loadProfileData(name, role);

        // Edit Profile button
        btnEditProfile.setOnClickListener(v ->
                Toast.makeText(this, "Edit Profile - Coming Soon!", Toast.LENGTH_SHORT).show()
        );

        // My Listings button
        btnMyListings.setOnClickListener(v ->
                Toast.makeText(this, "My Listings - Coming Soon!", Toast.LENGTH_SHORT).show()
        );

        // My Bids button
        btnMyBids.setOnClickListener(v ->
                Toast.makeText(this, "My Bids - Coming Soon!", Toast.LENGTH_SHORT).show()
        );

        // Settings button
        btnSettings.setOnClickListener(v ->
                Toast.makeText(this, "Settings - Coming Soon!", Toast.LENGTH_SHORT).show()
        );

        // Logout button
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go back to login screen
                Intent intent = new Intent(ProfileActivity.this, LoginRegisterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                Toast.makeText(ProfileActivity.this,
                        "লগআউট সফল!", Toast.LENGTH_SHORT).show();
            }
        });

        // Bottom Navigation
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        navAlerts.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, NotificationActivity.class))
        );

        navAdd.setOnClickListener(v ->
                Toast.makeText(this, "Add Crop - Coming Soon!", Toast.LENGTH_SHORT).show()
        );

        navSearch.setOnClickListener(v ->
                Toast.makeText(this, "Search - Coming Soon!", Toast.LENGTH_SHORT).show()
        );

        navProfile.setOnClickListener(v -> {
            // Already here
        });
    }

    private void loadProfileData(String name, String role) {
        // Use passed name or default
        if (name != null && !name.isEmpty()) {
            tvName.setText(name);
        } else {
            tvName.setText("Abdul Karim");
        }

        // Set role
        if (role != null && role.equals("businessman")) {
            tvRole.setText("ব্যবসায়ী (BUSINESSMAN) • ✓ যাচাইকৃত");
            tvTotalListings.setText("12");
            tvActiveBids.setText("8");
            tvCompleted.setText("24");
        } else {
            tvRole.setText("কৃষক (FARMER) • ✓ যাচাইকৃত");
            tvTotalListings.setText("7");
            tvActiveBids.setText("3");
            tvCompleted.setText("15");
        }

        // Dummy data
        tvLocation.setText("📍 Bogura, Bangladesh");
        tvEmail.setText("✉ abdulkarim@gmail.com");
        tvPhone.setText("📞 +880 1712 345678");
    }
}