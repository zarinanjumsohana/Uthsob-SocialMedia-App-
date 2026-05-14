package com.example.uthsob3o.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.example.uthsob3o.NotificationHelper;
import com.example.uthsob3o.R;
import com.example.uthsob3o.models.NotificationModel;
import java.util.List;

public class NotificationAdapter extends
        RecyclerView.Adapter<NotificationAdapter
                .NotifViewHolder> {

    private Context context;
    private List<NotificationModel> notifList;

    public NotificationAdapter(
            Context context,
            List<NotificationModel> notifList) {
        this.context = context;
        this.notifList = notifList;
    }

    @NonNull
    @Override
    public NotifViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_notification,
                        parent, false);
        return new NotifViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull NotifViewHolder holder,
            int position) {
        NotificationModel notif = notifList.get(position);

        holder.tvTitle.setText(notif.getTitle());
        holder.tvMessage.setText(notif.getMessage());
        holder.tvTime.setText(notif.getTime());

        // Set icon
        switch (notif.getType()) {
            case "bid_received":
                holder.tvIcon.setText("🔨"); break;
            case "bid_accepted":
            case "auction_won":
                holder.tvIcon.setText("🏆"); break;
            case "auction_lost":
                holder.tvIcon.setText("😔"); break;
            case "auction_ending":
                holder.tvIcon.setText("⏰"); break;
            case "booking_request":
                holder.tvIcon.setText("📦"); break;
            case "booking_accepted":
                holder.tvIcon.setText("✅"); break;
            case "auction_started":
                holder.tvIcon.setText("🔔"); break;
            case "new_follower":
                holder.tvIcon.setText("👤"); break;
            case "like":
                holder.tvIcon.setText("❤️"); break;
            default:
                holder.tvIcon.setText("🔔"); break;
        }

        // Show action buttons for bid/booking
        if ("bid_received".equals(notif.getType())
                || "booking_request".equals(
                notif.getType())) {
            holder.actionButtons.setVisibility(
                    View.VISIBLE);

            holder.btnSellNow.setText(
                    "booking_request".equals(notif.getType())
                            ? "✅ Accept" : "✅ Accept Bid");

            holder.btnSellNow.setOnClickListener(v ->
                    showAcceptDialog(notif)
            );

            holder.btnDetails.setOnClickListener(v ->
                    Toast.makeText(context,
                            "বিস্তারিত - Coming Soon!",
                            Toast.LENGTH_SHORT).show()
            );
        } else {
            holder.actionButtons.setVisibility(View.GONE);
        }
    }

    private void showAcceptDialog(
            NotificationModel notif) {
        new AlertDialog.Builder(context)
                .setTitle("নিশ্চিত করুন")
                .setMessage(
                        "bid_received".equals(notif.getType())
                                ? "আপনি কি এই বিড গ্রহণ করতে চান?\n"
                                + "নিলাম বন্ধ হয়ে যাবে।"
                                : "আপনি কি এই বুকিং গ্রহণ করতে চান?\n"
                                + "পণ্য বিক্রি হিসেবে চিহ্নিত হবে।")
                .setPositiveButton("হ্যাঁ",
                        (dialog, which) ->
                                acceptDeal(notif))
                .setNegativeButton("বাতিল", null)
                .show();
    }

    private void acceptDeal(NotificationModel notif) {
        FirebaseFirestore db =
                FirebaseFirestore.getInstance();
        FirebaseAuth mAuth =
                FirebaseAuth.getInstance();
        String cropId = notif.getRelatedId();

        if (cropId == null || cropId.isEmpty()) {
            Toast.makeText(context,
                    "ক্রপ তথ্য পাওয়া যায়নি!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String farmerUid =
                mAuth.getCurrentUser() != null
                        ? mAuth.getCurrentUser().getUid() : "";

        // Mark crop as sold
        db.collection("crops").document(cropId)
                .update("status", "sold")
                .addOnSuccessListener(unused -> {
                    Toast.makeText(context,
                            "সফলভাবে গৃহীত! ✅ পণ্য বিক্রি হয়েছে।",
                            Toast.LENGTH_LONG).show();

                    // Get farmer name
                    db.collection("users")
                            .document(farmerUid).get()
                            .addOnSuccessListener(farmerDoc -> {
                                String farmerName =
                                        farmerDoc.getString("name");
                                if (farmerName == null) {
                                    farmerName = "কৃষক";
                                }
                                final String fName = farmerName;

                                if ("bid_received".equals(
                                        notif.getType())) {
                                    // Find winning bid
                                    db.collection("bids")
                                            .whereEqualTo("cropId",
                                                    cropId)
                                            .orderBy("amount",
                                                    Query.Direction
                                                            .DESCENDING)
                                            .limit(1)
                                            .get()
                                            .addOnSuccessListener(
                                                    bids -> {
                                                        if (!bids.isEmpty()) {
                                                            var top =
                                                                    bids.getDocuments()
                                                                            .get(0);
                                                            String winnerId =
                                                                    top.getString(
                                                                            "businessmanId");
                                                            double amount =
                                                                    top.getDouble(
                                                                            "amount")
                                                                            != null
                                                                            ? top.getDouble(
                                                                            "amount")
                                                                            : 0;

                                                            if (winnerId
                                                                    != null) {
                                                                // Notify winner
                                                                NotificationHelper
                                                                        .sendAuctionWonAlert(
                                                                                winnerId,
                                                                                fName,
                                                                                notif.getMessage(),
                                                                                amount,
                                                                                cropId);

                                                                // Notify losers
                                                                db.collection("bids")
                                                                        .whereEqualTo(
                                                                                "cropId",
                                                                                cropId)
                                                                        .get()
                                                                        .addOnSuccessListener(
                                                                                allBids -> {
                                                                                    for (var b : allBids.getDocuments()) {
                                                                                        String bId = b.getString("businessmanId");
                                                                                        if (bId != null && !bId.equals(winnerId)) {
                                                                                            NotificationHelper.sendAuctionLostAlert(bId, notif.getMessage(), cropId);
                                                                                        }
                                                                                    }
                                                                                });
                                                            }
                                                        }
                                                    });
                                } else {
                                    // Booking accepted
                                    db.collection("crops")
                                            .document(cropId).get()
                                            .addOnSuccessListener(
                                                    cropDoc -> {
                                                        String bookerId =
                                                                cropDoc.getString(
                                                                        "bookedByUid");
                                                        String cropName =
                                                                cropDoc.getString(
                                                                        "cropName");
                                                        if (bookerId != null) {
                                                            NotificationHelper
                                                                    .sendBookingAcceptedAlert(
                                                                            bookerId,
                                                                            fName,
                                                                            cropName != null
                                                                                    ? cropName
                                                                                    : "ফসল",
                                                                            cropId);
                                                        }
                                                    });
                                }
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public int getItemCount() {
        return notifList.size();
    }

    public static class NotifViewHolder
            extends RecyclerView.ViewHolder {
        TextView tvIcon, tvTitle,
                tvMessage, tvTime;
        LinearLayout actionButtons;
        Button btnSellNow, btnDetails;

        public NotifViewHolder(
                @NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(
                    R.id.notif_icon);
            tvTitle = itemView.findViewById(
                    R.id.notif_title);
            tvMessage = itemView.findViewById(
                    R.id.notif_message);
            tvTime = itemView.findViewById(
                    R.id.notif_time);
            actionButtons = itemView.findViewById(
                    R.id.action_buttons);
            btnSellNow = itemView.findViewById(
                    R.id.btn_sell_now);
            btnDetails = itemView.findViewById(
                    R.id.btn_details);
        }
    }
}