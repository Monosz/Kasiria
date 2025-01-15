package com.example.kasiria.ui.dashboard;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kasiria.R;
import com.example.kasiria.adapter.TransactionProductAdapter;
import com.example.kasiria.model.Product;
import com.example.kasiria.model.Transaction;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionDialogFragment extends DialogFragment {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Context context;
    private List<Product> products;
    private TransactionProductAdapter adapter;
    private String bid;

    private static final String ARG_TRANSACTION = "transaction";

    public static TransactionDialogFragment newInstance(Transaction transaction) {
        TransactionDialogFragment fragment = new TransactionDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TRANSACTION, transaction);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.dialog_transaction, null);
        context = view.getContext();

        ImageButton ibTransactionPClose = view.findViewById(R.id.ibTransactionPClose);
        EditText etTransactionPTableNo = view.findViewById(R.id.etTransactionPTableNo);
        RecyclerView rvTransactionP = view.findViewById(R.id.rvTransactionP);
        Button btnTransactionPSubmit = view.findViewById(R.id.btnTransactionPSubmit);
        TextView tvTransactionPTitle = view.findViewById(R.id.tvTransactionPTitle);

        products = new ArrayList<>();
        adapter = new TransactionProductAdapter(products);

        Transaction transaction = getArguments() != null ? getArguments().getParcelable(ARG_TRANSACTION) : null;

        if (transaction != null) {
            etTransactionPTableNo.setText(String.valueOf(transaction.getTableNo()));
            tvTransactionPTitle.setText("Ubah Transaksi");
            btnTransactionPSubmit.setText("Ubah");
        }

        SharedPreferences pref = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        bid = pref.getString("bid", null);

        db.collection("businesses").document(bid)
                .collection("products")
                .whereEqualTo("available", true).get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot doc : task.getResult()) {
                                    Product p = doc.toObject(Product.class);
                                    products.add(p);
                                }

                                // If update (transaction != null),
                                // set existing qty back
                                if (transaction != null) {
                                    List<Product> prods = transaction.getProducts();

                                    Map<String, Integer> qty = new HashMap<>();
                                    for (Product p : prods) {
                                        qty.put(p.getId(), p.getQuantity());
                                    }

                                    for (Product p : products) {
                                        if (qty.containsKey(p.getId())) {
                                            p.setQuantity(qty.get(p.getId()));
                                        }
                                    }
                                }

                                if (products.isEmpty()) {
                                    Toast.makeText(context, "Buatlah produk terlebih dahulu", Toast.LENGTH_SHORT).show();
                                    safelyDismiss();
                                }

                                adapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(context, "Terjadi kesalahan saat mengambil produk", Toast.LENGTH_SHORT).show();
                            }
                        });

        // Set transaction product based on screen width
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        int spanCount = (int) (dpWidth / 240);
        spanCount = Math.max(spanCount, 2);
        spanCount = Math.min(spanCount, 4);
        rvTransactionP.setLayoutManager(new GridLayoutManager(context, spanCount));
        // rvTransactionP.setLayoutManager(new LinearLayoutManager(context));
        rvTransactionP.setAdapter(adapter);

        rvTransactionP.post(() -> Log.d("RVHeight", "Height: " + rvTransactionP.getHeight()));

        ibTransactionPClose.setOnClickListener(v -> safelyDismiss());

        btnTransactionPSubmit.setOnClickListener(v -> {
            String tableNoStr = etTransactionPTableNo.getText().toString();
            if (tableNoStr.isEmpty()) {
                Toast.makeText(context, "Nomor meja harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }
            int tableNo = Integer.parseInt(tableNoStr);

            if (transaction == null) {
                Transaction t = new Transaction(tableNo, getUpdatedProducts());

                db.collection("businesses").document(bid)
                        .collection("transactions").add(t)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentReference docRef = task.getResult();
                                t.setId(docRef.getId());
                                docRef.set(t).addOnCompleteListener(aTask -> {

                                    Toast.makeText(context, "Berhasil membuat transaksi", Toast.LENGTH_SHORT).show();
                                    safelyDismiss("new");
                                });
                            } else {
                                Toast.makeText(context, "Terjadi kesalahan saat membuat transaksi", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {

                db.collection("businesses").document(bid)
                        .collection("transactions")
                        .whereEqualTo("id", transaction.getId()).get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot doc : task.getResult()) {
                                    DocumentReference transactionDocRef = doc.getReference();
                                    List<Product> addProd = getUpdatedProducts();
                                    double subtotal = 0;
                                    for (Product p : addProd) {
                                        subtotal += p.getPrice() * p.getQuantity();
                                    }

                                    transactionDocRef.update("products", addProd)
                                            .addOnSuccessListener(aVoid -> {
                                                String s = transaction.getTableNo() == 0 ? "Takeaway" : "Meja " + transaction.getTableNo();
                                                Toast.makeText(context, String.format("Transaksi %s berhasil diubah", s), Toast.LENGTH_SHORT).show();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(context, "Terjadi kesalahan saat mengubah transaksi", Toast.LENGTH_SHORT).show();
                                            });
                                    transactionDocRef.update("subtotal", subtotal);


                                    safelyDismiss("update");
                                }
                            } else {
                                Toast.makeText(context, "Terjadi kesalahan saat mengubah transaksi", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

        });

        return new AlertDialog.Builder(context).setView(view).create();
    }

    private List<Product> getUpdatedProducts() {
        List<Product> updatedProducts = new ArrayList<>();
        for (Product p : products) {
            if (p.getQuantity() > 0) {
                updatedProducts.add(p);
            }
        }
        return updatedProducts;
    }

    private void safelyDismiss() {
        if (isAdded() && getDialog() != null && getDialog().isShowing()) {
            dismiss();
        }
    }

    private void safelyDismiss(String type) {
        safelyDismiss();
        if (type.equals("new")) {
            ((DashboardActivity)requireActivity()).loadFragment(new TransactionFragment(), "Transaksi");
        } else {
            requireActivity().finish();
        }
    }
}
