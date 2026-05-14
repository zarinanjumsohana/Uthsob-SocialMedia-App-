package com.example.uthsob3o.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.uthsob3o.R;
import com.example.uthsob3o.activities.AddStoryActivity;
import com.example.uthsob3o.activities.ViewStoryActivity;
import com.example.uthsob3o.models.StoryModel;
import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {

    private Context context;
    private List<StoryModel> storyList;
    private boolean showAddStory;

    public StoryAdapter(Context context, List<StoryModel> storyList,
                        boolean showAddStory) {
        this.context = context;
        this.storyList = storyList;
        this.showAddStory = showAddStory;
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                              int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_story, parent, false);
        return new StoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder,
                                 int position) {
        // First item is always "Add Story"
        if (showAddStory && position == 0) {
            holder.storyName.setText("Your Story");
            holder.storyImage.setImageResource(
                    android.R.drawable.ic_input_add);
            holder.addIndicator.setVisibility(View.VISIBLE);

            holder.itemView.setOnClickListener(v ->
                    context.startActivity(
                            new Intent(context, AddStoryActivity.class))
            );
            return;
        }

        // Real story items
        int storyIndex = showAddStory ? position - 1 : position;
        if (storyIndex >= storyList.size()) return;

        StoryModel story = storyList.get(storyIndex);
        holder.storyName.setText(story.getUserName());
        holder.addIndicator.setVisibility(View.GONE);

        // Load story image
        if (story.getImageUrl() != null
                && !story.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(story.getImageUrl())
                    .circleCrop()
                    .into(holder.storyImage);
        }

        // Click to view story
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ViewStoryActivity.class);
            intent.putExtra("imageUrl", story.getImageUrl());
            intent.putExtra("userName", story.getUserName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return storyList.size() + (showAddStory ? 1 : 0);
    }

    public static class StoryViewHolder extends RecyclerView.ViewHolder {
        ImageView storyImage;
        TextView storyName, addIndicator;

        public StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            storyImage = itemView.findViewById(R.id.story_image);
            storyName = itemView.findViewById(R.id.story_name);
            addIndicator = itemView.findViewById(R.id.add_indicator);
        }
    }
}