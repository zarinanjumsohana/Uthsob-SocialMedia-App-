package com.example.uthsob3o.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.uthsob3o.models.CropModel;
import java.util.Calendar;
import java.util.UUID;

public class AddCropActivity extends AppCompatActivity {

    EditText etCropName, etScientificName, etQuantity, etUnit, etBasePrice;
    TextView etCultivationDate, etProductionDate, etExpiryDate, etAuctionEndDate;
    ImageView ivCropImage;
    TextView tvUploadPhoto;
    Button btnSubmit;
    TextView btnBack;
    ProgressBar progressBar;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String currentUid;
    Uri selectedImageUri = null;
    String uploadedImageUrl = "";

    // Image picker launcher
    ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            selectedImageUri = uri;
                            // Show preview
                            Glide.with(this).load(uri).into(ivCropImage);
                            tvUploadPhoto.setText("ছবি নির্বাচন করা হয়েছে ✅");
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_crop);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            currentUid = mAuth.getCurrentUser().getUid();
        }

        initViews();
        setupDatePickers();

        btnBack.setOnClickListener(v -> finish());

        // Image upload click
        ivCropImage.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*")
        );
        tvUploadPhoto.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*")
        );

        btnSubmit.setOnClickListener(v -> submitCrop());
    }

    private void initViews() {
        etCropName = findViewById(R.id.et_crop_name);
        etScientificName = findViewById(R.id.et_scientific_name);
        etQuantity = findViewById(R.id.et_quantity);
        etUnit = findViewById(R.id.et_unit);
        etBasePrice = findViewById(R.id.et_base_price);
        etCultivationDate = findViewById(R.id.et_cultivation_date);
        etProductionDate = findViewById(R.id.et_production_date);
        etExpiryDate = findViewById(R.id.et_expiry_date);
        etAuctionEndDate = findViewById(R.id.et_auction_end_date);
        ivCropImage = findViewById(R.id.iv_crop_image);
        tvUploadPhoto = findViewById(R.id.tv_upload_photo);
        btnSubmit = findViewById(R.id.btn_submit);
        btnBack = findViewById(R.id.btn_back);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupDatePickers() {
        etCultivationDate.setOnClickListener(v ->
                showDatePicker(etCultivationDate));
        etProductionDate.setOnClickListener(v ->
                showDatePicker(etProductionDate));
        etExpiryDate.setOnClickListener(v ->
                showDatePicker(etExpiryDate));
        etAuctionEndDate.setOnClickListener(v ->
                showDatePicker(etAuctionEndDate));
    }

    private void showDatePicker(TextView target) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, year, month, day) ->
                        target.setText(day + "/" + (month+1) + "/" + year),
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void submitCrop() {
        String cropName = etCropName.getText().toString().trim();
        String scientificName = etScientificName.getText().toString().trim();
        String quantity = etQuantity.getText().toString().trim();
        String unit = etUnit.getText().toString().trim();
        String basePriceStr = etBasePrice.getText().toString().trim();
        String cultivationDate = etCultivationDate.getText().toString();
        String productionDate = etProductionDate.getText().toString();
        String expiryDate = etExpiryDate.getText().toString();
        String auctionEndDate = etAuctionEndDate.getText().toString();

        // Validation
        if (TextUtils.isEmpty(cropName)) {
            etCropName.setError("ফসলের নাম দিন!"); return; }
        if (TextUtils.isEmpty(quantity)) {
            etQuantity.setError("পরিমাণ দিন!"); return; }
        if (TextUtils.isEmpty(basePriceStr)) {
            etBasePrice.setError("বেস প্রাইস দিন!"); return; }
        if (cultivationDate.equals("চাষের তারিখ নির্বাচন করুন")) {
            Toast.makeText(this,
                    "চাষের তারিখ দিন!", Toast.LENGTH_SHORT).show(); return; }
        if (auctionEndDate.equals("নিলামের শেষ তারিখ নির্বাচন করুন")) {
            Toast.makeText(this,
                    "নিলামের শেষ তারিখ দিন!", Toast.LENGTH_SHORT).show(); return; }

        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        // Upload image first if selected
        if (selectedImageUri != null) {
            Toast.makeText(this,
                    "ছবি আপলোড হচ্ছে...", Toast.LENGTH_SHORT).show();

            ImageUploadHelper.uploadImage(this, selectedImageUri,
                    new ImageUploadHelper.UploadCallback() {
                        @Override
                        public void onSuccess(String imageUrl) {
                            uploadedImageUrl = imageUrl;
                            saveCropToFirestore(cropName, scientificName,
                                    quantity, unit, basePriceStr,
                                    cultivationDate, productionDate,
                                    expiryDate, auctionEndDate);
                        }

                        @Override
                        public void onFailure(String error) {
                            // Continue without image
                            uploadedImageUrl = "";
                            saveCropToFirestore(cropName, scientificName,
                                    quantity, unit, basePriceStr,
                                    cultivationDate, productionDate,
                                    expiryDate, auctionEndDate);
                        }
                    });
        } else {
            saveCropToFirestore(cropName, scientificName,
                    quantity, unit, basePriceStr,
                    cultivationDate, productionDate,
                    expiryDate, auctionEndDate);
        }
    }

    private void saveCropToFirestore(String cropName, String scientificName,
                                     String quantity, String unit,
                                     String basePriceStr,
                                     String cultivationDate,
                                     String productionDate,
                                     String expiryDate,
                                     String auctionEndDate) {
        double basePrice = Double.parseDouble(basePriceStr);

        db.collection("users").document(currentUid).get()
                .addOnSuccessListener(doc -> {
                    String farmerName = doc.getString("name");
                    String farmerLocation = doc.getString("location");
                    boolean isVerified = Boolean.TRUE.equals(
                            doc.getBoolean("verified"));

                    String cropId = UUID.randomUUID().toString();
                    CropModel crop = new CropModel(
                            cropId, currentUid, farmerName,
                            farmerLocation, isVerified,
                            cropName, quantity,
                            unit.isEmpty() ? "KG" : unit,
                            basePrice, auctionEndDate
                    );
                    crop.setScientificName(scientificName);
                    crop.setCultivationDate(cultivationDate);
                    crop.setProductionDate(productionDate);
                    crop.setExpiryDate(expiryDate);
                    crop.setImageUrl(uploadedImageUrl);

                    db.collection("crops").document(cropId)
                            .set(crop)
                            .addOnSuccessListener(unused -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(this,
                                        "ফসল সফলভাবে যোগ করা হয়েছে! ✅",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                btnSubmit.setEnabled(true);
                                Toast.makeText(this,
                                        "Error: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                });
    }
}