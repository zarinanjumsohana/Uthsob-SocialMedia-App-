package com.example.uthsob3o.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.example.uthsob3o.adapters.BidHistoryAdapter;
import com.example.uthsob3o.models.BidModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class BidActivity extends AppCompatActivity {

    TextView cropTitle, cropLocation, currentBid;
    TextView timerText, bidAmountDisplay;
    TextView btnMinus, btnPlus, btnBack;
    Button btnPlaceBid;
    RecyclerView rvBidHistory;
    LinearLayout navHome, navAlerts;
    ProgressBar progressBar;

    List<BidModel> bidList = new ArrayList<>();
    BidHistoryAdapter bidAdapter;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    ListenerRegistration bidListener;

    String cropId, cropName, farmerId;
    double basePrice, currentBidAmount, myBidAmount;
    double bidStep = 5;
    boolean isFarmer = false;
    boolean canBid = true;
    String currentUid;
    String currentUserName = "";

    CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bid);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            currentUid = mAuth.getCurrentUser().getUid();
        }

        // Get data from intent
        cropId = getIntent().getStringExtra("cropId");
        cropName = getIntent().getStringExtra("cropName");
        farmerId = getIntent().getStringExtra("farmerId");
        String location = getIntent().getStringExtra("location");
        String basePriceStr = getIntent().getStringExtra("basePrice");
        String currentBidStr = getIntent().getStringExtra("currentBid");
        isFarmer = getIntent().getBooleanExtra("isFarmer", false);
        canBid = getIntent().getBooleanExtra("canBid", true);

        basePrice = basePriceStr != null ? Double.parseDouble(basePriceStr) : 0;
        currentBidAmount = currentBidStr != null ?
                Double.parseDouble(currentBidStr) : basePrice;
        myBidAmount = currentBidAmount + bidStep;

        // Connect views
        cropTitle = findViewById(R.id.crop_title);
        cropLocation = findViewById(R.id.crop_location);
        currentBid = findViewById(R.id.current_bid);
        timerText = findViewById(R.id.timer_text);
        bidAmountDisplay = findViewById(R.id.bid_amount_display);
        btnMinus = findViewById(R.id.btn_minus);
        btnPlus = findViewById(R.id.btn_plus);
        btnPlaceBid = findViewById(R.id.btn_place_bid);
        btnBack = findViewById(R.id.btn_back);
        rvBidHistory = findViewById(R.id.rv_bid_history);
        navHome = findViewById(R.id.nav_home);
        navAlerts = findViewById(R.id.nav_alerts);

        // Set initial data
        if (cropName != null) cropTitle.setText(cropName);
        if (location != null) cropLocation.setText(location);
        currentBid.setText("৳" + (int)currentBidAmount);
        bidAmountDisplay.setText("৳" + (int)myBidAmount);

        // Setup bid history
        bidAdapter = new BidHistoryAdapter(this, bidList);
        rvBidHistory.setLayoutManager(new LinearLayoutManager(this));
        rvBidHistory.setAdapter(bidAdapter);

        // Get current user name
        if (currentUid != null) {
            db.collection("users").document(currentUid).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            currentUserName = doc.getString("name");
                        }
                    });
        }

        // Setup UI based on role
        setupRoleUI();

        // Load real bids from Firebase
        loadBidsFromFirebase();

        // Start timer
        startTimer(14 * 3600 * 1000 + 20 * 60 * 1000);

        // Button clicks
        setupButtons();
    }

    private void setupRoleUI() {
        if (isFarmer) {
            // Farmer sees bid history but cannot place bids
            btnPlaceBid.setText("👁 নিলাম পর্যবেক্ষণ করছেন");
            btnPlaceBid.setEnabled(false);
            btnMinus.setVisibility(View.GONE);
            btnPlus.setVisibility(View.GONE);
            bidAmountDisplay.setVisibility(View.GONE);
        } else if (!canBid) {
            btnPlaceBid.setText("👁 View Only");
            btnPlaceBid.setEnabled(false);
        }
    }

    private void loadBidsFromFirebase() {
        if (cropId == null || cropId.startsWith("dummy")) {
            // Load dummy bids if no real cropId
            loadDummyBids();
            return;
        }

        bidListener = db.collection("bids")
                .whereEqualTo("cropId", cropId)
                .orderBy("amount", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        loadDummyBids();
                        return;
                    }
                    if (snapshots != null) {
                        bidList.clear();
                        int rank = 1;
                        double highestBid = currentBidAmount;

                        for (var doc : snapshots.getDocuments()) {
                            BidModel bid = doc.toObject(BidModel.class);
                            if (bid != null) {
                                bid.setRank(rank++);
                                bidList.add(bid);
                                if (bid.getAmount() > highestBid) {
                                    highestBid = bid.getAmount();
                                }
                            }
                        }

                        // Update current bid display
                        currentBidAmount = highestBid;
                        currentBid.setText("৳" + (int)currentBidAmount);
                        myBidAmount = currentBidAmount + bidStep;
                        bidAmountDisplay.setText("৳" + (int)myBidAmount);

                        bidAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void loadDummyBids() {
        bidList.clear();
        bidList.add(new BidModel("1", cropId != null ? cropId : "",
                "dummy1", "Rahat Chowdhury", 130));
        bidList.add(new BidModel("2", cropId != null ? cropId : "",
                "dummy2", "Nabil Rashid", 125));
        bidList.add(new BidModel("3", cropId != null ? cropId : "",
                "dummy3", "Tariq Islam", 120));
        for (int i = 0; i < bidList.size(); i++) {
            bidList.get(i).setRank(i + 1);
        }
        bidAdapter.notifyDataSetChanged();
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> finish());

        btnMinus.setOnClickListener(v -> {
            if (myBidAmount > currentBidAmount + bidStep) {
                myBidAmount -= bidStep;
                bidAmountDisplay.setText("৳" + (int)myBidAmount);
            } else {
                Toast.makeText(this,
                        "বিড বর্তমান বিডের চেয়ে বেশি হতে হবে!",
                        Toast.LENGTH_SHORT).show();
            }
        });

        btnPlus.setOnClickListener(v -> {
            myBidAmount += bidStep;
            bidAmountDisplay.setText("৳" + (int)myBidAmount);
        });

        btnPlaceBid.setOnClickListener(v -> placeBid());

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        });

        navAlerts.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationActivity.class))
        );
    }

    private void placeBid() {
        if (myBidAmount <= currentBidAmount) {
            Toast.makeText(this,
                    "আপনার বিড বর্তমান বিডের চেয়ে বেশি হতে হবে!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if businessman is bidding on their own crop
        if (currentUid != null && currentUid.equals(farmerId)) {
            Toast.makeText(this,
                    "আপনি নিজের ফসলে বিড দিতে পারবেন না!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnPlaceBid.setEnabled(false);

        String bidId = UUID.randomUUID().toString();
        BidModel bid = new BidModel(
                bidId,
                cropId != null ? cropId : "dummy",
                currentUid,
                currentUserName.isEmpty() ? "আপনি" : currentUserName,
                myBidAmount
        );

        if (cropId != null && !cropId.startsWith("dummy")) {
            // Save to Firebase
            db.collection("bids").document(bidId)
                    .set(bid)
                    .addOnSuccessListener(unused -> {
                        // Update crop's current bid
                        db.collection("crops").document(cropId)
                                .update("currentBid", myBidAmount)
                                .addOnSuccessListener(u -> {
                                    btnPlaceBid.setEnabled(true);

                                    // Send notification to farmer
                                    sendBidNotification();

                                    Toast.makeText(this,
                                            "বিড সফল! ৳" + (int)myBidAmount,
                                            Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        btnPlaceBid.setEnabled(true);
                        Toast.makeText(this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Dummy mode — just show locally
            bid.setRank(1);
            bidAdapter.addBid(bid);
            rvBidHistory.scrollToPosition(0);
            currentBidAmount = myBidAmount;
            currentBid.setText("৳" + (int)currentBidAmount);
            myBidAmount += bidStep;
            bidAmountDisplay.setText("৳" + (int)myBidAmount);
            btnPlaceBid.setEnabled(true);
            Toast.makeText(this,
                    "বিড সফল! ৳" + (int)currentBidAmount,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void sendBidNotification() {
        if (farmerId == null) return;

        String notifId = UUID.randomUUID().toString();
        Map<String, Object> notif = new HashMap<>();
        notif.put("notifId", notifId);
        notif.put("type", "bid_received");
        notif.put("title", "নতুন বিড পাওয়া গেছে!");
        notif.put("message", currentUserName + " আপনার "
                + cropName + " এর জন্য ৳"
                + (int)myBidAmount + " বিড করেছেন।");
        notif.put("time", new java.text.SimpleDateFormat(
                "hh:mm a", java.util.Locale.getDefault())
                .format(new java.util.Date()));
        notif.put("read", false);
        notif.put("relatedId", cropId);
        notif.put("timestamp", System.currentTimeMillis());

        db.collection("notifications")
                .document(farmerId)
                .collection("alerts")
                .document(notifId)
                .set(notif);
    }

    private void startTimer(long milliseconds) {
        countDownTimer = new CountDownTimer(milliseconds, 1000) {
            @Override
            public void onTick(long ms) {
                long h = ms / 3600000;
                long m = (ms % 3600000) / 60000;
                long s = (ms % 60000) / 1000;
                timerText.setText(String.format(
                        Locale.getDefault(), "%02d:%02d:%02d", h, m, s));
            }

            @Override
            public void onFinish() {
                timerText.setText("00:00:00");
                btnPlaceBid.setEnabled(false);
                btnPlaceBid.setText("নিলাম শেষ");
                Toast.makeText(BidActivity.this,
                        "নিলাম শেষ হয়েছে!", Toast.LENGTH_LONG).show();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
        if (bidListener != null) bidListener.remove();
    }
}