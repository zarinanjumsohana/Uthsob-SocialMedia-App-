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
import androidx.recyclerview.widget.RecyclerView;
import com.example.uthsob3o.R;
import com.example.uthsob3o.models.NotificationModel;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotifViewHolder> {

    private Context context;
    private List<NotificationModel> notifList;

    public NotificationAdapter(Context context, List<NotificationModel> notifList) {
        this.context = context;
        this.notifList = notifList;
    }

    @NonNull
    @Override
    public NotifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_notification, parent, false);
        return new NotifViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotifViewHolder holder, int position) {
        NotificationModel notif = notifList.get(position);

        holder.title.setText(notif.getTitle());
        holder.message.setText(notif.getMessage());
        holder.time.setText(notif.getTime());

        // Show action buttons only for "bid" type
        if (notif.getType().equals("bid")) {
            holder.actionButtons.setVisibility(View.VISIBLE);

            holder.btnSellNow.setOnClickListener(v ->
                    Toast.makeText(context,
                            "বিক্রয় নিশ্চিত করা হয়েছে!", Toast.LENGTH_SHORT).show()
            );

            holder.btnDetails.setOnClickListener(v ->
                    Toast.makeText(context,
                            "বিস্তারিত - Coming Soon!", Toast.LENGTH_SHORT).show()
            );
        } else {
            holder.actionButtons.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return notifList.size(); }

    public static class NotifViewHolder extends RecyclerView.ViewHolder {
        TextView title, message, time;
        LinearLayout actionButtons;
        Button btnSellNow, btnDetails;

        public NotifViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.notif_title);
            message = itemView.findViewById(R.id.notif_message);
            time = itemView.findViewById(R.id.notif_time);
            actionButtons = itemView.findViewById(R.id.action_buttons);
            btnSellNow = itemView.findViewById(R.id.btn_sell_now);
            btnDetails = itemView.findViewById(R.id.btn_details);
        }
    }
}