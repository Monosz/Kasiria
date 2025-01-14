package com.example.kasiria.ui.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.kasiria.R;
import com.example.kasiria.ui.dashboard.DashboardActivity;
import com.example.kasiria.utils.Auth;

public class AuthActivity extends AppCompatActivity {
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_auth);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Failsafe for main's redirect if user is already logged in
        if (Auth.isLoggedIn()) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
            return;
        }

        toolbar = findViewById(R.id.tbAuth);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Handle fragment loading from passed intent
        if (savedInstanceState == null) {
            String fragment = getIntent().getStringExtra("fragment");
            if (fragment == null) {
                loadFragment(new LoginFragment(), "Masuk");
            } else if (fragment.equals("register")) {
                loadFragment(new RegisterFragment(), "Daftar");
            } else {
                loadFragment(new LoginFragment(), "Masuk");
            }
        }
    }

    // Load fragment and set toolbar title
    public void loadFragment(Fragment fragment, String title) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.flAuth, fragment)
                .commit();
        toolbar.setTitle(title);
    }

    // Overrides back press to go to previous stack if it exists
    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }
}