package com.example.kasiria.ui.dashboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.kasiria.R;
import com.example.kasiria.adapter.TransactionAdapter;
import com.example.kasiria.model.Transaction;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView rvHistory;
    private TransactionAdapter adapter;
    private List<Transaction> transactions;
    private FirebaseFirestore db;
    private TextView tvHistoryNone;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        transactions = new ArrayList<>();
        adapter = new TransactionAdapter(transactions, TransactionAdapter.Type.HISTORY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        Context context = view.getContext();

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvHistory = view.findViewById(R.id.rvHistory);
        tvHistoryNone = view.findViewById(R.id.tvHistoryNone);

        rvHistory.setLayoutManager(new LinearLayoutManager(context));
        rvHistory.setAdapter(adapter);

        SharedPreferences pref = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        String bid = pref.getString("bid", null);

        db.collection("businesses").document(bid)
                .collection("transactions")
                .whereNotEqualTo("completedAt", null)
                .orderBy("completedAt", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            return;
                        }

                        transactions.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Transaction transaction = doc.toObject(Transaction.class);
                            transactions.add(transaction);
                        }
                        adapter.notifyDataSetChanged();
                        tvHistoryNone.setVisibility(transactions.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });

        return view;
    }
}