package com.example.uthsob3o.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.uthsob3o.R;
import java.util.HashMap;
import java.util.Map;

public class FarmerProfileActivity extends AppCompatActivity {

    TextView tvName, tvLocation, tvRole, tvPhone;
    Button btnFollow;
    TextView btnBack;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String currentUid, farmerUid;
    boolean isFollowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_farmer_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            currentUid = mAuth.getCurrentUser().getUid();
        }

        farmerUid = getIntent().getStringExtra("farmerUid");
        String farmerName = getIntent().getStringExtra("farmerName");
        String farmerLocation = getIntent().getStringExtra("farmerLocation");

        tvName = findViewById(R.id.tv_name);
        tvLocation = findViewById(R.id.tv_location);
        tvRole = findViewById(R.id.tv_role);
        btnFollow = findViewById(R.id.btn_follow);
        btnBack = findViewById(R.id.btn_back);

        if (farmerName != null) tvName.setText(farmerName);
        if (farmerLocation != null) tvLocation.setText("📍 " + farmerLocation);
        tvRole.setText("কৃষক (FARMER)");

        btnBack.setOnClickListener(v -> finish());

        // Check if already following
        checkFollowStatus();

        btnFollow.setOnClickListener(v -> toggleFollow());
    }

    private void checkFollowStatus() {
        if (currentUid == null || farmerUid == null) return;

        db.collection("follows")
                .document(currentUid + "_" + farmerUid)
                .get()
                .addOnSuccessListener(doc -> {
                    isFollowing = doc.exists();
                    updateFollowButton();
                });
    }

    private void toggleFollow() {
        if (currentUid == null || farmerUid == null) return;

        String followId = currentUid + "_" + farmerUid;

        if (isFollowing) {
            // Unfollow
            db.collection("follows").document(followId)
                    .delete()
                    .addOnSuccessListener(unused -> {
                        isFollowing = false;
                        updateFollowButton();
                        Toast.makeText(this,
                                "আনফলো করা হয়েছে",
                                Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Follow
            Map<String, Object> follow = new HashMap<>();
            follow.put("followerId", currentUid);
            follow.put("followingId", farmerUid);
            follow.put("timestamp", System.currentTimeMillis());

            db.collection("follows").document(followId)
                    .set(follow)
                    .addOnSuccessListener(unused -> {
                        isFollowing = true;
                        updateFollowButton();
                        Toast.makeText(this,
                                "ফলো করা হয়েছে! ✅",
                                Toast.LENGTH_SHORT).show();

                        // Send notification to farmer
                        sendFollowNotification();
                    });
        }
    }

    private void updateFollowButton() {
        if (isFollowing) {
            btnFollow.setText("✓ Following");
            btnFollow.setBackgroundResource(R.drawable.btn_green_outline);
        } else {
            btnFollow.setText("+ Follow");
            btnFollow.setBackgroundResource(R.drawable.btn_green_solid);
        }
    }

    private void sendFollowNotification() {
        if (farmerUid == null || currentUid == null) return;

        db.collection("users").document(currentUid).get()
                .addOnSuccessListener(doc -> {
                    String myName = doc.getString("name");
                    String notifId = java.util.UUID.randomUUID().toString();

                    Map<String, Object> notif = new HashMap<>();
                    notif.put("type", "new_follower");
                    notif.put("title", "নতুন ফলোয়ার!");
                    notif.put("message", myName + " আপনাকে ফলো করেছেন।");
                    notif.put("time", new java.text.SimpleDateFormat(
                            "hh:mm a", java.util.Locale.getDefault())
                            .format(new java.util.Date()));
                    notif.put("read", false);
                    notif.put("timestamp", System.currentTimeMillis());

                    db.collection("notifications")
                            .document(farmerUid)
                            .collection("full")
                            .document(notifId)
                            .set(notif);
                });
    }
}