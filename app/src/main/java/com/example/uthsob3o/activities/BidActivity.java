package com.example.uthsob3o.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.example.uthsob3o.NavigationHelper;
import com.example.uthsob3o.NotificationHelper;
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

    // ===== VIEWS =====
    TextView cropTitle, cropLocation;
    TextView currentBidView, timerText;
    TextView bidAmountDisplay;
    TextView btnMinus, btnPlus, btnBack;
    Button btnPlaceBid;
    RecyclerView rvBidHistory;
    LinearLayout navHome, navSearch, navAdd,
            navAlerts, navProfile;
    LinearLayout bidInputSection;

    // ===== DATA =====
    List<BidModel> bidList = new ArrayList<>();
    BidHistoryAdapter bidAdapter;

    // ===== FIREBASE =====
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    ListenerRegistration bidListener;
    ListenerRegistration cropListener;

    // ===== CROP INFO =====
    String cropId = "";
    String cropName = "";
    String farmerId = "";
    String farmerName = "";
    double basePrice = 0;
    double currentBidAmount = 0;
    double myBidAmount = 0;
    double bidStep = 5;
    boolean isFarmer = false;
    boolean canBid = true;
    String currentUid = "";
    String currentUserName = "";
    String currentRole = "";
    long auctionEndTimestamp = 0;
    String cropStatus = "auction_live";

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

        // Get intent data
        cropId = getIntent().getStringExtra("cropId") != null
                ? getIntent().getStringExtra("cropId") : "";
        cropName = getIntent()
                .getStringExtra("cropName") != null
                ? getIntent().getStringExtra("cropName") : "";
        farmerId = getIntent()
                .getStringExtra("farmerId") != null
                ? getIntent().getStringExtra("farmerId") : "";
        farmerName = getIntent()
                .getStringExtra("farmerName") != null
                ? getIntent().getStringExtra("farmerName") : "";
        currentRole = getIntent()
                .getStringExtra("role") != null
                ? getIntent().getStringExtra("role") : "";
        isFarmer = getIntent()
                .getBooleanExtra("isFarmer", false);
        canBid = getIntent()
                .getBooleanExtra("canBid", true);
        auctionEndTimestamp = getIntent()
                .getLongExtra("auctionEndTimestamp", 0);

        String basePriceStr = getIntent()
                .getStringExtra("basePrice");
        String currentBidStr = getIntent()
                .getStringExtra("currentBid");
        String locationStr = getIntent()
                .getStringExtra("location");

        basePrice = basePriceStr != null
                ? Double.parseDouble(basePriceStr) : 0;
        currentBidAmount = currentBidStr != null
                ? Double.parseDouble(currentBidStr) : basePrice;
        myBidAmount = currentBidAmount + bidStep;

        // Connect views
        cropTitle = findViewById(R.id.crop_title);
        cropLocation = findViewById(R.id.crop_location);
        currentBidView = findViewById(R.id.current_bid);
        timerText = findViewById(R.id.timer_text);
        bidAmountDisplay = findViewById(
                R.id.bid_amount_display);
        btnMinus = findViewById(R.id.btn_minus);
        btnPlus = findViewById(R.id.btn_plus);
        btnPlaceBid = findViewById(R.id.btn_place_bid);
        btnBack = findViewById(R.id.btn_back);
        rvBidHistory = findViewById(R.id.rv_bid_history);
        navHome = findViewById(R.id.nav_home);
        navSearch = findViewById(R.id.nav_search);
        navAdd = findViewById(R.id.nav_add);
        navAlerts = findViewById(R.id.nav_alerts);
        navProfile = findViewById(R.id.nav_profile);
        bidInputSection = findViewById(
                R.id.bid_input_section);

        // Set initial display
        cropTitle.setText(cropName);
        if (locationStr != null) {
            cropLocation.setText(locationStr);
        }
        currentBidView.setText(
                "৳" + (int) currentBidAmount);
        bidAmountDisplay.setText(
                "৳" + (int) myBidAmount);

        // Setup RecyclerView
        bidAdapter = new BidHistoryAdapter(
                this, bidList);
        rvBidHistory.setLayoutManager(
                new LinearLayoutManager(this));
        rvBidHistory.setAdapter(bidAdapter);

        // Load user info
        loadCurrentUserInfo();

        // Setup everything
        setupRoleUI();
        listenToCropChanges(); // Real-time crop updates
        loadBidsRealtime();    // Real-time bid updates
        setupTimer();
        setupButtons();

        NavigationHelper.setupBottomNav(
                this, navHome, navSearch, navAdd,
                navAlerts, navProfile,
                currentRole, currentUid, ""
        );
    }

    // Load current user's name + role
    private void loadCurrentUserInfo() {
        if (currentUid.isEmpty()) return;
        db.collection("users")
                .document(currentUid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        if (name != null) currentUserName = name;
                        String role = doc.getString("role");
                        if (role != null
                                && currentRole.isEmpty()) {
                            currentRole = role;
                        }
                        // Update isFarmer check
                        if ("farmer".equals(currentRole)
                                && currentUid.equals(farmerId)) {
                            isFarmer = true;
                            setupRoleUI();
                        }
                    }
                });
    }

    // Listen to crop changes in real-time
    private void listenToCropChanges() {
        if (cropId.isEmpty()) return;

        cropListener = db.collection("crops")
                .document(cropId)
                .addSnapshotListener((doc, error) -> {
                    if (error != null || doc == null) return;
                    if (!doc.exists()) return;

                    String status = doc.getString("status");
                    if (status != null) cropStatus = status;

                    Double bidAmount =
                            doc.getDouble("currentBid");
                    if (bidAmount != null) {
                        currentBidAmount = bidAmount;
                        currentBidView.setText(
                                "৳" + (int) currentBidAmount);
                        myBidAmount =
                                currentBidAmount + bidStep;
                        if (!isFarmer && canBid) {
                            bidAmountDisplay.setText(
                                    "৳" + (int) myBidAmount);
                        }
                    }

                    Long endTime =
                            doc.getLong("auctionEndTimestamp");
                    if (endTime != null
                            && endTime != auctionEndTimestamp) {
                        auctionEndTimestamp = endTime;
                        setupTimer();
                    }

                    // Handle status changes
                    handleStatusChange(status);
                });
    }

    // Handle real-time status changes
    private void handleStatusChange(String status) {
        if (status == null) return;
        switch (status) {
            case "sold":
                // Auction ended
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                timerText.setText("নিলাম শেষ");
                timerText.setTextColor(
                        getResources().getColor(R.color.error));
                btnPlaceBid.setEnabled(false);
                btnPlaceBid.setText("❌ নিলাম শেষ হয়েছে");
                bidInputSection.setVisibility(View.GONE);
                Toast.makeText(this,
                        "এই নিলাম শেষ হয়েছে!",
                        Toast.LENGTH_LONG).show();
                break;
            case "auction_live":
                if (!isFarmer && canBid) {
                    bidInputSection.setVisibility(
                            View.VISIBLE);
                    btnPlaceBid.setEnabled(true);
                }
                break;
        }
    }

    // Load bids in real-time
    private void loadBidsRealtime() {
        if (cropId.isEmpty()) return;

        bidListener = db.collection("bids")
                .whereEqualTo("cropId", cropId)
                .orderBy("amount", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) return;
                    if (snapshots == null) return;

                    bidList.clear();
                    int rank = 1;
                    for (var doc : snapshots.getDocuments()) {
                        BidModel bid =
                                doc.toObject(BidModel.class);
                        if (bid != null) {
                            bid.setRank(rank++);
                            bidList.add(bid);
                        }
                    }
                    bidAdapter.notifyDataSetChanged();
                });
    }

    // Setup UI based on role
    private void setupRoleUI() {
        if (isFarmer) {
            // Farmer views bids only
            if (bidInputSection != null) {
                bidInputSection.setVisibility(View.GONE);
            }
            if (btnPlaceBid != null) {
                btnPlaceBid.setText(
                        "👁 নিলাম পর্যবেক্ষণ করছেন");
                btnPlaceBid.setEnabled(false);
            }
        } else if (!canBid) {
            if (bidInputSection != null) {
                bidInputSection.setVisibility(View.GONE);
            }
        }
    }

    // Setup countdown timer
    private void setupTimer() {
        if (auctionEndTimestamp > 0) {
            long timeLeft = auctionEndTimestamp
                    - System.currentTimeMillis();
            if (timeLeft > 0) {
                startTimer(timeLeft);
            } else {
                timerText.setText("00:00:00");
                timerText.setTextColor(
                        getResources().getColor(R.color.error));
                if (!isFarmer) {
                    btnPlaceBid.setEnabled(false);
                    btnPlaceBid.setText("নিলাম শেষ");
                }
            }
        } else {
            // Default demo timer
            startTimer(3600000L); // 1 hour default
        }
    }

    // Start countdown
    private void startTimer(long milliseconds) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(
                milliseconds, 1000) {
            @Override
            public void onTick(long ms) {
                long h = ms / 3600000;
                long m = (ms % 3600000) / 60000;
                long s = (ms % 60000) / 1000;
                timerText.setText(String.format(
                        Locale.getDefault(),
                        "%02d:%02d:%02d", h, m, s));

                // Color changes
                if (ms < 600000) { // < 10 min → red
                    timerText.setTextColor(
                            getResources()
                                    .getColor(R.color.error));
                } else if (ms < 3600000) { // < 1hr → orange
                    timerText.setTextColor(
                            getResources()
                                    .getColor(R.color.warning));
                }

                // Notify farmer when 10 mins left
                if (ms < 600000 && ms > 590000) {
                    if (!farmerId.isEmpty()) {
                        NotificationHelper
                                .sendAuctionEndingAlert(
                                        farmerId,
                                        cropName,
                                        cropId);
                    }
                }
            }

            @Override
            public void onFinish() {
                timerText.setText("00:00:00");
                timerText.setTextColor(
                        getResources().getColor(R.color.error));

                // Disable bidding
                btnPlaceBid.setEnabled(false);
                btnPlaceBid.setText("নিলাম শেষ হয়েছে");
                if (bidInputSection != null) {
                    bidInputSection.setVisibility(
                            View.GONE);
                }

                // Mark crop as sold automatically
                if (!cropId.isEmpty()) {
                    autoSelectWinnerOnEnd();
                }
            }
        }.start();
    }

    // Auto end auction when timer finishes
    private void autoSelectWinnerOnEnd() {
        db.collection("bids")
                .whereEqualTo("cropId", cropId)
                .orderBy("amount",
                        Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!snapshots.isEmpty()) {
                        var topBid = snapshots
                                .getDocuments().get(0);
                        String winnerId =
                                topBid.getString("businessmanId");
                        double winAmount =
                                topBid.getDouble("amount") != null
                                        ? topBid.getDouble("amount") : 0;

                        // Mark sold
                        Map<String, Object> updates =
                                new HashMap<>();
                        updates.put("status", "sold");
                        updates.put("currentBid", winAmount);

                        db.collection("crops")
                                .document(cropId)
                                .update(updates)
                                .addOnSuccessListener(u -> {
                                    // Notify winner
                                    if (winnerId != null) {
                                        NotificationHelper
                                                .sendAuctionWonAlert(
                                                        winnerId,
                                                        farmerName.isEmpty()
                                                                ? "কৃষক"
                                                                : farmerName,
                                                        cropName,
                                                        winAmount,
                                                        cropId);
                                    }

                                    // Notify all losers
                                    notifyLosers(winnerId,
                                            snapshots);
                                });
                    } else {
                        // No bids — just mark unsold
                        db.collection("crops")
                                .document(cropId)
                                .update("status", "available");
                    }
                });
    }

    // Setup all button clicks
    private void setupButtons() {
        btnBack.setOnClickListener(v -> finish());

        btnMinus.setOnClickListener(v -> {
            double minAllowed =
                    currentBidAmount + bidStep;
            if (myBidAmount > minAllowed) {
                myBidAmount -= bidStep;
                bidAmountDisplay.setText(
                        "৳" + (int) myBidAmount);
            } else {
                Toast.makeText(this,
                        "বিড বর্তমান বিডের চেয়ে বেশি হতে হবে!",
                        Toast.LENGTH_SHORT).show();
            }
        });

        btnPlus.setOnClickListener(v -> {
            myBidAmount += bidStep;
            bidAmountDisplay.setText(
                    "৳" + (int) myBidAmount);
        });

        btnPlaceBid.setOnClickListener(v -> placeBid());
    }

    // Place a bid
    private void placeBid() {
        if (myBidAmount <= currentBidAmount) {
            Toast.makeText(this,
                    "বিড বর্তমান বিডের চেয়ে বেশি হতে হবে!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUid.equals(farmerId)) {
            Toast.makeText(this,
                    "নিজের ফসলে বিড দেওয়া যাবে না!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUid.isEmpty()) {
            Toast.makeText(this,
                    "অনুগ্রহ করে লগইন করুন!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if ("sold".equals(cropStatus)) {
            Toast.makeText(this,
                    "এই নিলাম ইতিমধ্যে শেষ হয়েছে!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnPlaceBid.setEnabled(false);
        btnPlaceBid.setText("বিড দেওয়া হচ্ছে...");

        String bidId = UUID.randomUUID().toString();
        BidModel bid = new BidModel(
                bidId, cropId, currentUid,
                currentUserName.isEmpty()
                        ? "ব্যবসায়ী" : currentUserName,
                myBidAmount
        );

        // Save bid to Firebase
        db.collection("bids").document(bidId)
                .set(bid)
                .addOnSuccessListener(unused ->
                        // Update current bid on crop
                        db.collection("crops")
                                .document(cropId)
                                .update("currentBid", myBidAmount)
                                .addOnSuccessListener(u -> {
                                    double placedAmount = myBidAmount;
                                    btnPlaceBid.setEnabled(true);
                                    btnPlaceBid.setText(
                                            "🔨  Place Bid Now");
                                    myBidAmount += bidStep;
                                    bidAmountDisplay.setText(
                                            "৳" + (int) myBidAmount);

                                    // Notify farmer
                                    NotificationHelper
                                            .sendBidReceivedAlert(
                                                    farmerId,
                                                    currentUserName.isEmpty()
                                                            ? "একজন ব্যবসায়ী"
                                                            : currentUserName,
                                                    cropName,
                                                    placedAmount,
                                                    cropId);

                                    Toast.makeText(this,
                                            "বিড সফল! ৳"
                                                    + (int) placedAmount,
                                            Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    btnPlaceBid.setEnabled(true);
                                    btnPlaceBid.setText(
                                            "🔨  Place Bid Now");
                                    Toast.makeText(this,
                                            "Error: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                })
                )
                .addOnFailureListener(e -> {
                    btnPlaceBid.setEnabled(true);
                    btnPlaceBid.setText("🔨  Place Bid Now");
                    Toast.makeText(this,
                            "Bid Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    // Notify all losing bidders
    private void notifyLosers(
            String winnerId,
            com.google.firebase.firestore
                    .QuerySnapshot topBids) {

        db.collection("bids")
                .whereEqualTo("cropId", cropId)
                .get()
                .addOnSuccessListener(allBids -> {
                    for (var doc : allBids.getDocuments()) {
                        String bidderId =
                                doc.getString("businessmanId");
                        if (bidderId != null
                                && !bidderId.equals(
                                winnerId)) {
                            NotificationHelper
                                    .sendAuctionLostAlert(
                                            bidderId,
                                            cropName,
                                            cropId);
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (bidListener != null) {
            bidListener.remove();
        }
        if (cropListener != null) {
            cropListener.remove();
        }
    }
}