package com.example.kasiria.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kasiria.R;
import com.example.kasiria.model.Product;
import com.example.kasiria.ui.dashboard.ProductDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductHolder> {
    private List<Product> products;
    private Context context;
    private FirebaseFirestore db;

    public ProductAdapter(List<Product> products) {
        this.products = products;
    }

    @NonNull
    @Override
    public ProductHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        context = parent.getContext();
        db = FirebaseFirestore.getInstance();

        return new ProductHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductHolder holder, int position) {
        Product product = products.get(position);

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        currencyFormat.setMaximumFractionDigits(0);

        int img = product.isAvailable() ? R.drawable.ic_circle_green : R.drawable.ic_circle_red;
        holder.ivProductAvailable.setImageResource(img);
        holder.ivProductAvailable.setOnClickListener(v -> {
            int img2 = holder.ivProductAvailable.getDrawable() == context.getDrawable(R.drawable.ic_circle_green) ?
                    R.drawable.ic_circle_red : R.drawable.ic_circle_green;
            holder.ivProductAvailable.setImageResource(img2);
        });

        holder.tvIProductName.setText(product.getName());
        holder.tvIProductPrice.setText(currencyFormat.format(product.getPrice()));
        holder.ibIProductRemove.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Hapus Produk");
            builder.setMessage("Apakah Anda yakin ingin menghapus produk ini?");
            builder.setPositiveButton("Ya", (dialog, which) -> {
                SharedPreferences pref = context.getSharedPreferences("user", Context.MODE_PRIVATE);
                String bid = pref.getString("bid", "");
                db.collection("businesses").document(bid).collection("products").document(product.getId()).delete()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, String.format("Produk %s berhasil dihapus", product.getName()), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Terjadi kesalahan saat menghapus produk " + product.getName() , Toast.LENGTH_SHORT).show();
                            }
                        });
            });

            builder.setNegativeButton("Tidak", (dialog, which) -> dialog.dismiss());
            builder.show();
        });

        holder.itemView.setOnClickListener(v -> {
            ProductDialogFragment dialogFragment = ProductDialogFragment.newInstance(product);
            FragmentActivity fragmentActivity = (FragmentActivity) context;
            dialogFragment.show(fragmentActivity.getSupportFragmentManager(), "product_dialog");
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class ProductHolder extends RecyclerView.ViewHolder {
        TextView tvIProductName, tvIProductPrice;
        ImageButton ibIProductRemove;
        ImageView ivProductAvailable;

        public ProductHolder(@NonNull View itemView) {
            super(itemView);

            tvIProductName = itemView.findViewById(R.id.tvIProductName);
            tvIProductPrice = itemView.findViewById(R.id.tvIProductPrice);
            ibIProductRemove = itemView.findViewById(R.id.ibIProductRemove);
            ivProductAvailable = itemView.findViewById(R.id.ivProductAvailable);
        }
    }
}
