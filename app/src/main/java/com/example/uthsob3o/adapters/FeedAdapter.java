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

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    private Context context;
    private List<CropModel> cropList;

    public FeedAdapter(Context context, List<CropModel> cropList) {
        this.context = context;
        this.cropList = cropList;
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_feed, parent, false);
        return new FeedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        CropModel crop = cropList.get(position);

        holder.cropName.setText(crop.getCropName());
        holder.farmerName.setText(crop.getFarmerName());
        holder.farmerLocation.setText("📍 " + crop.getLocation());
        holder.weightBadge.setText("🌱 " + crop.getWeight());
        holder.basePrice.setText("৳" + crop.getBasePrice() + "/kg");

        // BID NOW button click
        holder.btnBid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BidActivity.class);
                intent.putExtra("cropName", crop.getCropName());
                intent.putExtra("farmerName", crop.getFarmerName());
                intent.putExtra("location", crop.getLocation());
                intent.putExtra("basePrice", crop.getBasePrice());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() { return cropList.size(); }

    public static class FeedViewHolder extends RecyclerView.ViewHolder {
        TextView cropName, farmerName, farmerLocation, weightBadge, basePrice;
        Button btnBid;

        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            cropName = itemView.findViewById(R.id.crop_name);
            farmerName = itemView.findViewById(R.id.farmer_name);
            farmerLocation = itemView.findViewById(R.id.farmer_location);
            weightBadge = itemView.findViewById(R.id.weight_badge);
            basePrice = itemView.findViewById(R.id.base_price);
            btnBid = itemView.findViewById(R.id.btn_bid);
        }
    }
}