package com.example.uthsob3o.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.uthsob3o.R;

public class ProfileActivity extends AppCompatActivity {

    TextView tvName, tvRole, tvLocation, tvPhone;
    TextView tvTotalListings, tvActiveBids, tvCompleted;
    LinearLayout btnEditProfile, btnMyListings, btnMyBids,
            btnSettings, btnLogout;
    LinearLayout navHome, navSearch, navAdd, navAlerts, navProfile;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String currentUid;
    String currentRole; // ← FIXED: added missing field

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        currentUid = getIntent().getStringExtra("uid");
        if (currentUid == null && mAuth.getCurrentUser() != null) {
            currentUid = mAuth.getCurrentUser().getUid();
        }

        // Connect views
        tvName = findViewById(R.id.tv_name);
        tvRole = findViewById(R.id.tv_role);
        tvLocation = findViewById(R.id.tv_location);
        tvPhone = findViewById(R.id.tv_phone);
        tvTotalListings = findViewById(R.id.tv_total_listings);
        tvActiveBids = findViewById(R.id.tv_active_bids);
        tvCompleted = findViewById(R.id.tv_completed);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnMyListings = findViewById(R.id.btn_my_listings);
        btnMyBids = findViewById(R.id.btn_my_bids);
        btnSettings = findViewById(R.id.btn_settings);
        btnLogout = findViewById(R.id.btn_logout);
        navHome = findViewById(R.id.nav_home);
        navSearch = findViewById(R.id.nav_search);
        navAdd = findViewById(R.id.nav_add);
        navAlerts = findViewById(R.id.nav_alerts);
        navProfile = findViewById(R.id.nav_profile);

        // Load real profile data
        loadProfileFromFirebase();
        loadStats();
        setupButtons();
    }

    private void loadProfileFromFirebase() {
        if (currentUid == null) return;

        db.collection("users").document(currentUid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        String role = doc.getString("role");
                        String location = doc.getString("location");
                        String phone = doc.getString("phone");
                        boolean verified = Boolean.TRUE.equals(
                                doc.getBoolean("verified"));

                        currentRole = role; // ← FIXED: store role in class field

                        tvName.setText(name != null ? name : "User");
                        tvLocation.setText("📍 " + (location != null ?
                                location : "Bangladesh"));
                        tvPhone.setText("📞 " + (phone != null ? phone : ""));

                        String verifiedBadge = verified ? " • ✓ যাচাইকৃত" : " • অপেক্ষায়";
                        if ("farmer".equals(role)) {
                            tvRole.setText("কৃষক (FARMER)" + verifiedBadge);
                        } else {
                            tvRole.setText("ব্যবসায়ী (BUSINESSMAN)" + verifiedBadge);
                        }
                    }
                });
    }

    private void loadStats() {
        if (currentUid == null) return;

        // Count crops posted by this farmer
        db.collection("crops")
                .whereEqualTo("farmerId", currentUid)
                .get()
                .addOnSuccessListener(snap ->
                        tvTotalListings.setText(String.valueOf(snap.size()))
                );

        // Count active bids by this businessman
        db.collection("bids")
                .whereEqualTo("businessmanId", currentUid)
                .get()
                .addOnSuccessListener(snap ->
                        tvActiveBids.setText(String.valueOf(snap.size()))
                );

        // Completed (sold crops)
        db.collection("crops")
                .whereEqualTo("farmerId", currentUid)
                .whereEqualTo("status", "sold")
                .get()
                .addOnSuccessListener(snap ->
                        tvCompleted.setText(String.valueOf(snap.size()))
                );
    }

    private void setupButtons() {
        btnEditProfile.setOnClickListener(v ->
                Toast.makeText(this, "Edit Profile - Coming Soon!",
                        Toast.LENGTH_SHORT).show()
        );

        btnMyListings.setOnClickListener(v -> {
            if ("farmer".equals(currentRole)) { // ← now works correctly
                startActivity(new Intent(this, MyCropsActivity.class));
            } else {
                Toast.makeText(this, "My Posts - Coming Soon!",
                        Toast.LENGTH_SHORT).show();
            }
        });

        btnMyBids.setOnClickListener(v ->
                Toast.makeText(this, "My Bids - Coming Soon!",
                        Toast.LENGTH_SHORT).show()
        );

        btnSettings.setOnClickListener(v ->
                Toast.makeText(this, "Settings - Coming Soon!",
                        Toast.LENGTH_SHORT).show()
        );

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(this, LoginRegisterActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            Toast.makeText(this, "লগআউট সফল!", Toast.LENGTH_SHORT).show();
        });

        navHome.setOnClickListener(v ->
                startActivity(new Intent(this, HomeActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        );

        navAlerts.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationActivity.class))
        );

        navAdd.setOnClickListener(v ->
                Toast.makeText(this, "Add - Go to Home first!",
                        Toast.LENGTH_SHORT).show()
        );

        navProfile.setOnClickListener(v -> {});
    }
}