package com.example.uthsob3o.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.uthsob3o.ImageUploadHelper;
import com.example.uthsob3o.NavigationHelper;
import com.example.uthsob3o.R;

public class ProfileActivity extends AppCompatActivity {

    // Profile header views
    ImageView ivProfilePhoto;
    LinearLayout btnEditPhoto;
    TextView tvName, tvRole, tvLocation, tvPhone;
    TextView tvVerifiedBadge;

    // Stats views
    TextView tvTotalListings, tvActiveBids, tvCompleted;

    // Menu buttons
    LinearLayout btnEditProfile, btnMyListings,
            btnMyBids, btnSettings,
            btnGetVerified, btnLogout;

    // Bottom nav
    LinearLayout navHome, navSearch, navAdd,
            navAlerts, navProfile;

    // Firebase
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String currentUid;
    String currentRole = "";

    // Image picker for profile photo
    ActivityResultLauncher<String> photoPickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            uploadProfilePhoto(uri);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get uid and role
        currentUid = getIntent().getStringExtra("uid");
        currentRole = getIntent()
                .getStringExtra("role") != null
                ? getIntent().getStringExtra("role") : "";

        // Fallback to Firebase Auth
        if (currentUid == null
                && mAuth.getCurrentUser() != null) {
            currentUid = mAuth.getCurrentUser().getUid();
        }

        initViews();
        loadProfileFromFirebase();
        loadStats();
        setupButtons();
    }

    private void initViews() {
        // Profile photo
        ivProfilePhoto = findViewById(
                R.id.iv_profile_photo);
        btnEditPhoto = findViewById(R.id.btn_edit_photo);

        // Profile info
        tvName = findViewById(R.id.tv_name);
        tvRole = findViewById(R.id.tv_role);
        tvLocation = findViewById(R.id.tv_location);
        tvPhone = findViewById(R.id.tv_phone);
        tvVerifiedBadge = findViewById(
                R.id.verified_badge_icon);

        // Stats
        tvTotalListings = findViewById(
                R.id.tv_total_listings);
        tvActiveBids = findViewById(R.id.tv_active_bids);
        tvCompleted = findViewById(R.id.tv_completed);

        // Menu buttons
        btnEditProfile = findViewById(
                R.id.btn_edit_profile);
        btnMyListings = findViewById(
                R.id.btn_my_listings);
        btnMyBids = findViewById(R.id.btn_my_bids);
        btnSettings = findViewById(R.id.btn_settings);
        btnGetVerified = findViewById(
                R.id.btn_get_verified);
        btnLogout = findViewById(R.id.btn_logout);

        // Bottom nav
        navHome = findViewById(R.id.nav_home);
        navSearch = findViewById(R.id.nav_search);
        navAdd = findViewById(R.id.nav_add);
        navAlerts = findViewById(R.id.nav_alerts);
        navProfile = findViewById(R.id.nav_profile);
    }

    private void loadProfileFromFirebase() {
        if (currentUid == null) return;

        db.collection("users")
                .document(currentUid).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    String name = doc.getString("name");
                    String role = doc.getString("role");
                    String location =
                            doc.getString("location");
                    String phone = doc.getString("phone");
                    String photoUrl =
                            doc.getString("photoUrl");
                    boolean verified = Boolean.TRUE.equals(
                            doc.getBoolean("verified"));

                    // Set name
                    if (name != null) {
                        tvName.setText(name);
                    }

                    // Set location and phone
                    tvLocation.setText("📍 "
                            + (location != null
                            ? location : "Bangladesh"));
                    if (tvPhone != null) {
                        tvPhone.setText("📞 "
                                + (phone != null ? phone : ""));
                    }

                    // Set role badge
                    String badge = verified
                            ? " • ✓ যাচাইকৃত"
                            : " • অপেক্ষায়";

                    if ("farmer".equals(role)) {
                        currentRole = "farmer";
                        tvRole.setText(
                                "কৃষক (FARMER)" + badge);
                    } else {
                        currentRole = "businessman";
                        tvRole.setText(
                                "ব্যবসায়ী (BUSINESSMAN)" + badge);
                    }

                    // Show verified badge icon
                    if (tvVerifiedBadge != null) {
                        tvVerifiedBadge.setVisibility(
                                verified ? View.VISIBLE
                                        : View.GONE);
                    }

                    // Load profile photo
                    if (photoUrl != null
                            && !photoUrl.isEmpty()
                            && ivProfilePhoto != null) {
                        Glide.with(this)
                                .load(photoUrl)
                                .circleCrop()
                                .placeholder(
                                        android.R.drawable
                                                .ic_menu_myplaces)
                                .into(ivProfilePhoto);
                    }
                });
    }

    private void loadStats() {
        if (currentUid == null) return;

        // Count farmer's crop listings
        db.collection("crops")
                .whereEqualTo("farmerId", currentUid)
                .get()
                .addOnSuccessListener(snap -> {
                    if (tvTotalListings != null) {
                        tvTotalListings.setText(
                                String.valueOf(snap.size()));
                    }
                });

        // Count bids placed by businessman
        db.collection("bids")
                .whereEqualTo("businessmanId", currentUid)
                .get()
                .addOnSuccessListener(snap -> {
                    if (tvActiveBids != null) {
                        tvActiveBids.setText(
                                String.valueOf(snap.size()));
                    }
                });

        // Count completed/sold crops
        db.collection("crops")
                .whereEqualTo("farmerId", currentUid)
                .whereEqualTo("status", "sold")
                .get()
                .addOnSuccessListener(snap -> {
                    if (tvCompleted != null) {
                        tvCompleted.setText(
                                String.valueOf(snap.size()));
                    }
                });
    }

    private void uploadProfilePhoto(Uri uri) {
        Toast.makeText(this,
                "ছবি আপলোড হচ্ছে...",
                Toast.LENGTH_SHORT).show();

        ImageUploadHelper.uploadImage(
                this, uri,
                new ImageUploadHelper.UploadCallback() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        // Save URL to Firestore
                        db.collection("users")
                                .document(currentUid)
                                .update("photoUrl", imageUrl)
                                .addOnSuccessListener(unused -> {
                                    // Show new photo
                                    Glide.with(
                                                    ProfileActivity.this)
                                            .load(imageUrl)
                                            .circleCrop()
                                            .into(ivProfilePhoto);
                                    Toast.makeText(
                                                    ProfileActivity.this,
                                                    "প্রোফাইল ছবি আপডেট! ✅",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                });
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(
                                ProfileActivity.this,
                                "আপলোড ব্যর্থ: " + error,
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setupButtons() {

        // Edit photo button
        if (btnEditPhoto != null) {
            btnEditPhoto.setOnClickListener(v ->
                    photoPickerLauncher.launch("image/*")
            );
        }

        // Click profile photo also opens picker
        if (ivProfilePhoto != null) {
            ivProfilePhoto.setOnClickListener(v ->
                    photoPickerLauncher.launch("image/*")
            );
        }

        // Edit Profile
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v ->
                    startActivity(new Intent(this,
                            EditProfileActivity.class))
            );
        }

        // My Listings
        if (btnMyListings != null) {
            btnMyListings.setOnClickListener(v -> {
                if ("farmer".equals(currentRole)) {
                    startActivity(new Intent(this,
                            MyCropsActivity.class));
                } else {
                    Toast.makeText(this,
                            "My Posts - Coming Soon!",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        // My Bids
        if (btnMyBids != null) {
            btnMyBids.setOnClickListener(v ->
                    startActivity(new Intent(this,
                            MyBidsActivity.class))
            );
        }

        // Settings
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v ->
                    startActivity(new Intent(this,
                            SettingsActivity.class))
            );
        }

        // Get Verified
        if (btnGetVerified != null) {
            btnGetVerified.setOnClickListener(v ->
                    startActivity(new Intent(this,
                            AdminVerificationActivity.class))
            );
        }

        // Logout
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                mAuth.signOut();
                Intent intent = new Intent(this,
                        LoginRegisterActivity.class);
                intent.setFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                Toast.makeText(this,
                        "লগআউট সফল!",
                        Toast.LENGTH_SHORT).show();
            });
        }

        // Bottom navigation
        NavigationHelper.setupBottomNav(
                this, navHome, navSearch, navAdd,
                navAlerts, navProfile,
                currentRole, currentUid, "profile"
        );

        NavigationHelper.highlightTab(
                navHome, navSearch, navAlerts, navProfile,
                "profile",
                getResources().getColor(R.color.dark_green),
                getResources().getColor(R.color.gray)
        );
    }
}