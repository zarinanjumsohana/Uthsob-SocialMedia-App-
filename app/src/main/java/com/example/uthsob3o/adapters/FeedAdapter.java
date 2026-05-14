package com.example.uthsob3o.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.uthsob3o.NotificationHelper;
import com.example.uthsob3o.R;
import com.example.uthsob3o.activities.BidActivity;
import com.example.uthsob3o.activities.UserProfileActivity;
import com.example.uthsob3o.models.CropModel;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class FeedAdapter extends
        RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    private Context context;
    private List<CropModel> cropList;
    private String currentRole;
    private String currentUid;
    private String currentUserName = "";
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public FeedAdapter(Context context,
                       List<CropModel> cropList,
                       String currentRole,
                       String currentUid) {
        this.context = context;
        this.cropList = cropList;
        this.currentRole = currentRole != null
                ? currentRole : "";
        this.currentUid = currentUid != null
                ? currentUid : "";
        this.db = FirebaseFirestore.getInstance();
        this.mAuth = FirebaseAuth.getInstance();

        // Load current user info
        if (!this.currentUid.isEmpty()) {
            db.collection("users")
                    .document(this.currentUid).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String name =
                                    doc.getString("name");
                            if (name != null) {
                                currentUserName = name;
                            }
                            String role =
                                    doc.getString("role");
                            if (role != null) {
                                updateRole(role);
                            }
                        }
                    });
        }
    }

    // Fix for lambda final variable issue
    private void updateRole(String role) {
        if (this.currentRole == null
                || this.currentRole.isEmpty()) {
            this.currentRole = role;
        }
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_feed, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull FeedViewHolder holder, int position) {
        CropModel crop = cropList.get(position);

        // ===== SET BASIC INFO =====
        holder.cropName.setText(crop.getCropName());
        holder.farmerName.setText(crop.getFarmerName());
        holder.farmerLocation.setText(
                "📍 " + crop.getFarmerLocation());
        holder.weightBadge.setText(
                "🌱 " + crop.getQuantity()
                        + " " + crop.getUnit());
        holder.basePrice.setText(
                "৳" + (int) crop.getBasePrice() + "/kg");

        // Verified badge
        if (holder.verifiedBadge != null) {
            holder.verifiedBadge.setVisibility(
                    crop.isFarmerVerified()
                            ? View.VISIBLE : View.GONE);
        }

        // ===== LOAD CROP IMAGE =====
        if (crop.getImageUrl() != null
                && !crop.getImageUrl().isEmpty()) {
            holder.cropImage.setPadding(0, 0, 0, 0);
            Glide.with(context)
                    .load(crop.getImageUrl())
                    .placeholder(
                            android.R.drawable.ic_menu_gallery)
                    .centerCrop()
                    .into(holder.cropImage);
        } else {
            holder.cropImage.setImageResource(
                    android.R.drawable.ic_menu_gallery);
            holder.cropImage.setPadding(
                    60, 60, 60, 60);
        }

        // ===== LOAD FARMER AVATAR =====
        if (crop.getFarmerId() != null
                && !crop.getFarmerId().isEmpty()) {
            db.collection("users")
                    .document(crop.getFarmerId()).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String photoUrl =
                                    doc.getString("photoUrl");
                            if (photoUrl != null
                                    && !photoUrl.isEmpty()
                                    && holder.farmerAvatar
                                    != null) {
                                Glide.with(context)
                                        .load(photoUrl)
                                        .circleCrop()
                                        .placeholder(
                                                android.R.drawable
                                                        .ic_menu_myplaces)
                                        .into(holder.farmerAvatar);
                            }
                        }
                    });
        }

        // ===== AVATAR CLICK → USER PROFILE =====
        if (holder.farmerAvatar != null) {
            holder.farmerAvatar.setOnClickListener(v -> {
                openUserProfile(crop.getFarmerId());
            });
        }

        // ===== FARMER NAME CLICK → USER PROFILE =====
        holder.farmerName.setOnClickListener(v ->
                openUserProfile(crop.getFarmerId())
        );

        // ===== STATUS + ACTION BUTTON =====
        setupStatusAndButton(holder, crop);

        // ===== AUCTION TIMER =====
        setupAuctionTimer(holder, crop);
    }

    // Open user profile page
    private void openUserProfile(String farmerId) {
        if (farmerId == null || farmerId.isEmpty()) return;
        Intent intent = new Intent(
                context, UserProfileActivity.class);
        intent.putExtra("profileUid", farmerId);
        context.startActivity(intent);
    }

    // Setup status badge + action button
    private void setupStatusAndButton(
            FeedViewHolder holder, CropModel crop) {

        String status = crop.getStatus();
        if (status == null) status = "available";

        // Reset
        holder.btnAction.setEnabled(true);
        if (holder.tvCurrentBidLabel != null) {
            holder.tvCurrentBidLabel
                    .setVisibility(View.GONE);
        }
        if (holder.tvAuctionTimer != null) {
            holder.tvAuctionTimer.setVisibility(View.GONE);
        }

        switch (status) {

            case "available":
                holder.tvStatusBadge.setText(
                        "🟢 Available");
                holder.tvStatusBadge.setTextColor(
                        0xFF4CAF50);

                if (isMyOwnCrop(crop)) {
                    holder.btnAction.setText(
                            "📊 View Bids");
                    holder.btnAction.setOnClickListener(
                            v -> openBidScreen(crop, true));
                } else if ("businessman"
                        .equals(currentRole)) {
                    holder.btnAction.setText(
                            "📦 Book Now");
                    holder.btnAction.setOnClickListener(
                            v -> bookProduct(crop));
                } else {
                    holder.btnAction.setText("👁 View");
                    holder.btnAction.setOnClickListener(
                            v -> openBidScreen(crop, false));
                }
                break;

            case "booked":
                holder.tvStatusBadge.setText(
                        "🟡 Booked");
                holder.tvStatusBadge.setTextColor(
                        0xFFFF9800);

                if (isMyOwnCrop(crop)) {
                    holder.btnAction.setText(
                            "⚡ Respond to Booking");
                    holder.btnAction.setOnClickListener(
                            v -> showFarmerDecisionPanel(crop));
                } else if (currentUid.equals(
                        crop.getBookedByUid())) {
                    holder.btnAction.setText(
                            "✅ You Booked This");
                    holder.btnAction.setEnabled(false);
                } else {
                    holder.btnAction.setText(
                            "🔨 Place Bid");
                    holder.btnAction.setOnClickListener(
                            v -> openBidScreen(crop, true));
                }
                break;

            case "auction_live":
                holder.tvStatusBadge.setText(
                        "🔵 Bidding Active");
                holder.tvStatusBadge.setTextColor(
                        0xFF1B4D1E);

                if (holder.tvCurrentBidLabel != null) {
                    holder.tvCurrentBidLabel
                            .setVisibility(View.VISIBLE);
                    holder.tvCurrentBidLabel.setText(
                            "Current Bid: ৳"
                                    + (int) crop.getCurrentBid());
                }

                if (holder.tvAuctionTimer != null) {
                    holder.tvAuctionTimer
                            .setVisibility(View.VISIBLE);
                }

                if (isMyOwnCrop(crop)) {
                    holder.btnAction.setText(
                            "🏆 Select Winner");
                    holder.btnAction.setOnClickListener(
                            v -> showSelectWinnerDialog(crop));
                } else {
                    holder.btnAction.setText(
                            "🔨 BID NOW →");
                    holder.btnAction.setOnClickListener(
                            v -> openBidScreen(crop, true));
                }
                break;

            case "sold":
                holder.tvStatusBadge.setText("🔴 Sold");
                holder.tvStatusBadge.setTextColor(
                        0xFFF44336);
                holder.btnAction.setText("❌ Sold Out");
                holder.btnAction.setEnabled(false);
                break;

            default:
                holder.tvStatusBadge.setText(
                        "🟢 Available");
                if ("businessman".equals(currentRole)) {
                    holder.btnAction.setText(
                            "📦 Book Now");
                    holder.btnAction.setOnClickListener(
                            v -> bookProduct(crop));
                } else {
                    holder.btnAction.setText("👁 View");
                }
                break;
        }
    }

    // Is this the farmer's own crop?
    private boolean isMyOwnCrop(CropModel crop) {
        return !currentUid.isEmpty()
                && currentUid.equals(crop.getFarmerId());
    }

    // Book a product
    private void bookProduct(CropModel crop) {
        if (currentUid.isEmpty()) {
            Toast.makeText(context,
                    "লগইন করুন!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(context)
                .setTitle("বুকিং নিশ্চিত করুন")
                .setMessage("আপনি কি "
                        + crop.getCropName()
                        + " বুক করতে চান?")
                .setPositiveButton("হ্যাঁ, বুক করুন",
                        (dialog, which) -> {
                            Map<String, Object> updates =
                                    new HashMap<>();
                            updates.put("status", "booked");
                            updates.put("bookedByUid",
                                    currentUid);
                            updates.put("bookedByName",
                                    currentUserName.isEmpty()
                                            ? "ব্যবসায়ী"
                                            : currentUserName);

                            db.collection("crops")
                                    .document(crop.getCropId())
                                    .update(updates)
                                    .addOnSuccessListener(u -> {
                                        Toast.makeText(context,
                                                        "বুকিং সফল! ✅",
                                                        Toast.LENGTH_SHORT)
                                                .show();
                                        NotificationHelper
                                                .sendBookingRequestAlert(
                                                        crop.getFarmerId(),
                                                        currentUserName
                                                                .isEmpty()
                                                                ? "একজন ব্যবসায়ী"
                                                                : currentUserName,
                                                        crop.getCropName(),
                                                        crop.getCropId());
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(context,
                                                            "Error: "
                                                                    + e.getMessage(),
                                                            Toast.LENGTH_SHORT)
                                                    .show()
                                    );
                        })
                .setNegativeButton("বাতিল", null)
                .show();
    }

    // Farmer decision panel
    private void showFarmerDecisionPanel(
            CropModel crop) {
        String bookedBy =
                crop.getBookedByName() != null
                        ? crop.getBookedByName()
                        : "একজন ব্যবসায়ী";

        new AlertDialog.Builder(context)
                .setTitle(bookedBy + " বুকিং করেছেন")
                .setItems(new String[]{
                        "✅ বুকিং গ্রহণ করুন",
                        "⏰ নিলাম শুরু করুন"
                }, (dialog, which) -> {
                    if (which == 0) {
                        acceptBooking(crop);
                    } else {
                        showAuctionDurationDialog(crop);
                    }
                })
                .show();
    }

    // Accept booking
    private void acceptBooking(CropModel crop) {
        db.collection("crops")
                .document(crop.getCropId())
                .update("status", "sold")
                .addOnSuccessListener(unused -> {
                    Toast.makeText(context,
                            "বুকিং গৃহীত! পণ্য বিক্রি হয়েছে। ✅",
                            Toast.LENGTH_SHORT).show();

                    if (crop.getBookedByUid() != null) {
                        db.collection("users")
                                .document(currentUid).get()
                                .addOnSuccessListener(doc -> {
                                    String farmerName =
                                            doc.getString("name");
                                    NotificationHelper
                                            .sendBookingAcceptedAlert(
                                                    crop.getBookedByUid(),
                                                    farmerName != null
                                                            ? farmerName
                                                            : "কৃষক",
                                                    crop.getCropName(),
                                                    crop.getCropId());
                                });
                    }
                });
    }

    // Choose auction duration
    private void showAuctionDurationDialog(
            CropModel crop) {
        String[] durations = {
                "1 ঘণ্টা",
                "6 ঘণ্টা",
                "12 ঘণ্টা",
                "24 ঘণ্টা",
                "48 ঘণ্টা"
        };
        long[] durationMs = {
                3600000L,
                21600000L,
                43200000L,
                86400000L,
                172800000L
        };

        new AlertDialog.Builder(context)
                .setTitle("নিলামের সময়কাল")
                .setItems(durations, (dialog, which) -> {
                    long endTime =
                            System.currentTimeMillis()
                                    + durationMs[which];
                    startAuction(crop, endTime);
                })
                .show();
    }

    // Start auction
    private void startAuction(
            CropModel crop, long endTime) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "auction_live");
        updates.put("auctionStartTimestamp",
                System.currentTimeMillis());
        updates.put("auctionEndTimestamp", endTime);

        db.collection("crops")
                .document(crop.getCropId())
                .update(updates)
                .addOnSuccessListener(u ->
                        Toast.makeText(context,
                                "নিলাম শুরু হয়েছে! ✅",
                                Toast.LENGTH_SHORT).show()
                );
    }

    // Select winner dialog
    private void showSelectWinnerDialog(CropModel crop) {
        db.collection("bids")
                .whereEqualTo("cropId", crop.getCropId())
                .orderBy("amount",
                        com.google.firebase.firestore.Query
                                .Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots.isEmpty()) {
                        Toast.makeText(context,
                                "এখনো কোনো বিড নেই!",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int size = snapshots.size();
                    String[] names = new String[size];
                    String[] uids = new String[size];
                    double[] amounts = new double[size];

                    for (int i = 0; i < size; i++) {
                        var doc = snapshots
                                .getDocuments().get(i);
                        String name = doc.getString(
                                "businessmanName");
                        double amount =
                                doc.getDouble("amount") != null
                                        ? doc.getDouble("amount") : 0;
                        String uid = doc.getString(
                                "businessmanId");

                        names[i] = (i + 1) + ". "
                                + (name != null ? name : "User")
                                + " — ৳" + (int) amount;
                        uids[i] = uid != null ? uid : "";
                        amounts[i] = amount;
                    }

                    new AlertDialog.Builder(context)
                            .setTitle("বিজয়ী নির্বাচন করুন")
                            .setItems(names,
                                    (dialog, which) ->
                                            confirmWinner(
                                                    crop,
                                                    uids[which],
                                                    amounts[which],
                                                    snapshots))
                            .show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context,
                                "Error loading bids: "
                                        + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    // Confirm winner and close auction
    private void confirmWinner(
            CropModel crop,
            String winnerUid,
            double winnerAmount,
            com.google.firebase.firestore
                    .QuerySnapshot snapshots) {

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "sold");
        updates.put("currentBid", winnerAmount);

        db.collection("crops")
                .document(crop.getCropId())
                .update(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(context,
                            "বিজয়ী নির্বাচিত! ✅",
                            Toast.LENGTH_SHORT).show();

                    db.collection("users")
                            .document(currentUid).get()
                            .addOnSuccessListener(doc -> {
                                String farmerName =
                                        doc.getString("name");

                                // Notify winner
                                NotificationHelper
                                        .sendAuctionWonAlert(
                                                winnerUid,
                                                farmerName != null
                                                        ? farmerName : "কৃষক",
                                                crop.getCropName(),
                                                winnerAmount,
                                                crop.getCropId());

                                // Notify all losers
                                for (var bidDoc :
                                        snapshots.getDocuments()) {
                                    String bidderUid =
                                            bidDoc.getString(
                                                    "businessmanId");
                                    if (bidderUid != null
                                            && !bidderUid.equals(
                                            winnerUid)) {
                                        NotificationHelper
                                                .sendAuctionLostAlert(
                                                        bidderUid,
                                                        crop.getCropName(),
                                                        crop.getCropId());
                                    }
                                }
                            });
                });
    }

    // Auction countdown timer on card
    private void setupAuctionTimer(
            FeedViewHolder holder, CropModel crop) {

        if (!"auction_live".equals(crop.getStatus())) {
            if (holder.tvAuctionTimer != null) {
                holder.tvAuctionTimer
                        .setVisibility(View.GONE);
            }
            if (holder.countDownTimer != null) {
                holder.countDownTimer.cancel();
                holder.countDownTimer = null;
            }
            return;
        }

        if (holder.tvAuctionTimer != null) {
            holder.tvAuctionTimer.setVisibility(
                    View.VISIBLE);
        }

        long endTime = crop.getAuctionEndTimestamp();
        long timeLeft = endTime
                - System.currentTimeMillis();

        if (timeLeft <= 0) {
            if (holder.tvAuctionTimer != null) {
                holder.tvAuctionTimer.setText(
                        "⏱ Ended");
            }
            // Auto close auction
            if (crop.getCropId() != null) {
                db.collection("crops")
                        .document(crop.getCropId())
                        .update("status", "sold");
            }
            return;
        }

        // Cancel previous timer
        if (holder.countDownTimer != null) {
            holder.countDownTimer.cancel();
        }

        holder.countDownTimer =
                new CountDownTimer(timeLeft, 1000) {
                    @Override
                    public void onTick(long ms) {
                        if (holder.tvAuctionTimer == null)
                            return;
                        long h = ms / 3600000;
                        long m = (ms % 3600000) / 60000;
                        long s = (ms % 60000) / 1000;
                        holder.tvAuctionTimer.setText(
                                String.format(
                                        Locale.getDefault(),
                                        "⏱ %02d:%02d:%02d",
                                        h, m, s));
                    }

                    @Override
                    public void onFinish() {
                        if (holder.tvAuctionTimer != null) {
                            holder.tvAuctionTimer.setText(
                                    "⏱ Ended");
                        }
                        if (crop.getCropId() != null) {
                            db.collection("crops")
                                    .document(crop.getCropId())
                                    .update("status", "sold");
                        }
                    }
                }.start();
    }

    // Open bid screen
    private void openBidScreen(
            CropModel crop, boolean canBid) {
        Intent intent = new Intent(
                context, BidActivity.class);
        intent.putExtra("cropId",
                crop.getCropId());
        intent.putExtra("cropName",
                crop.getCropName());
        intent.putExtra("farmerId",
                crop.getFarmerId());
        intent.putExtra("farmerName",
                crop.getFarmerName());
        intent.putExtra("location",
                crop.getFarmerLocation());
        intent.putExtra("basePrice",
                String.valueOf((int) crop.getBasePrice()));
        intent.putExtra("currentBid",
                String.valueOf((int) crop.getCurrentBid()));
        intent.putExtra("canBid", canBid);
        intent.putExtra("isFarmer",
                isMyOwnCrop(crop));
        intent.putExtra("role", currentRole);
        intent.putExtra("auctionEndTimestamp",
                crop.getAuctionEndTimestamp());
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return cropList.size();
    }

    @Override
    public void onViewRecycled(
            @NonNull FeedViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.countDownTimer != null) {
            holder.countDownTimer.cancel();
            holder.countDownTimer = null;
        }
    }

    // ===== VIEW HOLDER =====
    public static class FeedViewHolder
            extends RecyclerView.ViewHolder {

        TextView cropName, farmerName, farmerLocation;
        TextView weightBadge, basePrice, verifiedBadge;
        TextView tvStatusBadge, tvCurrentBidLabel;
        TextView tvAuctionTimer;
        ImageView cropImage;
        ImageView farmerAvatar;
        Button btnAction;
        CountDownTimer countDownTimer;

        public FeedViewHolder(
                @NonNull View itemView) {
            super(itemView);
            cropName = itemView.findViewById(
                    R.id.crop_name);
            farmerName = itemView.findViewById(
                    R.id.farmer_name);
            farmerLocation = itemView.findViewById(
                    R.id.farmer_location);
            weightBadge = itemView.findViewById(
                    R.id.weight_badge);
            basePrice = itemView.findViewById(
                    R.id.base_price);
            verifiedBadge = itemView.findViewById(
                    R.id.verified_badge);
            tvStatusBadge = itemView.findViewById(
                    R.id.tv_status_badge);
            tvCurrentBidLabel = itemView.findViewById(
                    R.id.tv_current_bid_label);
            tvAuctionTimer = itemView.findViewById(
                    R.id.tv_auction_timer);
            cropImage = itemView.findViewById(
                    R.id.crop_image);
            farmerAvatar = itemView.findViewById(
                    R.id.farmer_avatar);
            btnAction = itemView.findViewById(
                    R.id.btn_action);
        }
    }
}