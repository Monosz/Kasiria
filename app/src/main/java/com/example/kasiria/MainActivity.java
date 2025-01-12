package com.example.kasiria;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kasiria.adapter.GlideImageAdapter;
import com.example.kasiria.ui.auth.AuthActivity;
import com.example.kasiria.ui.dashboard.DashboardActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.PersistentCacheSettings;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static final Handler handler = new Handler();
    private RecyclerView rvMainGlide;
    private int currPos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Force light mode to burn user eye :D
        // (Light mode easier to see with direct sunlight)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        // Allow firestore local caching
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                .build();
        db.setFirestoreSettings(settings);

        // If user is already logged in, redirect to dashboard
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
            return;
        }

        // Register and login buttons
        Button btnMainRegister = findViewById(R.id.btnMainRegister);
        btnMainRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, AuthActivity.class);
            intent.putExtra("fragment", "register");
            startActivity(intent);
        });

        Button btnMainLogin = findViewById(R.id.btnMainLogin);
        btnMainLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, AuthActivity.class);
            intent.putExtra("fragment", "login");
            startActivity(intent);
        });

        // Image Carousel RV
        rvMainGlide = findViewById(R.id.rvMainGlide);
        ArrayList<Integer> images = new ArrayList<>(
                Arrays.asList(R.drawable.carousel1,
                R.drawable.carousel2, R.drawable.carousel3,
                R.drawable.carousel4, R.drawable.carousel5));

        GlideImageAdapter adapter = new GlideImageAdapter(this, images);
        rvMainGlide.setAdapter(adapter);

        // Set at layout file (CarouselLayoutManager)
        // rvMainGlide.setLayoutManager();

        // Center the in-between image
        LinearSnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(rvMainGlide);

        autoscrollImages(images.size());
    }

    private void autoscrollImages(int count) {
        Runnable scroll = new Runnable() {
            @Override
            public void run() {
                if (currPos == count) {
                    currPos = 0;
                }

                rvMainGlide.smoothScrollToPosition(currPos++);
                handler.postDelayed(this, 4000);
            }
        };
        handler.post(scroll);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}