package com.example.uthsob3o.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.uthsob3o.R;
import com.example.uthsob3o.adapters.FeedAdapter;
import com.example.uthsob3o.models.CropModel;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    EditText etSearch;
    RecyclerView rvResults;
    TextView btnBack, tvEmpty, tvResultCount;
    LinearLayout filterAll, filterVegetable,
            filterFruit, filterGrain,
            filterFarmer;

    List<CropModel> allCrops = new ArrayList<>();
    List<CropModel> filteredCrops = new ArrayList<>();
    FeedAdapter adapter;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    String currentRole = "";
    String currentUid = "";
    String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        currentRole = getIntent()
                .getStringExtra("role") != null
                ? getIntent().getStringExtra("role") : "";
        currentUid = getIntent()
                .getStringExtra("uid") != null
                ? getIntent().getStringExtra("uid") : "";

        if (currentUid.isEmpty()
                && mAuth.getCurrentUser() != null) {
            currentUid = mAuth.getCurrentUser()
                    .getUid();
        }

        etSearch = findViewById(R.id.et_search);
        rvResults = findViewById(R.id.rv_results);
        btnBack = findViewById(R.id.btn_back);
        tvEmpty = findViewById(R.id.tv_empty);
        tvResultCount = findViewById(
                R.id.tv_result_count);
        filterAll = findViewById(R.id.filter_all);
        filterVegetable = findViewById(
                R.id.filter_vegetable);
        filterFruit = findViewById(R.id.filter_fruit);
        filterGrain = findViewById(R.id.filter_grain);
        filterFarmer = findViewById(
                R.id.filter_farmer);

        adapter = new FeedAdapter(this, filteredCrops,
                currentRole, currentUid);
        rvResults.setLayoutManager(
                new LinearLayoutManager(this));
        rvResults.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        // Load all crops for search
        loadAllCrops();

        // Search listener
        etSearch.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s, int i,
                            int i1, int i2) {}

                    @Override
                    public void onTextChanged(
                            CharSequence s, int i,
                            int i1, int i2) {
                        filterResults(s.toString());
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s) {}
                });

        // Filter buttons
        setupFilterButtons();
    }

    private void setupFilterButtons() {
        filterAll.setOnClickListener(v -> {
            currentFilter = "all";
            updateFilterUI(filterAll);
            filterResults(etSearch.getText()
                    .toString());
        });

        filterVegetable.setOnClickListener(v -> {
            currentFilter = "সবজি";
            updateFilterUI(filterVegetable);
            filterResults(etSearch.getText()
                    .toString());
        });

        filterFruit.setOnClickListener(v -> {
            currentFilter = "ফল";
            updateFilterUI(filterFruit);
            filterResults(etSearch.getText()
                    .toString());
        });

        filterGrain.setOnClickListener(v -> {
            currentFilter = "শস্য";
            updateFilterUI(filterGrain);
            filterResults(etSearch.getText()
                    .toString());
        });

        filterFarmer.setOnClickListener(v -> {
            currentFilter = "farmer";
            updateFilterUI(filterFarmer);
            filterResults(etSearch.getText()
                    .toString());
        });
    }

    private void loadAllCrops() {
        db.collection("crops").get()
                .addOnSuccessListener(snapshots -> {
                    allCrops.clear();
                    for (var doc : snapshots.getDocuments()) {
                        CropModel crop =
                                doc.toObject(CropModel.class);
                        if (crop != null) {
                            crop.setCropId(doc.getId());
                            allCrops.add(crop);
                        }
                    }
                    // Show all by default
                    filteredCrops.clear();
                    filteredCrops.addAll(allCrops);
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                });
    }

    private void filterResults(String query) {
        String q = query.toLowerCase().trim();
        filteredCrops.clear();

        for (CropModel crop : allCrops) {
            boolean matchesQuery = q.isEmpty();

            if (!q.isEmpty()) {
                // Search by product name
                boolean matchName =
                        crop.getCropName() != null
                                && crop.getCropName()
                                .toLowerCase().contains(q);

                // Search by farmer name
                boolean matchFarmer =
                        crop.getFarmerName() != null
                                && crop.getFarmerName()
                                .toLowerCase().contains(q);

                // Search by location
                boolean matchLocation =
                        crop.getFarmerLocation() != null
                                && crop.getFarmerLocation()
                                .toLowerCase().contains(q);

                matchesQuery = matchName
                        || matchFarmer
                        || matchLocation;
            }

            // Apply category filter
            boolean matchesFilter = true;
            if (!"all".equals(currentFilter)) {
                if ("farmer".equals(currentFilter)) {
                    // Filter by farmer name search
                    matchesFilter =
                            crop.getFarmerName() != null
                                    && crop.getFarmerName()
                                    .toLowerCase()
                                    .contains(q.isEmpty()
                                            ? "" : q);
                } else {
                    // Filter by crop type keyword
                    matchesFilter =
                            crop.getCropName() != null
                                    && crop.getCropName()
                                    .toLowerCase()
                                    .contains(currentFilter);
                }
            }

            if (matchesQuery && matchesFilter) {
                filteredCrops.add(crop);
            }
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        int count = filteredCrops.size();

        if (count == 0) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvResults.setVisibility(View.GONE);
            tvEmpty.setText(
                    "🔍\n\nকিছু পাওয়া যায়নি!\n"
                            + "অন্য কীওয়ার্ড দিয়ে খুঁজুন।");
            if (tvResultCount != null) {
                tvResultCount.setVisibility(View.GONE);
            }
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvResults.setVisibility(View.VISIBLE);
            if (tvResultCount != null) {
                tvResultCount.setVisibility(View.VISIBLE);
                tvResultCount.setText(
                        count + " টি ফলাফল পাওয়া গেছে");
            }
        }
    }

    private void updateFilterUI(LinearLayout selected) {
        int activeColor = 0xFF1B4D1E;
        int inactiveColor = 0xFFFFFFFF;

        setFilterStyle(filterAll, inactiveColor);
        setFilterStyle(filterVegetable, inactiveColor);
        setFilterStyle(filterFruit, inactiveColor);
        setFilterStyle(filterGrain, inactiveColor);
        setFilterStyle(filterFarmer, inactiveColor);
        setFilterStyle(selected, activeColor);
    }

    private void setFilterStyle(
            LinearLayout layout, int bgColor) {
        if (layout == null) return;
        if (bgColor == 0xFF1B4D1E) {
            layout.setBackgroundResource(
                    R.drawable.btn_green_solid);
            if (layout.getChildCount() > 0) {
                ((TextView) layout.getChildAt(0))
                        .setTextColor(0xFFFFFFFF);
            }
        } else {
            layout.setBackgroundResource(
                    R.drawable.bg_input_field);
            if (layout.getChildCount() > 0) {
                ((TextView) layout.getChildAt(0))
                        .setTextColor(0xFF1B4D1E);
            }
        }
    }
}