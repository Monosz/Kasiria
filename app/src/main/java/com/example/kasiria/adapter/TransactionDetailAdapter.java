package com.example.kasiria.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kasiria.R;
import com.example.kasiria.model.Product;
import com.example.kasiria.utils.Format;

import java.util.List;

public class TransactionDetailAdapter extends RecyclerView.Adapter<TransactionDetailAdapter.TransactionDetailHolder> {
    private final List<Product> products;
    private Context context;

    public TransactionDetailAdapter(List<Product> products) {
        this.products = products;
    }


    @NonNull
    @Override
    public TransactionDetailAdapter.TransactionDetailHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction_detail, parent, false);
        return new TransactionDetailHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TransactionDetailAdapter.TransactionDetailHolder holder, int position) {
        Product product = products.get(position);

        holder.tvTransactionDName.setText(product.getName());
        holder.tvTransactionDQuantityPrice.setText(product.getQuantity() + " x " + Format.formatCurrency(product.getPrice()));
        holder.tvTransactionDTotal.setText(Format.formatCurrency(product.getQuantity() * product.getPrice()));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public static class TransactionDetailHolder extends RecyclerView.ViewHolder {
        TextView tvTransactionDName, tvTransactionDQuantityPrice, tvTransactionDTotal;

        public TransactionDetailHolder(@NonNull View itemView) {
            super(itemView);

            tvTransactionDName = itemView.findViewById(R.id.tvTransactionDName);
            tvTransactionDQuantityPrice = itemView.findViewById(R.id.tvTransactionDQuantityPrice);
            tvTransactionDTotal = itemView.findViewById(R.id.tvTransactionDTotal);
        }
    }
}
