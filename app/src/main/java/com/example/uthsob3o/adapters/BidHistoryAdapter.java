package com.example.uthsob3o.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.uthsob3o.R;
import com.example.uthsob3o.models.BidModel;
import java.util.List;

public class BidHistoryAdapter extends RecyclerView.Adapter<BidHistoryAdapter.BidViewHolder> {

    private Context context;
    private List<BidModel> bidList;

    public BidHistoryAdapter(Context context, List<BidModel> bidList) {
        this.context = context;
        this.bidList = bidList;
    }

    @NonNull
    @Override
    public BidViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_bid_history, parent, false);
        return new BidViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BidViewHolder holder, int position) {
        BidModel bid = bidList.get(position);
        holder.rank.setText(String.valueOf(bid.getRank()));
        holder.bidderName.setText(bid.getBidderName());
        holder.timeAgo.setText(bid.getTimeAgo());
        holder.bidAmount.setText("৳" + bid.getBidAmount());
    }

    @Override
    public int getItemCount() { return bidList.size(); }

    // Add new bid to top of list
    public void addBid(BidModel bid) {
        bidList.add(0, bid);
        notifyItemInserted(0);
    }

    public static class BidViewHolder extends RecyclerView.ViewHolder {
        TextView rank, bidderName, timeAgo, bidAmount;

        public BidViewHolder(@NonNull View itemView) {
            super(itemView);
            rank = itemView.findViewById(R.id.bid_rank);
            bidderName = itemView.findViewById(R.id.bid_name);
            timeAgo = itemView.findViewById(R.id.bid_time);
            bidAmount = itemView.findViewById(R.id.bid_amount);
        }
    }
}