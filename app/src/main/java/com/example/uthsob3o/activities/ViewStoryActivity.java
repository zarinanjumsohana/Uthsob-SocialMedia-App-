package com.example.uthsob3o.activities;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.uthsob3o.R;

public class ViewStoryActivity extends AppCompatActivity {

    ImageView ivStory;
    TextView tvUserName, btnClose;
    ProgressBar progressBar;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_story);

        ivStory = findViewById(R.id.iv_story);
        tvUserName = findViewById(R.id.tv_user_name);
        btnClose = findViewById(R.id.btn_close);
        progressBar = findViewById(R.id.progress_bar);

        String imageUrl = getIntent().getStringExtra("imageUrl");
        String userName = getIntent().getStringExtra("userName");

        if (userName != null) tvUserName.setText(userName);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .into(ivStory);
        }

        btnClose.setOnClickListener(v -> finish());

        // Auto close after 5 seconds
        handler.postDelayed(this::finish, 5000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}