package com.example.uthsob3o.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.example.uthsob3o.NavigationHelper;
import com.example.uthsob3o.R;
import com.example.uthsob3o.adapters.FeedAdapter;
import com.example.uthsob3o.adapters.PostAdapter;
import com.example.uthsob3o.adapters.StoryAdapter;
import com.example.uthsob3o.models.CropModel;
import com.example.uthsob3o.models.PostModel;
import com.example.uthsob3o.models.StoryModel;
import java.util.ArrayList;
import java.util.List;
import android.os.Build;

public class HomeActivity extends AppCompatActivity {

    // Views
    RecyclerView rvFeed, rvStories, rvPosts;
    LinearLayout navHome, navSearch, navAdd,
            navAlerts, navProfile;
    TextView btnNotification;
    ImageView btnProfile;

    // Data
    List<CropModel> cropList = new ArrayList<>();
    List<CropModel> allCrops = new ArrayList<>();
    List<StoryModel> storyList = new ArrayList<>();
    List<PostModel> postList = new ArrayList<>();

    // Adapters
    FeedAdapter feedAdapter;
    StoryAdapter storyAdapter;
    PostAdapter postAdapter;

    // Firebase
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    ListenerRegistration cropListener;
    ListenerRegistration postListener;

    // User info
    String currentRole = "";
    String currentUid = "";
    String currentUserName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Get from intent
        currentRole = getIntent()
                .getStringExtra("role") != null
                ? getIntent().getStringExtra("role") : "";
        currentUid = getIntent()
                .getStringExtra("uid") != null
                ? getIntent().getStringExtra("uid") : "";

        // Fallback from Firebase Auth
        if (currentUid.isEmpty()
                && mAuth.getCurrentUser() != null) {
            currentUid = mAuth.getCurrentUser().getUid();
        }

        // Get role + name from Firebase
        if (!currentUid.isEmpty()) {
            db.collection("users")
                    .document(currentUid).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            if (currentRole.isEmpty()) {
                                String role =
                                        doc.getString("role");
                                if (role != null) {
                                    currentRole = role;
                                }
                            }
                            String name =
                                    doc.getString("name");
                            if (name != null) {
                                currentUserName = name;
                                // Update adapters with name
                                if (postAdapter != null) {
                                    postAdapter
                                            .updateUserName(name);
                                }
                            }
                        }
                    });
        }
        // Request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(
                    android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{
                                android.Manifest.permission.POST_NOTIFICATIONS
                        }, 100);
            }
        }


        initViews();
        setupRecyclerViews();
        loadStories();
        loadCropsFromFirebase();
        loadPostsFromFirebase();
        setupNavigation();
    }

    private void initViews() {
        rvFeed = findViewById(R.id.rv_feed);
        rvStories = findViewById(R.id.rv_stories);
        rvPosts = findViewById(R.id.rv_posts);
        navHome = findViewById(R.id.nav_home);
        navSearch = findViewById(R.id.nav_search);
        navAdd = findViewById(R.id.nav_add);
        navAlerts = findViewById(R.id.nav_alerts);
        navProfile = findViewById(R.id.nav_profile);
        btnNotification = findViewById(
                R.id.btn_notification);
        btnProfile = findViewById(R.id.btn_profile);
    }

    private void setupRecyclerViews() {
        // Stories horizontal
        storyAdapter = new StoryAdapter(
                this, storyList, true);
        rvStories.setLayoutManager(
                new LinearLayoutManager(this,
                        LinearLayoutManager.HORIZONTAL, false));
        rvStories.setAdapter(storyAdapter);

        // Crop Feed vertical
        feedAdapter = new FeedAdapter(
                this, cropList, currentRole, currentUid);
        rvFeed.setLayoutManager(
                new LinearLayoutManager(this));
        rvFeed.setAdapter(feedAdapter);

        // Posts Feed vertical
        postAdapter = new PostAdapter(
                this, postList, currentUid, currentUserName);
        rvPosts.setLayoutManager(
                new LinearLayoutManager(this));
        rvPosts.setAdapter(postAdapter);
    }

    // Load stories from Firebase
    private void loadStories() {
        long now = System.currentTimeMillis();
        db.collection("stories")
                .whereGreaterThan("expiresAt", now)
                .get()
                .addOnSuccessListener(snapshots -> {
                    storyList.clear();
                    for (var doc : snapshots.getDocuments()) {
                        StoryModel story =
                                doc.toObject(StoryModel.class);
                        if (story != null) {
                            storyList.add(story);
                        }
                    }
                    storyAdapter.notifyDataSetChanged();
                });
    }

    // Load crops from Firebase (Farmer posts)
    private void loadCropsFromFirebase() {
        cropListener = db.collection("crops")
                .whereEqualTo("status", "active")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        // Show empty state instead of dummy
                        cropList.clear();
                        feedAdapter.notifyDataSetChanged();
                        return;
                    }
                    if (snapshots != null) {
                        allCrops.clear();
                        for (var doc : snapshots.getDocuments()) {
                            CropModel crop =
                                    doc.toObject(CropModel.class);
                            if (crop != null) {
                                crop.setCropId(doc.getId());
                                allCrops.add(crop);
                            }
                        }
                        cropList.clear();
                        cropList.addAll(allCrops);
                        feedAdapter.notifyDataSetChanged();
                    }
                });
    }

    // Load businessman posts from Firebase
    private void loadPostsFromFirebase() {
        postListener = db.collection("posts")
                .whereEqualTo("status", "active")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) return;
                    if (snapshots != null) {
                        postList.clear();
                        for (var doc
                                : snapshots.getDocuments()) {
                            PostModel post =
                                    doc.toObject(PostModel.class);
                            if (post != null) {
                                post.setPostId(doc.getId());
                                postList.add(post);
                            }
                        }
                        postAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void setupNavigation() {
        NavigationHelper.setupBottomNav(
                this, navHome, navSearch, navAdd,
                navAlerts, navProfile,
                currentRole, currentUid, "home"
        );

        NavigationHelper.highlightTab(
                navHome, navSearch, navAlerts, navProfile,
                "home",
                getResources().getColor(R.color.dark_green),
                getResources().getColor(R.color.gray)
        );

        btnNotification.setOnClickListener(v -> {
            Intent intent = new Intent(this,
                    NotificationActivity.class);
            intent.putExtra("role", currentRole);
            intent.putExtra("uid", currentUid);
            startActivity(intent);
        });

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this,
                    ProfileActivity.class);
            intent.putExtra("role", currentRole);
            intent.putExtra("uid", currentUid);
            startActivity(intent);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cropListener != null) cropListener.remove();
        if (postListener != null) postListener.remove();
    }
}