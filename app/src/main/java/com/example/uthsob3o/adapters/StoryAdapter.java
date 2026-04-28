package com.example.uthsob3o.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.uthsob3o.R;
import com.example.uthsob3o.models.UserModel;
import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {

    private Context context;
    private List<UserModel> userList;

    public StoryAdapter(Context context, List<UserModel> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_story, parent, false);
        return new StoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        UserModel user = userList.get(position);
        holder.storyName.setText(user.getName());
    }

    @Override
    public int getItemCount() { return userList.size(); }

    public static class StoryViewHolder extends RecyclerView.ViewHolder {
        TextView storyName;

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            storyName = itemView.findViewById(R.id.story_name);
        }
    }
}