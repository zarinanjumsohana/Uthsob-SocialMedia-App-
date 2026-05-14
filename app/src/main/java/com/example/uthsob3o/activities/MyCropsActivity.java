package com.example.uthsob3o.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.example.uthsob3o.R;
import com.example.uthsob3o.adapters.MyCropsAdapter;
import com.example.uthsob3o.models.CropModel;
import java.util.ArrayList;
import java.util.List;

public class MyCropsActivity extends AppCompatActivity {

    RecyclerView rvMyCrops;
    TextView btnBack, tvEmpty;
    LinearLayout btnAddNew;

    List<CropModel> myCropList = new ArrayList<>();
    MyCropsAdapter adapter;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    ListenerRegistration listener;
    String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_crops);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            currentUid = mAuth.getCurrentUser().getUid();
        }

        rvMyCrops = findViewById(R.id.rv_my_crops);
        btnBack = findViewById(R.id.btn_back);
        tvEmpty = findViewById(R.id.tv_empty);
        btnAddNew = findViewById(R.id.btn_add_new);

        adapter = new MyCropsAdapter(this, myCropList,
                this::onEditCrop, this::onDeleteCrop);
        rvMyCrops.setLayoutManager(new LinearLayoutManager(this));
        rvMyCrops.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        btnAddNew.setOnClickListener(v ->
                startActivity(new Intent(this, AddCropActivity.class))
        );

        loadMyCrops();
    }

    private void loadMyCrops() {
        if (currentUid == null) return;

        listener = db.collection("crops")
                .whereEqualTo("farmerId", currentUid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) return;
                    if (snapshots != null) {
                        myCropList.clear();
                        for (var doc : snapshots.getDocuments()) {
                            CropModel crop = doc.toObject(CropModel.class);
                            if (crop != null) {
                                crop.setCropId(doc.getId());
                                myCropList.add(crop);
                            }
                        }
                        adapter.notifyDataSetChanged();

                        if (myCropList.isEmpty()) {
                            tvEmpty.setVisibility(android.view.View.VISIBLE);
                            rvMyCrops.setVisibility(android.view.View.GONE);
                        } else {
                            tvEmpty.setVisibility(android.view.View.GONE);
                            rvMyCrops.setVisibility(android.view.View.VISIBLE);
                        }
                    }
                });
    }

    private void onEditCrop(CropModel crop) {
        Intent intent = new Intent(this, EditCropActivity.class);
        intent.putExtra("cropId", crop.getCropId());
        intent.putExtra("cropName", crop.getCropName());
        intent.putExtra("quantity", crop.getQuantity());
        intent.putExtra("basePrice", String.valueOf(crop.getBasePrice()));
        startActivity(intent);
    }

    private void onDeleteCrop(CropModel crop) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("ফসল মুছে ফেলুন")
                .setMessage("আপনি কি নিশ্চিত? এই ফসলের সকল বিড মুছে যাবে!")
                .setPositiveButton("হ্যাঁ, মুছুন", (dialog, which) -> {
                    db.collection("crops").document(crop.getCropId())
                            .delete()
                            .addOnSuccessListener(unused ->
                                    Toast.makeText(this,
                                            "ফসল মুছে ফেলা হয়েছে!",
                                            Toast.LENGTH_SHORT).show()
                            );
                })
                .setNegativeButton("বাতিল", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null) listener.remove();
    }
}