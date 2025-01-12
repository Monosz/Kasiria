package com.example.kasiria.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kasiria.R;
import com.example.kasiria.model.Product;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TransactionDetailAdapter extends RecyclerView.Adapter<TransactionDetailAdapter.TransactionDetailHolder> {
    private List<Product> products;
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

    @Override
    public void onBindViewHolder(@NonNull TransactionDetailAdapter.TransactionDetailHolder holder, int position) {
        Product product = products.get(position);

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        currencyFormat.setMaximumFractionDigits(0);

        holder.tvTransactionDName.setText(product.getName());
        holder.tvTransactionDQuantityPrice.setText(product.getQuantity() + " x " + currencyFormat.format(product.getPrice()));
        holder.tvTransactionDTotal.setText(currencyFormat.format(product.getQuantity() * product.getPrice()));
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
