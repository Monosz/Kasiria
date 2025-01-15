package com.example.kasiria.ui.dashboard;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.DialogFragment;

import com.example.kasiria.R;
import com.example.kasiria.model.Product;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProductDialogFragment extends DialogFragment {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Context context;
    private String bid;

    private static final String ARG_PRODUCT = "product";

    public static ProductDialogFragment newInstance(Product product) {
        ProductDialogFragment fragment = new ProductDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PRODUCT, product);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = getLayoutInflater().inflate(R.layout.dialog_product, null);
        context = view.getContext();

        ImageButton ibProductClose = view.findViewById(R.id.ibProductClose);
        EditText etProductName = view.findViewById(R.id.etProductName);
        EditText etProductPrice = view.findViewById(R.id.etProductPrice);
        EditText etProductType = view.findViewById(R.id.etProductType);
        SwitchCompat swProductAvailable = view.findViewById(R.id.swProductAvailable);
        Button btnProductSubmit = view.findViewById(R.id.btnProductSubmit);
        TextView tvProductTitle = view.findViewById(R.id.tvProductTitle);

        Product product = getArguments() != null ? getArguments().getParcelable(ARG_PRODUCT) : null;

        if (product != null) {
            etProductName.setText(product.getName());
            etProductPrice.setText(String.valueOf(product.getPrice()));
            etProductType.setText(product.getType());
            swProductAvailable.setChecked(product.isAvailable());
            tvProductTitle.setText("Ubah Produk");
            btnProductSubmit.setText("Ubah");
        }

        ibProductClose.setOnClickListener(v -> safelyDismiss());

        btnProductSubmit.setOnClickListener(v -> {
            String name = etProductName.getText().toString().trim();
            double price = Double.parseDouble(etProductPrice.getText().toString().trim());
            String type = etProductType.getText().toString().trim();
            boolean available = swProductAvailable.isChecked();

            SharedPreferences pref = requireContext().getSharedPreferences("user", 0);
            bid = pref.getString("bid", "");
            Log.d("bid", bid);

            if (product == null) {
                Product p = new Product(name, type, price, available);
                db.collection("businesses").document(bid)
                        .collection("products").add(p)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentReference docRef = task.getResult();
                                String pid = docRef.getId();

                                p.setId(pid);
                                docRef.set(p).addOnCompleteListener(aTask -> {
                                    if (aTask.isSuccessful()) {
                                        Toast.makeText(context, String.format("Produk %s berhasil ditambahkan", name), Toast.LENGTH_SHORT).show();
                                        safelyDismiss();
                                    }
                                });
                            } else {
                                Toast.makeText(context, "Terjadi kesalahan saat menambahkan produk" + name, Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Map<String, Object> p = new HashMap<>();
                p.put("name", name);
                p.put("price", price);
                p.put("type", type);
                p.put("available", available);

                db.collection("businesses").document(bid)
                        .collection("products").document(product.getId())
                        .update(p)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, String.format("Produk %s berhasil diubah", name), Toast.LENGTH_SHORT).show();
                                safelyDismiss();
                            } else {
                                Toast.makeText(context, "Terjadi kesalahan saat mengubah produk", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        return new AlertDialog.Builder(requireContext()).setView(view).create();
    }

    private void safelyDismiss() {
        if (isAdded() && getDialog() != null && getDialog().isShowing()) {
            dismiss();
        }
        ((DashboardActivity)requireActivity()).loadFragment(new ProductFragment(), "Produk");
    }
}
