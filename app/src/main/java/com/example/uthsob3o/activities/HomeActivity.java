package com.example.uthsob3o.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.example.uthsob3o.R;
import com.example.uthsob3o.adapters.FeedAdapter;
import com.example.uthsob3o.adapters.StoryAdapter;
import com.example.uthsob3o.models.CropModel;
import com.example.uthsob3o.models.UserModel;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    RecyclerView rvFeed, rvStories;
    LinearLayout navHome, navSearch, navAdd, navAlerts, navProfile;
    TextView btnNotification;
    ImageView btnProfile;
    EditText searchBar;

    List<CropModel> cropList = new ArrayList<>();
    List<CropModel> allCrops = new ArrayList<>();
    List<UserModel> storyList = new ArrayList<>();

    FeedAdapter feedAdapter;
    StoryAdapter storyAdapter;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    ListenerRegistration cropListener;

    String currentRole = "";
    String currentUid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get role and uid
        currentRole = getIntent().getStringExtra("role");
        currentUid = getIntent().getStringExtra("uid");

        // If no uid from intent, get from Firebase Auth
        if (currentUid == null && mAuth.getCurrentUser() != null) {
            currentUid = mAuth.getCurrentUser().getUid();
        }

        // If still no role, fetch from Firestore
        if (currentRole == null && currentUid != null) {
            fetchUserRole();
        }

        initViews();
        setupRecyclerViews();
        loadStories();
        loadCropsFromFirebase();
        setupSearch();
        setupNavigation();
    }

    private void fetchUserRole() {
        db.collection("users").document(currentUid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        currentRole = doc.getString("role");
                        updateUIForRole();
                    }
                });
    }

    private void updateUIForRole() {
        // Show/hide features based on role
        // Farmers see "Add Crop", Businessmen see "Add Post"
    }

    private void initViews() {
        rvFeed = findViewById(R.id.rv_feed);
        rvStories = findViewById(R.id.rv_stories);
        navHome = findViewById(R.id.nav_home);
        navSearch = findViewById(R.id.nav_search);
        navAdd = findViewById(R.id.nav_add);
        navAlerts = findViewById(R.id.nav_alerts);
        navProfile = findViewById(R.id.nav_profile);
        btnNotification = findViewById(R.id.btn_notification);
        btnProfile = findViewById(R.id.btn_profile);
        searchBar = findViewById(R.id.search_bar);
    }

    private void setupRecyclerViews() {
        // Stories
        storyAdapter = new StoryAdapter(this, storyList);
        rvStories.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvStories.setAdapter(storyAdapter);

        // Feed
        feedAdapter = new FeedAdapter(this, cropList, currentRole, currentUid);
        rvFeed.setLayoutManager(new LinearLayoutManager(this));
        rvFeed.setAdapter(feedAdapter);
    }

    private void loadCropsFromFirebase() {
        // Real-time listener for crops
        cropListener = db.collection("crops")
                .whereEqualTo("status", "active")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        // Fall back to dummy data if Firebase fails
                        loadDummyCrops();
                        return;
                    }
                    if (snapshots != null) {
                        allCrops.clear();
                        for (var doc : snapshots.getDocuments()) {
                            CropModel crop = doc.toObject(CropModel.class);
                            if (crop != null) {
                                crop.setCropId(doc.getId());
                                allCrops.add(crop);
                            }
                        }
                        cropList.clear();
                        cropList.addAll(allCrops);
                        feedAdapter.notifyDataSetChanged();

                        // If no real data, show dummy
                        if (allCrops.isEmpty()) {
                            loadDummyCrops();
                        }
                    }
                });
    }

    private void setupSearch() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCrops(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterCrops(String query) {
        cropList.clear();
        if (query.isEmpty()) {
            cropList.addAll(allCrops);
        } else {
            String lowerQuery = query.toLowerCase();
            for (CropModel crop : allCrops) {
                if (crop.getCropName().toLowerCase().contains(lowerQuery)
                        || crop.getFarmerName().toLowerCase().contains(lowerQuery)
                        || crop.getFarmerLocation().toLowerCase().contains(lowerQuery)) {
                    cropList.add(crop);
                }
            }
        }
        feedAdapter.notifyDataSetChanged();
    }

    private void loadStories() {
        storyList.clear();
        storyList.add(new UserModel("story1", "Your Story", "", "", ""));
        storyList.add(new UserModel("story2", "Karim", "", "", ""));
        storyList.add(new UserModel("story3", "Rahat", "", "", ""));
        storyList.add(new UserModel("story4", "Sohail", "", "", ""));
        storyList.add(new UserModel("story5", "Fatema", "", "", ""));
        storyAdapter.notifyDataSetChanged();
    }

    private void loadDummyCrops() {
        allCrops.clear();
        allCrops.add(new CropModel("1", "dummy1", "Abdul Karim",
                "Bogura, Bangladesh", true,
                "Premium Diamond Potatoes", "5000", "KG", 25, "2025-12-31"));
        allCrops.add(new CropModel("2", "dummy2", "Rahim Uddin",
                "Rajshahi, Bangladesh", true,
                "Himsagar Mangoes", "2000", "KG", 120, "2025-12-31"));
        allCrops.add(new CropModel("3", "dummy3", "Kamal Hossain",
                "Dinajpur, Bangladesh", false,
                "Organic Rice BR-28", "10000", "KG", 45, "2025-12-31"));
        cropList.clear();
        cropList.addAll(allCrops);
        feedAdapter.notifyDataSetChanged();
    }

    private void setupNavigation() {
        navHome.setOnClickListener(v -> {
            // Already on home
        });

        navAlerts.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationActivity.class))
        );

        btnNotification.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationActivity.class))
        );

        navAdd.setOnClickListener(v -> {
            if ("farmer".equals(currentRole)) {
                startActivity(new Intent(this, AddCropActivity.class));
            } else {
                startActivity(new Intent(this, AddPostActivity.class));
            }
        });

        navSearch.setOnClickListener(v ->
                searchBar.requestFocus()
        );

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("uid", currentUid);
            intent.putExtra("role", currentRole);
            startActivity(intent);
        });

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("uid", currentUid);
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cropListener != null) cropListener.remove();
    }
}