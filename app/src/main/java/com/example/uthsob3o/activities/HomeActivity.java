package com.example.uthsob3o.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

    List<CropModel> cropList = new ArrayList<>();
    List<UserModel> storyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Connect views
        rvFeed = findViewById(R.id.rv_feed);
        rvStories = findViewById(R.id.rv_stories);
        navHome = findViewById(R.id.nav_home);
        navSearch = findViewById(R.id.nav_search);
        navAdd = findViewById(R.id.nav_add);
        navAlerts = findViewById(R.id.nav_alerts);
        navProfile = findViewById(R.id.nav_profile);
        btnNotification = findViewById(R.id.btn_notification);

        // Load dummy data
        loadDummyStories();
        loadDummyCrops();

        // Setup Stories RecyclerView (horizontal)
        StoryAdapter storyAdapter = new StoryAdapter(this, storyList);
        rvStories.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvStories.setAdapter(storyAdapter);

        // Setup Feed RecyclerView (vertical)
        FeedAdapter feedAdapter = new FeedAdapter(this, cropList);
        rvFeed.setLayoutManager(new LinearLayoutManager(this));
        rvFeed.setAdapter(feedAdapter);

        // Bottom Nav Clicks
        navHome.setOnClickListener(v -> {
            // Already on home
        });

        navAlerts.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, NotificationActivity.class));
        });

        btnNotification.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, NotificationActivity.class));
        });

        navAdd.setOnClickListener(v -> {
            // Will be Add Crop screen (coming soon)
            android.widget.Toast.makeText(this,
                    "Add Crop - Coming Soon!", android.widget.Toast.LENGTH_SHORT).show();
        });

        navSearch.setOnClickListener(v -> {
            android.widget.Toast.makeText(this,
                    "Search - Coming Soon!", android.widget.Toast.LENGTH_SHORT).show();
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            intent.putExtra("role", getIntent().getStringExtra("role"));
            intent.putExtra("name", getIntent().getStringExtra("name"));
            startActivity(intent);
        });
    }

    // Dummy story data
    private void loadDummyStories() {
        storyList.add(new UserModel("Your Story", ""));
        storyList.add(new UserModel("Karim", ""));
        storyList.add(new UserModel("Rahat", ""));
        storyList.add(new UserModel("Sohail", ""));
        storyList.add(new UserModel("Fatema", ""));
        storyList.add(new UserModel("Rahim", ""));
    }

    // Dummy crop feed data
    private void loadDummyCrops() {
        cropList.add(new CropModel(
                "1",
                "Premium Diamond Potatoes",
                "Abdul Karim",
                "Bogura, Bangladesh",
                "5000 KG",
                "25",
                "",
                true
        ));

        cropList.add(new CropModel(
                "2",
                "Himsagar Mangoes",
                "Rahim Uddin",
                "Rajshahi, Bangladesh",
                "2000 KG",
                "120",
                "",
                true
        ));

        cropList.add(new CropModel(
                "3",
                "Fresh Hilsha Fish",
                "Kamal Hossain",
                "Chandpur, Bangladesh",
                "500 KG",
                "800",
                "",
                false
        ));

        cropList.add(new CropModel(
                "4",
                "Organic Rice (BR-28)",
                "Nur Islam",
                "Dinajpur, Bangladesh",
                "10000 KG",
                "45",
                "",
                true
        ));

        cropList.add(new CropModel(
                "5",
                "Red Tomatoes",
                "Selim Ahmed",
                "Comilla, Bangladesh",
                "3000 KG",
                "30",
                "",
                false
        ));
    }
}