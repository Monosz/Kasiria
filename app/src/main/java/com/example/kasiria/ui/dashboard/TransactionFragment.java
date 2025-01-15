package com.example.kasiria.ui.dashboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.kasiria.R;
import com.example.kasiria.adapter.TransactionAdapter;
import com.example.kasiria.model.Transaction;
import com.example.kasiria.utils.Format;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TransactionFragment extends Fragment {
    private FirebaseFirestore db;
    private RecyclerView rvTransaction;
    private TransactionAdapter adapter;
    private List<Transaction> transactions;
    private TextView tvTransactionNone;

    private TextView tvTransactionCount, tvTransactionEarning;
    private String bid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        transactions = new ArrayList<>();
        adapter = new TransactionAdapter(transactions, TransactionAdapter.Type.TRANSACTION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transaction, container, false);
        Context context = view.getContext();

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvTransaction = view.findViewById(R.id.rvTransaction);
        tvTransactionNone = view.findViewById(R.id.tvTransactionNone);
        tvTransactionCount = view.findViewById(R.id.tvTransactionCount);
        tvTransactionEarning = view.findViewById(R.id.tvTransactionEarning);

        SharedPreferences pref = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        bid = pref.getString("bid", null);

        if (bid == null) return view;

        loadTransactions(bid);

        rvTransaction.setLayoutManager(new LinearLayoutManager(context));
        rvTransaction.setAdapter(adapter);

        Calendar startOfDay = Calendar.getInstance();
        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);
        startOfDay.set(Calendar.MILLISECOND, 0);

        Calendar endOfDay = Calendar.getInstance();
        endOfDay.set(Calendar.HOUR_OF_DAY, 23);
        endOfDay.set(Calendar.MINUTE, 59);
        endOfDay.set(Calendar.SECOND, 59);
        endOfDay.set(Calendar.MILLISECOND, 999);

        db.collection("businesses").document(bid)
                .collection("transactions")
                .whereGreaterThanOrEqualTo("createdAt", new Timestamp(startOfDay.getTime()))
                .whereLessThanOrEqualTo("createdAt", new Timestamp(endOfDay.getTime()))
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    int count = 0;
                    double totalEarning = 0;

                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Transaction transaction = doc.toObject(Transaction.class);
                            count++;
                            totalEarning += transaction.getSubtotal();
                        }
                    }

                    tvTransactionCount.setText(String.valueOf(transactions.size()));
                    tvTransactionEarning.setText(Format.formatCurrency(totalEarning));

                });

        tvTransactionNone.setOnClickListener(v -> {
            Log.d("TransactionNone", "Clicked");
            TransactionDialogFragment dialogFragment = TransactionDialogFragment.newInstance(null);
            dialogFragment.show(getChildFragmentManager(), "transaction_add_dialog");
        });

        return view;
    }

    private void loadTransactions(String bid) {
        db.collection("businesses").document(bid)
                .collection("transactions")
                .whereEqualTo("completedAt", null)
                .orderBy("tableNo", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }

                    transactions.clear();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            transactions.add(doc.toObject(Transaction.class));
                        }
                    }
                    adapter.notifyDataSetChanged();
                });

        // Display empty message
        // TODO: Find better/cost-efficient solution?
        db.collection("businesses").document(bid)
                .collection("transactions").get()
                .addOnCompleteListener(task -> {
                    if (task.getResult() != null) {
                        tvTransactionNone.setVisibility(transactions.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("TransactionFragment onResume", "onResume: Resumed");
        loadTransactions(bid);
    }
}