package com.example.kasiria.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kasiria.R;
import com.example.kasiria.model.Product;

import java.util.List;

public class TransactionProductAdapter extends RecyclerView.Adapter<TransactionProductAdapter.TransactionProductHolder> {
    private List<Product> products;
    private Context context;

    public TransactionProductAdapter(List<Product> products) {
        this.products = products;
    }

    @NonNull
    @Override
    public TransactionProductAdapter.TransactionProductHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction_product, parent, false);
        return new TransactionProductHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionProductAdapter.TransactionProductHolder holder, int position) {
        Product product = products.get(position);

        holder.tvTransactionPName.setText(product.getName());
        holder.tvTransactionPQuantity.setText(String.valueOf(product.getQuantity()));

        holder.ibTransactionPDecrement.setOnClickListener(v -> {
            if (product.getQuantity() > 0) {
                product.setQuantity(product.getQuantity() - 1);
                notifyItemChanged(position);
            }
        });

        holder.ibTransactionPIncrement.setOnClickListener(v -> {
            product.setQuantity(product.getQuantity() + 1);
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class TransactionProductHolder extends RecyclerView.ViewHolder {
        TextView tvTransactionPName, tvTransactionPQuantity;
        ImageButton ibTransactionPDecrement, ibTransactionPIncrement;

        public TransactionProductHolder(@NonNull View itemView) {
            super(itemView);

            tvTransactionPName = itemView.findViewById(R.id.tvTransactionPName);
            tvTransactionPQuantity = itemView.findViewById(R.id.tvTransactionPQuantity);
            ibTransactionPDecrement = itemView.findViewById(R.id.ibTransactionPDecrement);
            ibTransactionPIncrement = itemView.findViewById(R.id.ibTransactionPIncrement);
        }
    }
}
