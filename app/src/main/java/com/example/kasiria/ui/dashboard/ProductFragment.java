package com.example.kasiria.ui.dashboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kasiria.R;
import com.example.kasiria.adapter.ProductAdapter;
import com.example.kasiria.model.Product;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ProductFragment extends Fragment {

    private RecyclerView rvProduct;
    private ProductAdapter adapter;
    private FirebaseFirestore db;
    private List<Product> products;
    private TextView tvProductNone;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        products = new ArrayList<>();
        adapter = new ProductAdapter(products);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product, container, false);
        Context context = view.getContext();

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rvProduct = view.findViewById(R.id.rvProduct);
        tvProductNone = view.findViewById(R.id.tvProductNone);

        SharedPreferences pref = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        String bid = pref.getString("bid", null);

        loadProducts(bid);

        rvProduct.setLayoutManager(new LinearLayoutManager(context));
        rvProduct.setAdapter(adapter);

        tvProductNone.setOnClickListener(v -> {
            ProductDialogFragment dialogFragment = ProductDialogFragment.newInstance(null);
            dialogFragment.show(getChildFragmentManager(), "product_add_dialog");
        });

        return view;
    }

    private void loadProducts(String bid) {
        db.collection("businesses").document(bid)
                .collection("products")
                .orderBy("name")
                .orderBy("type")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        return;
                    }

                    products.clear();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            products.add(doc.toObject(Product.class));
                        }
                    }
                    adapter.notifyDataSetChanged();
                });

        // Display empty message
        // TODO: Find better/cost-efficient solution?
        db.collection("businesses").document(bid)
                .collection("products").get()
                .addOnCompleteListener(task -> {
                    if (task.getResult() != null) {
                        tvProductNone.setVisibility(products.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }
}