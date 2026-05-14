package com.example.uthsob3o.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.example.uthsob3o.NotificationHelper;
import com.example.uthsob3o.R;
import com.example.uthsob3o.adapters.BidHistoryAdapter;
import com.example.uthsob3o.adapters.FeedAdapter;
import com.example.uthsob3o.adapters.PostAdapter;
import com.example.uthsob3o.models.BidModel;
import com.example.uthsob3o.models.CropModel;
import com.example.uthsob3o.models.PostModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {

    // Header views
    ImageView ivAvatar;
    TextView tvName, tvRole, tvLocation;
    TextView tvFollowers, tvFollowing,
            tvRating, tvListings, tvBids;
    Button btnFollow;
    TextView btnBack;

    // Tab views
    LinearLayout tabPosts, tabBids, tabDetails;
    View indicatorPosts, indicatorBids,
            indicatorDetails;

    // Content
    RecyclerView rvUserContent;

    // Firebase
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String currentUid = "";
    String profileUid = "";
    String profileRole = "";
    String currentUserName = "";
    boolean isFollowing = false;

    // Data lists
    List<CropModel> cropList = new ArrayList<>();
    List<PostModel> postList = new ArrayList<>();
    List<BidModel> bidList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            currentUid = mAuth.getCurrentUser().getUid();
        }

        profileUid = getIntent()
                .getStringExtra("profileUid") != null
                ? getIntent().getStringExtra("profileUid")
                : "";

        if (profileUid.isEmpty()) {
            Toast.makeText(this,
                    "User not found!",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        loadCurrentUserName();
        loadUserProfile();
        loadUserStats();
        checkFollowStatus();
        setupTabs();
        loadCropPosts(); // Default tab = Posts
    }

    private void initViews() {
        ivAvatar = findViewById(R.id.iv_avatar);
        tvName = findViewById(R.id.tv_name);
        tvRole = findViewById(R.id.tv_role);
        tvLocation = findViewById(R.id.tv_location);
        tvFollowers = findViewById(R.id.tv_followers);
        tvFollowing = findViewById(R.id.tv_following);
        tvRating = findViewById(R.id.tv_rating);
        tvListings = findViewById(R.id.tv_listings);
        tvBids = findViewById(R.id.tv_bids);
        btnFollow = findViewById(R.id.btn_follow);
        btnBack = findViewById(R.id.btn_back);
        rvUserContent = findViewById(
                R.id.rv_user_posts);
        tabPosts = findViewById(R.id.tab_posts);
        tabBids = findViewById(R.id.tab_bids);
        tabDetails = findViewById(R.id.tab_details);
        indicatorPosts = findViewById(
                R.id.indicator_posts);
        indicatorBids = findViewById(
                R.id.indicator_bids);
        indicatorDetails = findViewById(
                R.id.indicator_details);

        btnBack.setOnClickListener(v -> finish());

        // Hide follow on own profile
        if (currentUid.equals(profileUid)) {
            btnFollow.setVisibility(View.GONE);
        } else {
            btnFollow.setOnClickListener(
                    v -> toggleFollow());
        }
    }

    private void loadCurrentUserName() {
        if (currentUid.isEmpty()) return;
        db.collection("users")
                .document(currentUid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        if (name != null) {
                            currentUserName = name;
                        }
                    }
                });
    }

    private void loadUserProfile() {
        db.collection("users")
                .document(profileUid).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    String name = doc.getString("name");
                    String role = doc.getString("role");
                    String location =
                            doc.getString("location");
                    String photoUrl =
                            doc.getString("photoUrl");
                    boolean verified =
                            Boolean.TRUE.equals(
                                    doc.getBoolean("verified"));

                    profileRole = role != null ? role : "";

                    if (name != null) {
                        tvName.setText(name);
                    }

                    tvLocation.setText("📍 "
                            + (location != null
                            ? location : "Bangladesh"));

                    String badge = verified ? " ✓" : "";
                    if ("farmer".equals(role)) {
                        tvRole.setText(
                                "🌾 কৃষক" + badge);
                    } else {
                        tvRole.setText(
                                "💼 ব্যবসায়ী" + badge);
                    }

                    // Load avatar photo
                    if (photoUrl != null
                            && !photoUrl.isEmpty()) {
                        Glide.with(this)
                                .load(photoUrl)
                                .circleCrop()
                                .placeholder(
                                        android.R.drawable
                                                .ic_menu_myplaces)
                                .into(ivAvatar);
                    }
                });
    }

    private void loadUserStats() {
        // Followers count
        db.collection("follows")
                .whereEqualTo("followingId", profileUid)
                .get()
                .addOnSuccessListener(snap ->
                        tvFollowers.setText(
                                snap.size() + "\nFollowers")
                );

        // Following count
        db.collection("follows")
                .whereEqualTo("followerId", profileUid)
                .get()
                .addOnSuccessListener(snap ->
                        tvFollowing.setText(
                                snap.size() + "\nFollowing")
                );

        // Rating placeholder
        tvRating.setText("4.5⭐\nRating");

        // Listings count
        db.collection("crops")
                .whereEqualTo("farmerId", profileUid)
                .get()
                .addOnSuccessListener(snap ->
                        tvListings.setText(
                                snap.size() + "\nListings")
                );

        // Bids count
        db.collection("bids")
                .whereEqualTo("businessmanId", profileUid)
                .get()
                .addOnSuccessListener(snap ->
                        tvBids.setText(
                                snap.size() + "\nBids")
                );
    }

    private void checkFollowStatus() {
        if (currentUid.isEmpty()
                || currentUid.equals(profileUid)) {
            return;
        }

        String followId = currentUid + "_" + profileUid;
        db.collection("follows")
                .document(followId).get()
                .addOnSuccessListener(doc -> {
                    isFollowing = doc.exists();
                    updateFollowButton();
                });
    }

    private void toggleFollow() {
        if (currentUid.isEmpty()) {
            Toast.makeText(this,
                    "লগইন করুন!", Toast.LENGTH_SHORT).show();
            return;
        }

        String followId = currentUid + "_" + profileUid;

        if (isFollowing) {
            // Unfollow
            db.collection("follows")
                    .document(followId).delete()
                    .addOnSuccessListener(unused -> {
                        isFollowing = false;
                        updateFollowButton();
                        loadUserStats();
                        Toast.makeText(this,
                                "আনফলো করা হয়েছে",
                                Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Follow
            Map<String, Object> follow = new HashMap<>();
            follow.put("followerId", currentUid);
            follow.put("followingId", profileUid);
            follow.put("timestamp",
                    System.currentTimeMillis());

            db.collection("follows")
                    .document(followId).set(follow)
                    .addOnSuccessListener(unused -> {
                        isFollowing = true;
                        updateFollowButton();
                        loadUserStats();
                        Toast.makeText(this,
                                "ফলো করা হয়েছে! ✅",
                                Toast.LENGTH_SHORT).show();

                        // Notify
                        NotificationHelper
                                .sendFollowNotification(
                                        profileUid,
                                        currentUserName.isEmpty()
                                                ? "Someone"
                                                : currentUserName);
                    });
        }
    }

    private void updateFollowButton() {
        if (isFollowing) {
            btnFollow.setText("✓ Following");
            btnFollow.setBackgroundResource(
                    R.drawable.btn_green_outline);
        } else {
            btnFollow.setText("+ Follow");
            btnFollow.setBackgroundResource(
                    R.drawable.btn_green_solid);
        }
    }

    private void setupTabs() {
        tabPosts.setOnClickListener(v -> {
            setActiveTab("posts");
            loadCropPosts();
        });

        tabBids.setOnClickListener(v -> {
            setActiveTab("bids");
            loadBidHistory();
        });

        tabDetails.setOnClickListener(v -> {
            setActiveTab("details");
            loadBusinessPosts();
        });

        // Set posts as default active
        setActiveTab("posts");
    }

    private void setActiveTab(String tab) {
        // Reset all indicators
        indicatorPosts.setVisibility(View.INVISIBLE);
        indicatorBids.setVisibility(View.INVISIBLE);
        indicatorDetails.setVisibility(View.INVISIBLE);

        // Reset all text colors
        TextView tabPostsText =
                (TextView) tabPosts.getChildAt(0);
        TextView tabBidsText =
                (TextView) tabBids.getChildAt(0);
        TextView tabDetailsText =
                (TextView) tabDetails.getChildAt(0);

        int gray = getResources().getColor(R.color.gray);
        int green = getResources()
                .getColor(R.color.dark_green);

        tabPostsText.setTextColor(gray);
        tabBidsText.setTextColor(gray);
        tabDetailsText.setTextColor(gray);

        // Activate selected tab
        switch (tab) {
            case "posts":
                indicatorPosts.setVisibility(
                        View.VISIBLE);
                tabPostsText.setTextColor(green);
                break;
            case "bids":
                indicatorBids.setVisibility(
                        View.VISIBLE);
                tabBidsText.setTextColor(green);
                break;
            case "details":
                indicatorDetails.setVisibility(
                        View.VISIBLE);
                tabDetailsText.setTextColor(green);
                break;
        }
    }

    // Tab 1: Farmer crop listings
    private void loadCropPosts() {
        db.collection("crops")
                .whereEqualTo("farmerId", profileUid)
                .orderBy("timestamp",
                        Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    cropList.clear();
                    for (var doc
                            : snapshots.getDocuments()) {
                        CropModel crop =
                                doc.toObject(CropModel.class);
                        if (crop != null) {
                            crop.setCropId(doc.getId());
                            cropList.add(crop);
                        }
                    }

                    if (cropList.isEmpty()) {
                        showEmptyContent(
                                "📭\n\nকোনো তালিকা নেই!");
                    } else {
                        FeedAdapter feedAdapter =
                                new FeedAdapter(
                                        this, cropList,
                                        "businessman", currentUid);
                        rvUserContent.setLayoutManager(
                                new LinearLayoutManager(this));
                        rvUserContent.setAdapter(feedAdapter);
                    }
                })
                .addOnFailureListener(e ->
                        loadCropPostsSimple()
                );
    }

    // Fallback without orderBy
    private void loadCropPostsSimple() {
        db.collection("crops")
                .whereEqualTo("farmerId", profileUid)
                .get()
                .addOnSuccessListener(snapshots -> {
                    cropList.clear();
                    for (var doc
                            : snapshots.getDocuments()) {
                        CropModel crop =
                                doc.toObject(CropModel.class);
                        if (crop != null) {
                            crop.setCropId(doc.getId());
                            cropList.add(crop);
                        }
                    }

                    if (cropList.isEmpty()) {
                        showEmptyContent(
                                "📭\n\nকোনো তালিকা নেই!");
                    } else {
                        FeedAdapter feedAdapter =
                                new FeedAdapter(
                                        this, cropList,
                                        "businessman", currentUid);
                        rvUserContent.setLayoutManager(
                                new LinearLayoutManager(this));
                        rvUserContent.setAdapter(feedAdapter);
                    }
                });
    }

    // Tab 2: Bid history
    private void loadBidHistory() {
        db.collection("bids")
                .whereEqualTo("businessmanId", profileUid)
                .orderBy("timestamp",
                        Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    bidList.clear();
                    int rank = 1;
                    for (var doc
                            : snapshots.getDocuments()) {
                        BidModel bid =
                                doc.toObject(BidModel.class);
                        if (bid != null) {
                            bid.setRank(rank++);
                            bidList.add(bid);
                        }
                    }

                    if (bidList.isEmpty()) {
                        showEmptyContent(
                                "🔨\n\nকোনো বিড নেই!");
                    } else {
                        BidHistoryAdapter bidAdapter =
                                new BidHistoryAdapter(
                                        this, bidList);
                        rvUserContent.setLayoutManager(
                                new LinearLayoutManager(this));
                        rvUserContent.setAdapter(bidAdapter);
                    }
                })
                .addOnFailureListener(e ->
                        showEmptyContent("🔨\n\nকোনো বিড নেই!")
                );
    }

    // Tab 3: Business posts (businessman's demand posts)
    private void loadBusinessPosts() {
        db.collection("posts")
                .whereEqualTo("userId", profileUid)
                .get()
                .addOnSuccessListener(snapshots -> {
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

                    if (postList.isEmpty()) {
                        showEmptyContent(
                                "📝\n\nকোনো পোস্ট নেই!");
                    } else {
                        PostAdapter postAdapter =
                                new PostAdapter(
                                        this, postList,
                                        currentUid,
                                        currentUserName);
                        rvUserContent.setLayoutManager(
                                new LinearLayoutManager(this));
                        rvUserContent.setAdapter(postAdapter);
                    }
                });
    }

    private void showEmptyContent(String message) {
        // Clear RecyclerView and show message
        rvUserContent.setAdapter(null);
        Toast.makeText(this, message,
                Toast.LENGTH_SHORT).show();
    }
}