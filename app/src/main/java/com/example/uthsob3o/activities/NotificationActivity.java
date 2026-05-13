package com.example.uthsob3o.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.example.uthsob3o.R;
import com.example.uthsob3o.adapters.NotificationAdapter;
import com.example.uthsob3o.models.NotificationModel;
import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    RecyclerView rvNotifications;
    LinearLayout navHome, navAlerts;
    List<NotificationModel> notifList = new ArrayList<>();
    NotificationAdapter adapter;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    ListenerRegistration notifListener;
    String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            currentUid = mAuth.getCurrentUser().getUid();
        }

        rvNotifications = findViewById(R.id.rv_notifications);
        navHome = findViewById(R.id.nav_home);
        navAlerts = findViewById(R.id.nav_alerts);

        adapter = new NotificationAdapter(this, notifList);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);

        loadNotificationsFromFirebase();
        setupNavigation();
    }

    private void loadNotificationsFromFirebase() {
        if (currentUid == null) {
            loadDummyNotifications();
            return;
        }

        notifListener = db.collection("notifications")
                .document(currentUid)
                .collection("alerts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        loadDummyNotifications();
                        return;
                    }
                    if (snapshots != null && !snapshots.isEmpty()) {
                        notifList.clear();
                        for (var doc : snapshots.getDocuments()) {
                            String notifId = doc.getId();
                            String type = doc.getString("type");
                            String title = doc.getString("title");
                            String message = doc.getString("message");
                            String time = doc.getString("time");
                            String relatedId = doc.getString("relatedId");

                            NotificationModel notif = new NotificationModel(
                                    notifId,
                                    type != null ? type : "general",
                                    title != null ? title : "",
                                    message != null ? message : "",
                                    time != null ? time : "",
                                    relatedId != null ? relatedId : ""
                            );
                            notifList.add(notif);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        loadDummyNotifications();
                    }
                });
    }

    private void loadDummyNotifications() {
        notifList.clear();
        notifList.add(new NotificationModel("1", "bid_received",
                "নতুন বিড পাওয়া গেছে! (New Bid Received!)",
                "Rahat Chowdhury আপনার Himsagar আমের জন্য ৳140 বিড করেছেন।",
                "10:41 AM", "crop1"));
        notifList.add(new NotificationModel("2", "auction",
                "নিলাম শেষ হচ্ছে! (Auction Ending Soon)",
                "আপনার Diamond আলুর নিলাম ২ ঘণ্টায় শেষ হবে।",
                "09:11 AM", "crop2"));
        notifList.add(new NotificationModel("3", "bid_received",
                "নতুন বিড পাওয়া গেছে!",
                "Kamal Hossain আপনার Rice এর জন্য ৳50 বিড করেছেন।",
                "Yesterday", "crop3"));
        adapter.notifyDataSetChanged();
    }

    private void setupNavigation() {
        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        });
        navAlerts.setOnClickListener(v -> {});
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notifListener != null) notifListener.remove();
    }
}