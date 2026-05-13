package com.example.uthsob3o.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.uthsob3o.R;
import com.example.uthsob3o.activities.BidActivity;
import com.example.uthsob3o.models.CropModel;
import java.util.List;
import android.widget.ImageView;
import com.bumptech.glide.Glide;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    private Context context;
    private List<CropModel> cropList;
    private String currentRole;
    private String currentUid;

    public FeedAdapter(Context context, List<CropModel> cropList,
                       String currentRole, String currentUid) {
        this.context = context;
        this.cropList = cropList;
        this.currentRole = currentRole;
        this.currentUid = currentUid;
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_feed, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        CropModel crop = cropList.get(position);

        holder.cropName.setText(crop.getCropName());
        holder.farmerName.setText(crop.getFarmerName());
        holder.farmerLocation.setText("📍 " + crop.getFarmerLocation());
        holder.weightBadge.setText("🌱 " + crop.getQuantity() + " " + crop.getUnit());
        holder.basePrice.setText("৳" + (int)crop.getCurrentBid() + "/kg");

        // Show verified badge
        if (crop.isFarmerVerified()) {
            holder.verifiedBadge.setVisibility(View.VISIBLE);
        } else {
            holder.verifiedBadge.setVisibility(View.GONE);
        }

        // Role-based button behavior
        if ("farmer".equals(currentRole)) {
            // Farmer sees their own crops differently
            if (currentUid != null && currentUid.equals(crop.getFarmerId())) {
                holder.btnBid.setText("📊 View Bids");
                holder.btnBid.setOnClickListener(v -> {
                    Intent intent = new Intent(context, BidActivity.class);
                    intent.putExtra("cropId", crop.getCropId());
                    intent.putExtra("cropName", crop.getCropName());
                    intent.putExtra("farmerName", crop.getFarmerName());
                    intent.putExtra("location", crop.getFarmerLocation());
                    intent.putExtra("basePrice", String.valueOf((int)crop.getBasePrice()));
                    intent.putExtra("currentBid", String.valueOf((int)crop.getCurrentBid()));
                    intent.putExtra("isFarmer", true);
                    context.startActivity(intent);
                });
            } else {
                // Other farmer's crop — can view but not bid
                holder.btnBid.setText("👁 View Details");
                holder.btnBid.setOnClickListener(v -> {
                    openBidScreen(crop, false);
                });
            }
        } else {
            // Businessman sees BID NOW
            holder.btnBid.setText("🔨 BID NOW →");
            holder.btnBid.setOnClickListener(v -> {
                openBidScreen(crop, true);
            });
        }

        // Load crop image
        ImageView cropImage = holder.itemView.findViewById(R.id.crop_image);
        if (crop.getImageUrl() != null && !crop.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(crop.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(cropImage);
        } else {
            cropImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    private void openBidScreen(CropModel crop, boolean canBid) {
        Intent intent = new Intent(context, BidActivity.class);
        intent.putExtra("cropId", crop.getCropId());
        intent.putExtra("cropName", crop.getCropName());
        intent.putExtra("farmerName", crop.getFarmerName());
        intent.putExtra("farmerId", crop.getFarmerId());
        intent.putExtra("location", crop.getFarmerLocation());
        intent.putExtra("basePrice", String.valueOf((int)crop.getBasePrice()));
        intent.putExtra("currentBid", String.valueOf((int)crop.getCurrentBid()));
        intent.putExtra("canBid", canBid);
        intent.putExtra("isFarmer", false);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() { return cropList.size(); }

    public static class FeedViewHolder extends RecyclerView.ViewHolder {
        TextView cropName, farmerName, farmerLocation;
        TextView weightBadge, basePrice, verifiedBadge;
        Button btnBid;

        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            cropName = itemView.findViewById(R.id.crop_name);
            farmerName = itemView.findViewById(R.id.farmer_name);
            farmerLocation = itemView.findViewById(R.id.farmer_location);
            weightBadge = itemView.findViewById(R.id.weight_badge);
            basePrice = itemView.findViewById(R.id.base_price);
            verifiedBadge = itemView.findViewById(R.id.verified_badge);
            btnBid = itemView.findViewById(R.id.btn_bid);
        }
    }
}