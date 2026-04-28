package com.example.uthsob3o.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.uthsob3o.R;
import com.example.uthsob3o.adapters.BidHistoryAdapter;
import com.example.uthsob3o.models.BidModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BidActivity extends AppCompatActivity {

    // Views
    TextView cropTitle, cropLocation, currentBid;
    TextView timerText, bidAmountDisplay;
    TextView btnMinus, btnPlus, btnBack;
    Button btnPlaceBid;
    RecyclerView rvBidHistory;
    LinearLayout navHome, navAlerts;

    // Data
    List<BidModel> bidList = new ArrayList<>();
    BidHistoryAdapter bidAdapter;

    // Bid tracking
    int currentBidAmount = 135;
    int myBidAmount = 135;
    int bidStep = 5; // increase/decrease by 5
    int bidCounter = 3; // for dummy bid history numbering

    // Timer
    CountDownTimer countDownTimer;
    long timeLeftMillis = 14 * 3600 * 1000 + 20 * 60 * 1000 + 5 * 1000; // 14:20:05

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bid);

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

        // Get data from Intent
        String cropName = getIntent().getStringExtra("cropName");
        String farmerLocation = getIntent().getStringExtra("location");
        String basePrice = getIntent().getStringExtra("basePrice");

        // Set data on screen
        if (cropName != null) cropTitle.setText(cropName);
        if (farmerLocation != null) cropLocation.setText(farmerLocation);
        if (basePrice != null) {
            currentBidAmount = Integer.parseInt(basePrice);
            myBidAmount = currentBidAmount;
            currentBid.setText("৳" + currentBidAmount);
            bidAmountDisplay.setText("৳" + myBidAmount);
        }

        // Setup bid history
        loadDummyBidHistory();
        bidAdapter = new BidHistoryAdapter(this, bidList);
        rvBidHistory.setLayoutManager(new LinearLayoutManager(this));
        rvBidHistory.setAdapter(bidAdapter);

        // Start countdown timer
        startTimer();

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Minus button — decrease bid
        btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myBidAmount > currentBidAmount + bidStep) {
                    myBidAmount -= bidStep;
                    bidAmountDisplay.setText("৳" + myBidAmount);
                } else {
                    Toast.makeText(BidActivity.this,
                            "Bid must be higher than current bid!",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Plus button — increase bid
        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myBidAmount += bidStep;
                bidAmountDisplay.setText("৳" + myBidAmount);
            }
        });

        // Place Bid button
        btnPlaceBid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myBidAmount <= currentBidAmount) {
                    Toast.makeText(BidActivity.this,
                            "আপনার বিড বর্তমান বিডের চেয়ে বেশি হতে হবে!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                // Update current bid
                currentBidAmount = myBidAmount;
                currentBid.setText("৳" + currentBidAmount);

                // Add to history
                bidCounter++;
                BidModel newBid = new BidModel(
                        1,
                        "You",
                        String.valueOf(myBidAmount),
                        "Just now"
                );
                bidAdapter.addBid(newBid);
                rvBidHistory.scrollToPosition(0);

                // Increase for next bid
                myBidAmount += bidStep;
                bidAmountDisplay.setText("৳" + myBidAmount);

                Toast.makeText(BidActivity.this,
                        "বিড সফল! ৳" + currentBidAmount,
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Navigation
        navHome.setOnClickListener(v -> {
            Intent intent = new Intent(BidActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        navAlerts.setOnClickListener(v -> {
            startActivity(new Intent(BidActivity.this, NotificationActivity.class));
        });
    }

    // Load dummy bids
    private void loadDummyBidHistory() {
        bidList.add(new BidModel(1, "Rahat Chowdhury", "130", "2 minutes ago"));
        bidList.add(new BidModel(2, "Nabil Rashid", "125", "5 minutes ago"));
        bidList.add(new BidModel(3, "Tariq Islam", "120", "10 minutes ago"));
        bidList.add(new BidModel(4, "Kamal Hossain", "115", "15 minutes ago"));
    }

    // Countdown timer logic
    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftMillis = millisUntilFinished;

                long hours = millisUntilFinished / 3600000;
                long minutes = (millisUntilFinished % 3600000) / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;

                timerText.setText(String.format(Locale.getDefault(),
                        "%02d:%02d:%02d", hours, minutes, seconds));
            }

            @Override
            public void onFinish() {
                timerText.setText("00:00:00");
                timerText.setTextColor(getResources().getColor(R.color.error));
                btnPlaceBid.setEnabled(false);
                btnPlaceBid.setText("Auction Ended");
                Toast.makeText(BidActivity.this,
                        "নিলাম শেষ হয়েছে!", Toast.LENGTH_LONG).show();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop timer when leaving screen
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}