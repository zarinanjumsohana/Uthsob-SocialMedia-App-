package com.example.uthsob3o.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.uthsob3o.ImageUploadHelper;
import com.example.uthsob3o.R;
import com.example.uthsob3o.models.StoryModel;
import java.util.UUID;

public class AddStoryActivity extends AppCompatActivity {

    ImageView ivStoryPreview;
    TextView tvSelectPhoto, btnBack;
    Button btnPostStory;
    ProgressBar progressBar;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String currentUid;
    Uri selectedImageUri = null;

    // Image picker
    ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            selectedImageUri = uri;
                            Glide.with(this).load(uri).into(ivStoryPreview);
                            tvSelectPhoto.setText("ছবি নির্বাচন করা হয়েছে ✅");
                            btnPostStory.setEnabled(true);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            currentUid = mAuth.getCurrentUser().getUid();
        }

        ivStoryPreview = findViewById(R.id.iv_story_preview);
        tvSelectPhoto = findViewById(R.id.tv_select_photo);
        btnBack = findViewById(R.id.btn_back);
        btnPostStory = findViewById(R.id.btn_post_story);
        progressBar = findViewById(R.id.progress_bar);

        // Disable post button until image selected
        btnPostStory.setEnabled(false);

        btnBack.setOnClickListener(v -> finish());

        // Click image area to select photo
        ivStoryPreview.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*")
        );
        tvSelectPhoto.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*")
        );

        btnPostStory.setOnClickListener(v -> postStory());
    }

    private void postStory() {
        if (selectedImageUri == null) {
            Toast.makeText(this,
                    "প্রথমে একটি ছবি নির্বাচন করুন!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnPostStory.setEnabled(false);

        Toast.makeText(this,
                "স্টোরি আপলোড হচ্ছে...",
                Toast.LENGTH_SHORT).show();

        // Upload image to Cloudinary
        ImageUploadHelper.uploadImage(this, selectedImageUri,
                new ImageUploadHelper.UploadCallback() {
                    @Override
                    public void onSuccess(String imageUrl) {
                        saveStoryToFirebase(imageUrl);
                    }

                    @Override
                    public void onFailure(String error) {
                        progressBar.setVisibility(View.GONE);
                        btnPostStory.setEnabled(true);
                        Toast.makeText(AddStoryActivity.this,
                                "ছবি আপলোড ব্যর্থ: " + error,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveStoryToFirebase(String imageUrl) {
        // Get user info first
        db.collection("users").document(currentUid).get()
                .addOnSuccessListener(doc -> {
                    String userName = doc.getString("name");
                    String userPhoto = doc.getString("photoUrl");

                    String storyId = UUID.randomUUID().toString();
                    StoryModel story = new StoryModel(
                            storyId, currentUid,
                            userName != null ? userName : "User",
                            imageUrl
                    );
                    if (userPhoto != null) {
                        story.setUserPhoto(userPhoto);
                    }

                    // Save to Firestore
                    db.collection("stories").document(storyId)
                            .set(story)
                            .addOnSuccessListener(unused -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(this,
                                        "স্টোরি পোস্ট হয়েছে! ✅",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                btnPostStory.setEnabled(true);
                                Toast.makeText(this,
                                        "Error: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                });
    }
}