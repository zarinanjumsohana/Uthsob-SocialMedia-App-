package com.example.uthsob3o;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    // This is just a test tag for logs
    private static final String TAG = "FirebaseTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Test if Firebase connected successfully
        FirebaseApp.initializeApp(this);
        Log.d(TAG, "Firebase Connected Successfully! ✅");
    }
}