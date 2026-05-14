package com.example.uthsob3o.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.example.uthsob3o.NavigationHelper;
import com.example.uthsob3o.R;
import com.example.uthsob3o.adapters.NotificationAdapter;
import com.example.uthsob3o.models.NotificationModel;
import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    // Views
    RecyclerView rvNotifications;
    LinearLayout navHome, navSearch, navAdd,
            navAlerts, navProfile;
    LinearLayout tabAlerts, tabFull;
    View indicatorAlerts, indicatorFull;
    TextView tvEmpty;
    TextView tabAlertsText, tabFullText;

    // Data
    List<NotificationModel> alertsList = new ArrayList<>();
    List<NotificationModel> fullList = new ArrayList<>();
    NotificationAdapter adapter;

    // Firebase
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    ListenerRegistration alertsListener;
    ListenerRegistration fullListener;
    String currentUid;
    String currentRole = "";

    // State
    boolean isAlertsTab = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            currentUid = mAuth.getCurrentUser().getUid();
        }

        currentRole = getIntent()
                .getStringExtra("role") != null
                ? getIntent().getStringExtra("role") : "";

        // If role not passed, get from Firebase
        if (currentRole.isEmpty() && currentUid != null) {
            db.collection("users")
                    .document(currentUid).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String role = doc.getString("role");
                            if (role != null) currentRole = role;
                        }
                    });
        }

        initViews();
        setupTabs();
        loadAlerts(); // Default tab
        setupNavigation();
    }

    private void initViews() {
        rvNotifications = findViewById(R.id.rv_notifications);
        navHome = findViewById(R.id.nav_home);
        navSearch = findViewById(R.id.nav_search);
        navAdd = findViewById(R.id.nav_add);
        navAlerts = findViewById(R.id.nav_alerts);
        navProfile = findViewById(R.id.nav_profile);
        tabAlerts = findViewById(R.id.tab_alerts);
        tabFull = findViewById(R.id.tab_full);
        indicatorAlerts = findViewById(R.id.indicator_alerts);
        indicatorFull = findViewById(R.id.indicator_full);
        tvEmpty = findViewById(R.id.tv_empty);

        // Get text views inside tabs
        tabAlertsText = (TextView) tabAlerts.getChildAt(0);
        tabFullText = (TextView) tabFull.getChildAt(0);

        adapter = new NotificationAdapter(this, alertsList);
        rvNotifications.setLayoutManager(
                new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);
    }

    private void setupTabs() {
        tabAlerts.setOnClickListener(v -> {
            if (isAlertsTab) return;
            isAlertsTab = true;
            updateTabUI();
            loadAlerts();
        });

        tabFull.setOnClickListener(v -> {
            if (!isAlertsTab) return;
            isAlertsTab = false;
            updateTabUI();
            loadFullNotifications();
        });
    }

    private void updateTabUI() {
        if (isAlertsTab) {
            indicatorAlerts.setVisibility(View.VISIBLE);
            indicatorFull.setVisibility(View.INVISIBLE);
            tabAlertsText.setTextColor(
                    getResources().getColor(R.color.dark_green));
            tabFullText.setTextColor(
                    getResources().getColor(R.color.gray));
        } else {
            indicatorAlerts.setVisibility(View.INVISIBLE);
            indicatorFull.setVisibility(View.VISIBLE);
            tabAlertsText.setTextColor(
                    getResources().getColor(R.color.gray));
            tabFullText.setTextColor(
                    getResources().getColor(R.color.dark_green));
        }
    }

    private void loadAlerts() {
        if (currentUid == null) {
            showEmpty("কোনো সতর্কতা নেই!");
            return;
        }

        if (alertsListener != null) alertsListener.remove();

        alertsListener = db.collection("notifications")
                .document(currentUid)
                .collection("alerts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        showEmpty("লোড করতে সমস্যা হয়েছে!");
                        return;
                    }

                    alertsList.clear();

                    if (snapshots != null
                            && !snapshots.isEmpty()) {
                        for (DocumentSnapshot doc
                                : snapshots.getDocuments()) {
                            NotificationModel notif =
                                    buildNotif(doc);
                            if (notif != null) {
                                alertsList.add(notif);
                                // Mark as read
                                if (Boolean.FALSE.equals(
                                        doc.getBoolean("read"))) {
                                    doc.getReference()
                                            .update("read", true);
                                }
                            }
                        }
                    }

                    adapter = new NotificationAdapter(
                            this, alertsList);
                    rvNotifications.setAdapter(adapter);

                    if (alertsList.isEmpty()) {
                        showEmpty("কোনো সতর্কতা নেই!\n"
                                + "বিড পেলে এখানে দেখাবে।");
                    } else {
                        hideEmpty();
                    }
                });
    }

    private void loadFullNotifications() {
        if (currentUid == null) {
            showEmpty("কোনো বিজ্ঞপ্তি নেই!");
            return;
        }

        if (fullListener != null) fullListener.remove();

        fullListener = db.collection("notifications")
                .document(currentUid)
                .collection("full")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        showEmpty("লোড করতে সমস্যা হয়েছে!");
                        return;
                    }

                    fullList.clear();

                    if (snapshots != null
                            && !snapshots.isEmpty()) {
                        for (DocumentSnapshot doc
                                : snapshots.getDocuments()) {
                            NotificationModel notif =
                                    buildNotif(doc);
                            if (notif != null) {
                                fullList.add(notif);
                                if (Boolean.FALSE.equals(
                                        doc.getBoolean("read"))) {
                                    doc.getReference()
                                            .update("read", true);
                                }
                            }
                        }
                    }

                    adapter = new NotificationAdapter(
                            this, fullList);
                    rvNotifications.setAdapter(adapter);

                    if (fullList.isEmpty()) {
                        showEmpty("কোনো কার্যক্রম নেই!\n"
                                + "ফলো, লাইক পেলে এখানে দেখাবে।");
                    } else {
                        hideEmpty();
                    }
                });
    }

    private NotificationModel buildNotif(
            DocumentSnapshot doc) {
        String title = doc.getString("title");
        if (title == null) return null;

        return new NotificationModel(
                doc.getId(),
                doc.getString("type") != null
                        ? doc.getString("type") : "general",
                title,
                doc.getString("message") != null
                        ? doc.getString("message") : "",
                doc.getString("time") != null
                        ? doc.getString("time") : "",
                doc.getString("relatedId") != null
                        ? doc.getString("relatedId") : ""
        );
    }

    private void showEmpty(String msg) {
        tvEmpty.setText(msg);
        tvEmpty.setVisibility(View.VISIBLE);
        rvNotifications.setVisibility(View.GONE);
    }

    private void hideEmpty() {
        tvEmpty.setVisibility(View.GONE);
        rvNotifications.setVisibility(View.VISIBLE);
    }

    private void setupNavigation() {
        NavigationHelper.setupBottomNav(
                this, navHome, navSearch, navAdd,
                navAlerts, navProfile,
                currentRole, currentUid, "alerts"
        );

        NavigationHelper.highlightTab(
                navHome, navSearch, navAlerts, navProfile,
                "alerts",
                getResources().getColor(R.color.dark_green),
                getResources().getColor(R.color.gray)
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (alertsListener != null) alertsListener.remove();
        if (fullListener != null) fullListener.remove();
    }
}