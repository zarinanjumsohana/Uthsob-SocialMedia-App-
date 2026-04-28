package com.example.uthsob3o.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.uthsob3o.R;
import com.example.uthsob3o.adapters.NotificationAdapter;
import com.example.uthsob3o.models.NotificationModel;
import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    RecyclerView rvNotifications;
    LinearLayout navHome, navAlerts;
    List<NotificationModel> notifList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        rvNotifications = findViewById(R.id.rv_notifications);
        navHome = findViewById(R.id.nav_home);
        navAlerts = findViewById(R.id.nav_alerts);

        // Load dummy notifications
        loadDummyNotifications();

        // Setup RecyclerView
        NotificationAdapter adapter = new NotificationAdapter(this, notifList);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);

        // Navigation
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(NotificationActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        navAlerts.setOnClickListener(v -> {
            // Already here
        });
    }

    private void loadDummyNotifications() {
        notifList.add(new NotificationModel(
                "নতুন বিড পাওয়া গেছে! (New Bid Received!)",
                "New Bid Received",
                "Rahat Chowdhury আপনার Himsagar আমের জন্য ৳140 টাকা বিড করেছেন। (Rahat Chowdhury placed a bid of ৳140 on your Himsagar Mangoes.)",
                "10:41 AM",
                "bid"
        ));

        notifList.add(new NotificationModel(
                "নিলাম শেষ হচ্ছে! (Auction Ending Soon)",
                "Auction Ending Soon",
                "আপনার Diamond আলুর নিলাম ২ ঘণ্টায় শেষ হবে। (Your auction for Diamond Potatoes is ending in 2 hours.)",
                "09:11 AM",
                "auction"
        ));

        notifList.add(new NotificationModel(
                "নতুন বিড পাওয়া গেছে! (New Bid Received!)",
                "New Bid Received",
                "Kamal Hossain আপনার Rice এর জন্য ৳50 টাকা বিড করেছেন।",
                "Yesterday",
                "bid"
        ));

        notifList.add(new NotificationModel(
                "নিবন্ধন সফল! (Registration Successful)",
                "Registration Successful",
                "আপনার অ্যাকাউন্ট সফলভাবে তৈরি হয়েছে। স্বাগতম UTH SOB এ!",
                "2 days ago",
                "auction"
        ));
    }
}