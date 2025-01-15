package com.example.kasiria.ui.dashboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.kasiria.R;
import com.example.kasiria.ui.auth.AuthActivity;
import com.example.kasiria.utils.Auth;
import com.example.kasiria.utils.Format;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class HomeFragment extends Fragment {

    private TextView tvHomeHello, tvHomeUsername;
    private FirebaseFirestore db;

    private TextView tvHomeDaily, tvHomeWeekly, tvHomeYesterday, tvHomeLastWeek;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        Context context = view.getContext();

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvHomeDaily = view.findViewById(R.id.tvHomeDaily);
        tvHomeWeekly = view.findViewById(R.id.tvHomeWeekly);
        tvHomeYesterday = view.findViewById(R.id.tvHomeYesterday);
        tvHomeLastWeek = view.findViewById(R.id.tvHomeLastWeek);

        tvHomeHello = view.findViewById(R.id.tvHomeHello);
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        String hello;
        if (hour < 12) {
            hello = "Selamat pagi,";
        } else if (hour < 17) {
            hello = "Selamat siang,";
        } else if (hour < 19) {
            hello = "Selamat sore,";
        } else {
            hello = "Selamat malam,";
        }
        tvHomeHello.setText(hello);

        tvHomeUsername = view.findViewById(R.id.tvHomeUsername);
        String uid = Auth.getUid();
        db.collection("users").document(uid).get().addOnSuccessListener(task -> {
            tvHomeUsername.setText(Objects.requireNonNull(task.get("name")).toString());
        });

        ImageButton ibHome = view.findViewById(R.id.ibHome);

        ImageButton ibHomeToProduct = view.findViewById(R.id.ibHomeToProduct);
        ibHomeToProduct.setOnClickListener(v -> {
            ((DashboardActivity)requireActivity()).loadFragment(new ProductFragment(), "Produk");
        });

        ImageButton ibHomeToTransaction = view.findViewById(R.id.ibHomeToTransaction);
        ibHomeToTransaction.setOnClickListener(v -> {
            ((DashboardActivity)requireActivity()).loadFragment(new TransactionFragment(), "Transaction");
        });

        ImageButton ibHomeToHistory = view.findViewById(R.id.ibHomeToHistory);
        ibHomeToHistory.setOnClickListener(v -> {
            ((DashboardActivity)requireActivity()).loadFragment(new HistoryFragment(), "Riwayat");
        });

        ImageButton ibHomeToProfile = view.findViewById(R.id.ibHomeToProfile);
        ibHomeToProfile.setOnClickListener(v -> {
            ((DashboardActivity)requireActivity()).loadFragment(new ProfileFragment(), "Profil");
        });

        SharedPreferences pref = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        String bid = pref.getString("bid", null);
        if (bid != null) {
            getSalesData(bid);
        } else {
            Toast.makeText(context, "Terjadi kesalahan, mohon login kembali", Toast.LENGTH_SHORT).show();
            Auth.signOut();
            context.startActivity(new Intent(context, AuthActivity.class));
            requireActivity().finish();
        }

        return view;
    }

    private void getSalesData(String bid) {
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.set(Calendar.HOUR_OF_DAY, 0);
        todayCalendar.set(Calendar.MINUTE, 0);
        todayCalendar.set(Calendar.SECOND, 0);
        todayCalendar.set(Calendar.MILLISECOND, 0);
        Date today = todayCalendar.getTime();

        // Yesterday's date
        Calendar yesterdayCalendar = (Calendar) todayCalendar.clone();
        yesterdayCalendar.add(Calendar.DAY_OF_YEAR, -1);
        Date yesterday = yesterdayCalendar.getTime();

        // Start of the week (Monday)
        Calendar weekStartCalendar = (Calendar) todayCalendar.clone();
        if (todayCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            weekStartCalendar.add(Calendar.DAY_OF_YEAR, -6);  // If today is Sunday, go back to last Monday
        } else {
            weekStartCalendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        }
        Date weekStart = weekStartCalendar.getTime();

        // Start of the previous week
        Calendar lastWeekStartCalendar = (Calendar) weekStartCalendar.clone();
        lastWeekStartCalendar.add(Calendar.WEEK_OF_YEAR, -1);
        Date lastWeekStart = lastWeekStartCalendar.getTime();

        // End of the previous week (Sunday)
        Calendar lastWeekEndCalendar = (Calendar) lastWeekStartCalendar.clone();
        lastWeekEndCalendar.add(Calendar.DAY_OF_YEAR, 6);
        Date lastWeekEnd = lastWeekEndCalendar.getTime();

        Log.d("Debug", "Today's date: " + today);
        Log.d("Debug", "Yesterday's date: " + yesterday);
        Log.d("Debug", "Start of the week: " + weekStart);
        Log.d("Debug", "Start of last week: " + lastWeekStart);
        Log.d("Debug", "End of last week: " + lastWeekEnd);

        Query query = db.collection("businesses").document(bid)
                .collection("transactions")
                .whereNotEqualTo("completedAt", null);

        query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore ERR", "Listed failed: " + error);
                return;
            }

            int daily = 0, weekly = 0, yesterdayTotal = 0, lastWeekTotal = 0;
            assert value != null;
            for (QueryDocumentSnapshot document : value) {
                Timestamp createdAtTimestamp = document.getTimestamp("createdAt");
                int subtotal = Objects.requireNonNull(document.getLong("subtotal")).intValue();

                if (createdAtTimestamp != null) {
                    Date createdAt = createdAtTimestamp.toDate();
                    Calendar createdAtCalendar = Calendar.getInstance();
                    createdAtCalendar.setTime(createdAt);

                    Log.d("Debug", "Created At: " + createdAt + ", Subtotal: " + subtotal);

                    // Normalize createdAtCalendar
                    createdAtCalendar.set(Calendar.HOUR_OF_DAY, 0);
                    createdAtCalendar.set(Calendar.MINUTE, 0);
                    createdAtCalendar.set(Calendar.SECOND, 0);
                    createdAtCalendar.set(Calendar.MILLISECOND, 0);

                    // Check if it's the same day
                    if (isSameDay(createdAtCalendar, todayCalendar)) {
                        daily += subtotal;
                        Log.d("Debug", "Added to daily total: " + subtotal);
                    }

                    // Check if it's yesterday
                    if (isSameDay(createdAtCalendar, yesterdayCalendar)) {
                        yesterdayTotal += subtotal;
                        Log.d("Debug", "Added to yesterday total: " + subtotal);
                    }

                    // Check if within the week range
                    if (createdAtCalendar.compareTo(weekStartCalendar) >= 0 && createdAtCalendar.compareTo(todayCalendar) <= 0) {
                        weekly += subtotal;
                        Log.d("Debug", "Added to weekly total: " + subtotal);
                    }

                    // Check if within last week's range
                    if (createdAtCalendar.compareTo(lastWeekStartCalendar) >= 0 && createdAtCalendar.compareTo(lastWeekEndCalendar) <= 0) {
                        lastWeekTotal += subtotal;
                        Log.d("Debug", "Added to last week total: " + subtotal);
                    }
                }
            }

            tvHomeDaily.setText(Format.formatCurrency(daily));
            tvHomeWeekly.setText(Format.formatCurrency(weekly));
            tvHomeYesterday.setText(Format.formatCurrency(yesterdayTotal));
            tvHomeLastWeek.setText(Format.formatCurrency(lastWeekTotal));
        });
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

}