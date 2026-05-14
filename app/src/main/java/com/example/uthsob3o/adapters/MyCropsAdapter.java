package com.example.uthsob3o.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.uthsob3o.R;
import com.example.uthsob3o.models.CropModel;
import java.util.List;

public class MyCropsAdapter extends RecyclerView.Adapter<MyCropsAdapter.ViewHolder> {

    public interface OnCropAction {
        void onAction(CropModel crop);
    }

    private Context context;
    private List<CropModel> cropList;
    private OnCropAction onEdit;
    private OnCropAction onDelete;

    public MyCropsAdapter(Context context, List<CropModel> cropList,
                          OnCropAction onEdit, OnCropAction onDelete) {
        this.context = context;
        this.cropList = cropList;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_my_crop, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CropModel crop = cropList.get(position);

        holder.tvCropName.setText(crop.getCropName());
        holder.tvQuantity.setText("পরিমাণ: " + crop.getQuantity()
                + " " + crop.getUnit());
        holder.tvBasePrice.setText("বেস প্রাইস: ৳"
                + (int)crop.getBasePrice());
        holder.tvCurrentBid.setText("সর্বোচ্চ বিড: ৳"
                + (int)crop.getCurrentBid());
        holder.tvStatus.setText(crop.getStatus().toUpperCase());

        // Status color
        if ("active".equals(crop.getStatus())) {
            holder.tvStatus.setTextColor(
                    context.getResources().getColor(R.color.bright_green));
        } else if ("sold".equals(crop.getStatus())) {
            holder.tvStatus.setTextColor(
                    context.getResources().getColor(R.color.dark_green));
        } else {
            holder.tvStatus.setTextColor(
                    context.getResources().getColor(R.color.gray));
        }

        holder.btnEdit.setOnClickListener(v -> onEdit.onAction(crop));
        holder.btnDelete.setOnClickListener(v -> onDelete.onAction(crop));
    }

    @Override
    public int getItemCount() { return cropList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCropName, tvQuantity, tvBasePrice, tvCurrentBid, tvStatus;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCropName = itemView.findViewById(R.id.tv_crop_name);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            tvBasePrice = itemView.findViewById(R.id.tv_base_price);
            tvCurrentBid = itemView.findViewById(R.id.tv_current_bid);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}