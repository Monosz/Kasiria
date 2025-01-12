    package com.example.kasiria.ui.dashboard;

    import android.content.Intent;
    import android.os.Bundle;
    import android.view.Menu;
    import android.view.MenuItem;
    import android.view.MotionEvent;
    import android.view.View;
    import android.widget.PopupMenu;
    import android.widget.Toast;

    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.appcompat.widget.Toolbar;
    import androidx.fragment.app.Fragment;

    import com.example.kasiria.R;
    import com.example.kasiria.ui.auth.AuthActivity;
    import com.google.android.material.bottomnavigation.BottomNavigationView;
    import com.google.android.material.floatingactionbutton.FloatingActionButton;
    import com.google.firebase.auth.FirebaseAuth;

    public class DashboardActivity extends AppCompatActivity {
        private BottomNavigationView nav;
        private FloatingActionButton fabDashboard;
        private Toolbar tbDashboard;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
//            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_dashboard);
//            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//                return insets;
//            });

            nav = findViewById(R.id.navDashboard);
            fabDashboard = findViewById(R.id.fabDashboard);
            tbDashboard = findViewById(R.id.tbDashboard);

            replaceFragment(new HomeFragment(), "Beranda");

            nav.setOnNavigationItemSelectedListener(item -> {
                int selected = item.getItemId();
                if (selected == R.id.navHome) {
                    replaceFragment(new HomeFragment(), "Beranda");
                } else if (selected == R.id.navProduct) {
                    replaceFragment(new ProductFragment(), "Produk");
                } else if (selected == R.id.navTransaction) {
                    replaceFragment(new TransactionFragment(), "Transaksi");
                } else if (selected == R.id.navHistory) {
                    replaceFragment(new HistoryFragment(), "Riwayat");
                } else if (selected == R.id.navProfile) {
                    replaceFragment(new ProfileFragment(), "Profil");
                }
                return true;
            });

            PopupMenu popupMenu = new PopupMenu(this, fabDashboard);
            popupMenu.getMenuInflater().inflate(R.menu.menu_fab, popupMenu.getMenu());
            fabDashboard.setOnLongClickListener(v -> {
                if (!isDragging) {
                    popupMenu.show();
                    return true;
                }
                return false;
            });

            popupMenu.setOnMenuItemClickListener(item -> {
                int selected = item.getItemId();
                if (selected == R.id.fabProductAdd) {
                    ProductDialogFragment dialogFragment = ProductDialogFragment.newInstance(null);
                    dialogFragment.show(getSupportFragmentManager(), "product_add_dialog");
                    return true;
                } else if (selected == R.id.fabTransactionAdd) {
                    TransactionDialogFragment dialogFragment = TransactionDialogFragment.newInstance(null);
                    dialogFragment.show(getSupportFragmentManager(), "transaction_add_dialog");
                    return true;
                }
                return false;
            });

            fabDashboard.setOnClickListener(v -> {
                Fragment current = getSupportFragmentManager().findFragmentById(R.id.flDashboard);
                if (current instanceof ProductFragment) {
                    ProductDialogFragment dialogFragment = ProductDialogFragment.newInstance(null);
                    dialogFragment.show(getSupportFragmentManager(), "product_add_dialog");
                } else if (current instanceof TransactionFragment) {
                    TransactionDialogFragment dialogFragment = TransactionDialogFragment.newInstance(null);
                    dialogFragment.show(getSupportFragmentManager(), "transaction_add_dialog");
                } else if (current instanceof ProfileFragment) {
                    Toast.makeText(this, "Data usaha dan pengguna akan digunakan dalam pembuatan struk transaksi", Toast.LENGTH_SHORT).show();
                }
            });

            fabDashboard.setOnTouchListener((v, event) -> fabMovement(v, event));
        }

        private void replaceFragment(Fragment fragment, String title) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.flDashboard, fragment)
                    .addToBackStack(null)
                    .commit();
            if (title.equals("Beranda")) {
                tbDashboard.setTitle("Kasiria");
//                tbDashboard.setTitleTextAppearance(this, R.style.AppBarTitleKasiria);
            } else {
                tbDashboard.setTitle(title);
            }

            fabDashboard.hide();
            if (fragment instanceof ProductFragment || fragment instanceof TransactionFragment) {
                fabDashboard.setImageResource(R.drawable.ic_add);
                fabDashboard.show();
            } else if (fragment instanceof ProfileFragment) {
                fabDashboard.setImageResource(R.drawable.ic_info);
                fabDashboard.show();
            }
        }

        @Override
        public void onStart() {
            super.onStart();
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                startActivity(new Intent(this, AuthActivity.class));
                finish();
            }
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.menu_nav, menu);
//            return super.onCreateOptionsMenu(menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//            int selected = item.getItemId();
//
//            if (selected == R.id.navHome) {
//                replaceFragment(new HomeFragment());
//                return true;
//            } else if (selected == R.id.navProduct) {
//                replaceFragment(new ProductFragment());
//                return true;
//            } else if (selected == R.id.navTransaction) {
//                replaceFragment(new TransactionFragment());
//                return true;
//            } else if (selected == R.id.navHistory) {
//                replaceFragment(new HistoryFragment());
//                return true;
//            } else if (selected == R.id.navProfile) {
//                replaceFragment(new ProfileFragment());
//                return true;
//            } else {
//                replaceFragment(new HomeFragment());
//            }
//
            return super.onOptionsItemSelected(item);
        }

        private float startX, startY;
        private float xDelta, yDelta;
        private static final float DRAG_THRESHOLD = 10f;
        private boolean isDragging = false;

        private boolean fabMovement(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // Store the initial touch coordinates
                    startX = event.getRawX();
                    startY = event.getRawY();
                    xDelta = v.getX() - event.getRawX();
                    yDelta = v.getY() - event.getRawY();

                    isDragging = false;
                    return false;

                case MotionEvent.ACTION_MOVE:
                    // Calculate new position
                    float newX = event.getRawX() + xDelta;
                    float newY = event.getRawY() + yDelta;

                    // Restrict movement within screen bounds
                    int screenWidth = getResources().getDisplayMetrics().widthPixels;
                    int screenHeight = getResources().getDisplayMetrics().heightPixels;

                    newY = Math.max(0 + 24, Math.min(newY, screenHeight - v.getHeight() - nav.getHeight() - 24));

                    v.setX(newX);
                    v.setY(newY);

                    isDragging = true;
                    return true; // Event consumed, it's a drag

                case MotionEvent.ACTION_UP:
                    // Check if movement exceeded the drag threshold
                    float deltaX = Math.abs(event.getRawX() - startX);
                    float deltaY = Math.abs(event.getRawY() - startY);
                    if (deltaX < DRAG_THRESHOLD && deltaY < DRAG_THRESHOLD) {
                        // Not a drag; allow click
                        return false;
                    }
                    snapFABToSide(v);

                    isDragging = true;
                    return true; // Event consumed as a drag

                case MotionEvent.ACTION_CANCEL:
                    isDragging = false;
                    return false;
            }
            return false;
        }

        private void snapFABToSide(View v) {
            int screenWidth = getResources().getDisplayMetrics().widthPixels;

            // Get the current position of the FAB
            float fabX = v.getX();

            // Determine the nearest side (left or right)
            float distanceToLeft = fabX;
            float distanceToRight = screenWidth - (fabX + v.getWidth());

            // Check which side is closer
            if (distanceToLeft < distanceToRight) {
                // Snap to the left side
                v.animate().x(0 + 24).setDuration(200).start();
            } else {
                // Snap to the right side
                v.animate().x(screenWidth - v.getWidth() - 24).setDuration(200).start();
            }
        }
    }