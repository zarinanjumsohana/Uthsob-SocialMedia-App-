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
import java.util.HashMap;
import java.util.Map;

public class AdminVerificationActivity extends AppCompatActivity {

    ImageView ivSelfie;
    TextView tvTakeSelfie, btnBack;
    Button btnSubmitVerification;
    ProgressBar progressBar;
    TextView tvStatus;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String currentUid;
    Uri selfieUri = null;

    // Camera/gallery launcher
    ActivityResultLauncher<String> selfieLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            selfieUri = uri;
                            Glide.with(this).load(uri).into(ivSelfie);
                            tvTakeSelfie.setText("সেলফি নেওয়া হয়েছে ✅");
                            btnSubmitVerification.setEnabled(true);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_verification);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            currentUid = mAuth.getCurrentUser().getUid();
        }

        ivSelfie = findViewById(R.id.iv_selfie);
        tvTakeSelfie = findViewById(R.id.tv_take_selfie);
        btnBack = findViewById(R.id.btn_back);
        btnSubmitVerification = findViewById(
                R.id.btn_submit_verification);
        progressBar = findViewById(R.id.progress_bar);
        tvStatus = findViewById(R.id.tv_status);

        btnSubmitVerification.setEnabled(false);
        btnBack.setOnClickListener(v -> finish());

        // Check current verification status
        checkVerificationStatus();

        ivSelfie.setOnClickListener(v ->
                selfieLauncher.launch("image/*")
        );
        tvTakeSelfie.setOnClickListener(v ->
                selfieLauncher.launch("image/*")
        );

        btnSubmitVerification.setOnClickListener(v ->
                submitVerification()
        );
    }

    private void checkVerificationStatus() {
        if (currentUid == null) return;

        db.collection("users")
                .document(currentUid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String status = doc.getString("status");
                        boolean verified = Boolean.TRUE.equals(
                                doc.getBoolean("verified"));

                        if (verified) {
                            tvStatus.setText(
                                    "✅ আপনি ইতিমধ্যে যাচাইকৃত!");
                            tvStatus.setTextColor(
                                    getResources().getColor(
                                            R.color.bright_green));
                            btnSubmitVerification.setEnabled(false);
                        } else if ("pending".equals(status)) {
                            tvStatus.setText(
                                    "⏳ যাচাইয়ের জন্য অপেক্ষা করুন...\n"
                                            + "Admin শীঘ্রই পর্যালোচনা করবেন।");
                            tvStatus.setTextColor(
                                    getResources().getColor(
                                            R.color.warning));
                            btnSubmitVerification.setEnabled(false);
                        } else if ("rejected".equals(status)) {
                            tvStatus.setText(
                                    "❌ যাচাই প্রত্যাখ্যাত!\n"
                                            + "নতুন সেলফি দিয়ে আবার চেষ্টা করুন।");
                            tvStatus.setTextColor(
                                    getResources().getColor(
                                            R.color.error));
                        } else {
                            tvStatus.setText(
                                    "📸 একটি স্পষ্ট সেলফি তুলুন\n"
                                            + "Admin পর্যালোচনা করে অনুমোদন করবেন।");
                            tvStatus.setTextColor(
                                    getResources().getColor(
                                            R.color.gray));
                        }
                    }
                });
    }

    private void submitVerification() {
        if (selfieUri == null) {
            Toast.makeText(this,
                    "প্রথমে একটি সেলফি তুলুন!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSubmitVerification.setEnabled(false);

        // Upload selfie to Cloudinary
        ImageUploadHelper.uploadImage(this, selfieUri,
                new ImageUploadHelper.UploadCallback() {
                    @Override
                    public void onSuccess(String selfieUrl) {
                        saveVerificationRequest(selfieUrl);
                    }

                    @Override
                    public void onFailure(String error) {
                        progressBar.setVisibility(View.GONE);
                        btnSubmitVerification.setEnabled(true);
                        Toast.makeText(
                                AdminVerificationActivity.this,
                                "আপলোড ব্যর্থ: " + error,
                                Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void saveVerificationRequest(String selfieUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("selfieUrl", selfieUrl);
        updates.put("status", "pending");
        updates.put("verificationRequestTime",
                System.currentTimeMillis());

        db.collection("users").document(currentUid)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    // Save to verification_requests collection
                    // for admin to review
                    Map<String, Object> request =
                            new HashMap<>();
                    request.put("uid", currentUid);
                    request.put("selfieUrl", selfieUrl);
                    request.put("status", "pending");
                    request.put("timestamp",
                            System.currentTimeMillis());

                    db.collection("verification_requests")
                            .document(currentUid)
                            .set(request)
                            .addOnSuccessListener(v -> {
                                progressBar.setVisibility(View.GONE);
                                tvStatus.setText(
                                        "✅ আবেদন জমা দেওয়া হয়েছে!\n"
                                                + "Admin শীঘ্রই পর্যালোচনা করবেন।");
                                tvStatus.setTextColor(
                                        getResources().getColor(
                                                R.color.bright_green));
                                Toast.makeText(this,
                                        "যাচাইয়ের আবেদন সফল! ✅",
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSubmitVerification.setEnabled(true);
                    Toast.makeText(this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}