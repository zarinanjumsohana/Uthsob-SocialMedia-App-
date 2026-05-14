package com.example.uthsob3o.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.uthsob3o.R;
import java.util.HashMap;
import java.util.Map;

public class EditCropActivity extends AppCompatActivity {

    EditText etCropName, etQuantity, etBasePrice;
    Button btnUpdate;
    TextView btnBack;
    ProgressBar progressBar;

    FirebaseFirestore db;
    String cropId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_crop);

        db = FirebaseFirestore.getInstance();

        cropId = getIntent().getStringExtra("cropId");
        String cropName = getIntent().getStringExtra("cropName");
        String quantity = getIntent().getStringExtra("quantity");
        String basePrice = getIntent().getStringExtra("basePrice");

        etCropName = findViewById(R.id.et_crop_name);
        etQuantity = findViewById(R.id.et_quantity);
        etBasePrice = findViewById(R.id.et_base_price);
        btnUpdate = findViewById(R.id.btn_update);
        btnBack = findViewById(R.id.btn_back);
        progressBar = findViewById(R.id.progress_bar);

        // Pre-fill existing data
        if (cropName != null) etCropName.setText(cropName);
        if (quantity != null) etQuantity.setText(quantity);
        if (basePrice != null) etBasePrice.setText(basePrice);

        btnBack.setOnClickListener(v -> finish());
        btnUpdate.setOnClickListener(v -> updateCrop());
    }

    private void updateCrop() {
        String name = etCropName.getText().toString().trim();
        String quantity = etQuantity.getText().toString().trim();
        String price = etBasePrice.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etCropName.setError("নাম দিন!"); return; }
        if (TextUtils.isEmpty(quantity)) {
            etQuantity.setError("পরিমাণ দিন!"); return; }
        if (TextUtils.isEmpty(price)) {
            etBasePrice.setError("মূল্য দিন!"); return; }

        progressBar.setVisibility(View.VISIBLE);
        btnUpdate.setEnabled(false);

        Map<String, Object> updates = new HashMap<>();
        updates.put("cropName", name);
        updates.put("quantity", quantity);
        updates.put("basePrice", Double.parseDouble(price));

        db.collection("crops").document(cropId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this,
                            "ফসল আপডেট করা হয়েছে! ✅",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnUpdate.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}