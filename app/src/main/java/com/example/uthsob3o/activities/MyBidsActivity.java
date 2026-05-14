package com.example.uthsob3o.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.example.uthsob3o.R;
import com.example.uthsob3o.adapters.BidHistoryAdapter;
import com.example.uthsob3o.models.BidModel;
import java.util.ArrayList;
import java.util.List;

public class MyBidsActivity extends AppCompatActivity {

    RecyclerView rvMyBids;
    TextView btnBack, tvEmpty;
    List<BidModel> myBidList = new ArrayList<>();
    BidHistoryAdapter adapter;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bids);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            currentUid = mAuth.getCurrentUser().getUid();
        }

        rvMyBids = findViewById(R.id.rv_my_bids);
        btnBack = findViewById(R.id.btn_back);
        tvEmpty = findViewById(R.id.tv_empty);

        adapter = new BidHistoryAdapter(this, myBidList);
        rvMyBids.setLayoutManager(new LinearLayoutManager(this));
        rvMyBids.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
        loadMyBids();
    }

    private void loadMyBids() {
        if (currentUid == null) return;

        db.collection("bids")
                .whereEqualTo("businessmanId", currentUid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshots -> {
                    myBidList.clear();
                    int rank = 1;
                    for (var doc : snapshots.getDocuments()) {
                        BidModel bid = doc.toObject(BidModel.class);
                        if (bid != null) {
                            bid.setRank(rank++);
                            myBidList.add(bid);
                        }
                    }
                    adapter.notifyDataSetChanged();

                    if (myBidList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        rvMyBids.setVisibility(View.GONE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                        rvMyBids.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    tvEmpty.setText("Error loading bids: " + e.getMessage());
                    tvEmpty.setVisibility(View.VISIBLE);
                });
    }
}