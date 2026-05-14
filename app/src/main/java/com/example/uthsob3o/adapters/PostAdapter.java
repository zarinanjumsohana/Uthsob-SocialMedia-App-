package com.example.uthsob3o.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.example.uthsob3o.NotificationHelper;
import com.example.uthsob3o.R;
import com.example.uthsob3o.activities.UserProfileActivity;
import com.example.uthsob3o.models.PostModel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PostAdapter extends
        RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private Context context;
    private List<PostModel> postList;
    private String currentUid;
    private String currentUserName;
    private FirebaseFirestore db;

    public PostAdapter(Context context,
                       List<PostModel> postList,
                       String currentUid,
                       String currentUserName) {
        this.context = context;
        this.postList = postList;
        this.currentUid = currentUid != null
                ? currentUid : "";
        this.currentUserName = currentUserName != null
                ? currentUserName : "";
        this.db = FirebaseFirestore.getInstance();
    }

    public void updateUserName(String name) {
        if (name != null) this.currentUserName = name;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull PostViewHolder holder, int position) {
        PostModel post = postList.get(position);

        holder.userName.setText(post.getUserName());
        holder.userRole.setText(
                "businessman".equals(post.getUserRole())
                        ? "ব্যবসায়ী" : "কৃষক");
        holder.postContent.setText(post.getContent());
        holder.postTime.setText(
                getTimeAgo(post.getTimestamp()));

        // Like count display
        int likes = post.getLikesCount();
        holder.likeCount.setText(
                likes > 0 ? String.valueOf(likes) : "Like");
        if (holder.tvLikeCountDisplay != null) {
            if (likes > 0) {
                holder.tvLikeCountDisplay.setText(
                        likes + " জন পছন্দ করেছেন");
            } else {
                holder.tvLikeCountDisplay.setText("");
            }
        }

        // Comment count display
        int comments = post.getCommentsCount();
        holder.commentCount.setText(
                comments > 0
                        ? comments + " Comments" : "Comment");
        if (holder.tvCommentCountDisplay != null) {
            if (comments > 0) {
                holder.tvCommentCountDisplay.setText(
                        comments + " টি মন্তব্য");
            } else {
                holder.tvCommentCountDisplay.setText("");
            }
        }

        // Load avatar
        if (post.getUserPhoto() != null
                && !post.getUserPhoto().isEmpty()) {
            Glide.with(context)
                    .load(post.getUserPhoto())
                    .circleCrop()
                    .placeholder(
                            android.R.drawable.ic_menu_myplaces)
                    .into(holder.avatar);
        }

        // Post image
        if (post.getImageUrl() != null
                && !post.getImageUrl().isEmpty()) {
            holder.postImage.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(post.getImageUrl())
                    .centerCrop()
                    .into(holder.postImage);
        } else {
            holder.postImage.setVisibility(View.GONE);
        }

        // Product details (businessman)
        boolean hasDemand =
                "businessman".equals(post.getUserRole())
                        && post.getProductNeeded() != null
                        && !post.getProductNeeded().isEmpty();

        if (hasDemand) {
            holder.productDetails.setVisibility(
                    View.VISIBLE);
            holder.tvProductNeeded.setText(
                    "🛒 চাই: " + post.getProductNeeded());
            holder.tvQuantityNeeded.setText(
                    "📦 পরিমাণ: "
                            + (post.getQuantity() != null
                            ? post.getQuantity() : "N/A"));
            holder.tvBudget.setText(
                    "💰 বাজেট: ৳"
                            + (post.getBudget() != null
                            ? post.getBudget() : "N/A"));
        } else {
            holder.productDetails.setVisibility(
                    View.GONE);
        }

        // Check if liked
        checkLiked(post.getPostId(), holder);

        // Like
        holder.btnLike.setOnClickListener(v ->
                toggleLike(post, holder)
        );

        // Comment
        holder.btnComment.setOnClickListener(v ->
                showCommentDialog(post, holder)
        );

        // Share
        holder.btnShare.setOnClickListener(v -> {
            Intent shareIntent =
                    new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    post.getUserName()
                            + " পোস্ট করেছেন:\n\n"
                            + post.getContent()
                            + "\n\nUTH SOB অ্যাপে দেখুন!");
            context.startActivity(
                    Intent.createChooser(
                            shareIntent, "শেয়ার করুন"));
        });

        // Click avatar or name → profile
        holder.avatar.setOnClickListener(v ->
                openProfile(post.getUserId())
        );
        holder.userName.setOnClickListener(v ->
                openProfile(post.getUserId())
        );
    }

    private void openProfile(String userId) {
        if (userId == null || userId.isEmpty()) return;
        Intent intent = new Intent(
                context, UserProfileActivity.class);
        intent.putExtra("profileUid", userId);
        context.startActivity(intent);
    }

    private void checkLiked(String postId,
                            PostViewHolder holder) {
        if (currentUid.isEmpty()
                || postId == null) return;

        db.collection("posts")
                .document(postId)
                .collection("likes")
                .document(currentUid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        holder.likeIcon.setText("❤️");
                        holder.likeCount.setTextColor(
                                context.getResources()
                                        .getColor(R.color.error));
                    } else {
                        holder.likeIcon.setText("🤍");
                        holder.likeCount.setTextColor(
                                context.getResources()
                                        .getColor(R.color.gray));
                    }
                });
    }

    private void toggleLike(PostModel post,
                            PostViewHolder holder) {
        if (currentUid.isEmpty()) {
            Toast.makeText(context,
                    "লগইন করুন!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (post.getPostId() == null) return;

        db.collection("posts")
                .document(post.getPostId())
                .collection("likes")
                .document(currentUid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // Unlike
                        doc.getReference().delete()
                                .addOnSuccessListener(u -> {
                                    int newCount = Math.max(
                                            0, post.getLikesCount()
                                                    - 1);
                                    post.setLikesCount(newCount);
                                    holder.likeIcon.setText("🤍");
                                    holder.likeCount.setText(
                                            newCount > 0
                                                    ? String.valueOf(
                                                    newCount)
                                                    : "Like");
                                    holder.likeCount.setTextColor(
                                            context.getResources()
                                                    .getColor(R.color.gray));
                                    if (holder
                                            .tvLikeCountDisplay
                                            != null) {
                                        holder.tvLikeCountDisplay
                                                .setText(newCount > 0
                                                        ? newCount
                                                        + " জন পছন্দ করেছেন"
                                                        : "");
                                    }
                                    db.collection("posts")
                                            .document(post.getPostId())
                                            .update("likesCount",
                                                    newCount);
                                });
                    } else {
                        // Like
                        Map<String, Object> like =
                                new HashMap<>();
                        like.put("uid", currentUid);
                        like.put("timestamp",
                                System.currentTimeMillis());

                        doc.getReference().set(like)
                                .addOnSuccessListener(u -> {
                                    int newCount =
                                            post.getLikesCount() + 1;
                                    post.setLikesCount(newCount);
                                    holder.likeIcon.setText("❤️");
                                    holder.likeCount.setText(
                                            String.valueOf(newCount));
                                    holder.likeCount.setTextColor(
                                            context.getResources()
                                                    .getColor(R.color.error));
                                    if (holder
                                            .tvLikeCountDisplay
                                            != null) {
                                        holder.tvLikeCountDisplay
                                                .setText(newCount
                                                        + " জন পছন্দ করেছেন");
                                    }
                                    db.collection("posts")
                                            .document(post.getPostId())
                                            .update("likesCount",
                                                    newCount);

                                    // Notify
                                    if (!currentUid.equals(
                                            post.getUserId())) {
                                        NotificationHelper
                                                .sendLikeNotification(
                                                        post.getUserId(),
                                                        currentUserName
                                                                .isEmpty()
                                                                ? "Someone"
                                                                : currentUserName,
                                                        post.getContent()
                                                                .length() > 30
                                                                ? post.getContent()
                                                                .substring(
                                                                        0, 30)
                                                                + "..."
                                                                : post.getContent()
                                                );
                                    }
                                });
                    }
                });
    }

    // Comment dialog
    private void showCommentDialog(PostModel post,
                                   PostViewHolder holder) {
        if (currentUid.isEmpty()) {
            Toast.makeText(context,
                    "লগইন করুন!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build dialog with EditText
        android.app.AlertDialog.Builder builder =
                new android.app.AlertDialog.Builder(context);
        builder.setTitle("মন্তব্য করুন");

        // Input field
        final EditText input = new EditText(context);
        input.setHint("আপনার মন্তব্য লিখুন...");
        input.setPadding(40, 20, 40, 20);
        builder.setView(input);

        builder.setPositiveButton("পোস্ট করুন",
                (dialog, which) -> {
                    String comment =
                            input.getText().toString().trim();
                    if (TextUtils.isEmpty(comment)) {
                        Toast.makeText(context,
                                "মন্তব্য লিখুন!",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    submitComment(post, comment, holder);
                });

        builder.setNegativeButton("বাতিল",
                (dialog, which) -> dialog.cancel());

        // Show recent comments first
        showCommentsWithInput(post, holder, builder);
    }

    private void showCommentsWithInput(
            PostModel post,
            PostViewHolder holder,
            android.app.AlertDialog.Builder builder) {

        // Load existing comments
        db.collection("posts")
                .document(post.getPostId())
                .collection("comments")
                .orderBy("timestamp",
                        Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(snapshots -> {
                    StringBuilder commentText =
                            new StringBuilder();
                    for (var doc : snapshots.getDocuments()) {
                        String name =
                                doc.getString("userName");
                        String text =
                                doc.getString("text");
                        if (name != null && text != null) {
                            commentText.append("👤 ")
                                    .append(name)
                                    .append(": ")
                                    .append(text)
                                    .append("\n\n");
                        }
                    }

                    if (commentText.length() > 0) {
                        builder.setMessage(
                                commentText.toString());
                    }
                    builder.show();
                })
                .addOnFailureListener(e -> builder.show());
    }

    private void submitComment(PostModel post,
                               String commentText,
                               PostViewHolder holder) {
        Map<String, Object> comment = new HashMap<>();
        comment.put("userId", currentUid);
        comment.put("userName",
                currentUserName.isEmpty()
                        ? "User" : currentUserName);
        comment.put("text", commentText);
        comment.put("timestamp",
                System.currentTimeMillis());
        comment.put("commentId",
                UUID.randomUUID().toString());

        db.collection("posts")
                .document(post.getPostId())
                .collection("comments")
                .add(comment)
                .addOnSuccessListener(ref -> {
                    int newCount =
                            post.getCommentsCount() + 1;
                    post.setCommentsCount(newCount);
                    holder.commentCount.setText(
                            newCount + " Comments");
                    if (holder.tvCommentCountDisplay
                            != null) {
                        holder.tvCommentCountDisplay.setText(
                                newCount + " টি মন্তব্য");
                    }
                    db.collection("posts")
                            .document(post.getPostId())
                            .update("commentsCount", newCount);
                    Toast.makeText(context,
                            "মন্তব্য পোস্ট হয়েছে! ✅",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    private String getTimeAgo(long timestamp) {
        long diff =
                System.currentTimeMillis() - timestamp;
        long minutes = diff / 60000;
        long hours = minutes / 60;
        long days = hours / 24;
        if (minutes < 1) return "এইমাত্র";
        if (minutes < 60) return minutes + " মিনিট আগে";
        if (hours < 24) return hours + " ঘণ্টা আগে";
        return days + " দিন আগে";
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class PostViewHolder
            extends RecyclerView.ViewHolder {
        ImageView avatar, postImage;
        TextView userName, userRole, postTime;
        TextView postContent;
        TextView likeIcon, likeCount, commentCount;
        TextView tvLikeCountDisplay,
                tvCommentCountDisplay;
        LinearLayout btnLike, btnComment, btnShare;
        LinearLayout productDetails;
        TextView tvProductNeeded,
                tvQuantityNeeded, tvBudget;

        public PostViewHolder(
                @NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(
                    R.id.post_avatar);
            postImage = itemView.findViewById(
                    R.id.post_image);
            userName = itemView.findViewById(
                    R.id.post_user_name);
            userRole = itemView.findViewById(
                    R.id.post_user_role);
            postTime = itemView.findViewById(
                    R.id.post_time);
            postContent = itemView.findViewById(
                    R.id.post_content);
            likeIcon = itemView.findViewById(
                    R.id.like_icon);
            likeCount = itemView.findViewById(
                    R.id.like_count);
            commentCount = itemView.findViewById(
                    R.id.comment_count);
            tvLikeCountDisplay = itemView.findViewById(
                    R.id.tv_like_count_display);
            tvCommentCountDisplay =
                    itemView.findViewById(
                            R.id.tv_comment_count_display);
            btnLike = itemView.findViewById(
                    R.id.btn_like);
            btnComment = itemView.findViewById(
                    R.id.btn_comment);
            btnShare = itemView.findViewById(
                    R.id.btn_share);
            productDetails = itemView.findViewById(
                    R.id.product_details);
            tvProductNeeded = itemView.findViewById(
                    R.id.tv_product_needed);
            tvQuantityNeeded = itemView.findViewById(
                    R.id.tv_quantity_needed);
            tvBudget = itemView.findViewById(
                    R.id.tv_budget);
        }
    }
}