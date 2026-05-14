package com.example.uthsob3o.activities;
import com.example.uthsob3o.models.PostModel;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.uthsob3o.R;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddPostActivity extends AppCompatActivity {

    EditText etProductNeeded, etQuantity, etBudget, etDescription;
    Button btnSubmit;
    TextView btnBack;
    ProgressBar progressBar;

    FirebaseAuth mAuth;
    FirebaseFirestore db;
    String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            currentUid = mAuth.getCurrentUser().getUid();
        }

        etProductNeeded = findViewById(R.id.et_product_needed);
        etQuantity = findViewById(R.id.et_quantity);
        etBudget = findViewById(R.id.et_budget);
        etDescription = findViewById(R.id.et_description);
        btnSubmit = findViewById(R.id.btn_submit);
        btnBack = findViewById(R.id.btn_back);
        progressBar = findViewById(R.id.progress_bar);

        btnBack.setOnClickListener(v -> finish());
        btnSubmit.setOnClickListener(v -> submitPost());
    }

    private void submitPost() {
        String product = etProductNeeded.getText()
                .toString().trim();
        String quantity = etQuantity.getText()
                .toString().trim();
        String budget = etBudget.getText()
                .toString().trim();
        String description = etDescription.getText()
                .toString().trim();

        if (TextUtils.isEmpty(product)) {
            etProductNeeded.setError("পণ্যের নাম দিন!");
            return;
        }
        if (TextUtils.isEmpty(quantity)) {
            etQuantity.setError("পরিমাণ দিন!");
            return;
        }
        if (TextUtils.isEmpty(budget)) {
            etBudget.setError("বাজেট দিন!");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        db.collection("users").document(currentUid).get()
                .addOnSuccessListener(doc -> {
                    String businessmanName =
                            doc.getString("name");
                    String businessmanPhoto =
                            doc.getString("photoUrl");

                    String postId = UUID.randomUUID().toString();

                    // Create PostModel properly
                    PostModel post = new PostModel(
                            postId,
                            currentUid,
                            businessmanName != null
                                    ? businessmanName : "ব্যবসায়ী",
                            "businessman",
                            description.isEmpty()
                                    ? product + " চাই" : description
                    );
                    post.setProductNeeded(product);
                    post.setQuantity(quantity);
                    post.setBudget(budget);
                    post.setStatus("active");
                    if (businessmanPhoto != null) {
                        post.setUserPhoto(businessmanPhoto);
                    }

                    db.collection("posts")
                            .document(postId)
                            .set(post)
                            .addOnSuccessListener(unused -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(this,
                                        "পোস্ট সফলভাবে যোগ হয়েছে! ✅",
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