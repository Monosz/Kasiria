package com.example.kasiria.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kasiria.R;
import com.example.kasiria.model.Transaction;
import com.example.kasiria.ui.dashboard.TransactionDetailActivity;
import com.example.kasiria.ui.dashboard.TransactionFragment;
import com.example.kasiria.utils.Format;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionHolder> {
    private final List<Transaction> transactions;
    private Context context;
    private final Type type;
    private final FirebaseFirestore db;

    public enum Type { TRANSACTION, HISTORY }

    public TransactionAdapter(List<Transaction> transactions, Type type) {
        this.transactions = transactions;
        this.type = type;

        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public TransactionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionHolder holder, int position) {
        Transaction transaction = transactions.get(position);

        String date;
        if (this.type == Type.TRANSACTION) {
            date = Format.formatDate(transaction.getCreatedAt().toDate());
            holder.tvTransactionSubtotal.setVisibility(View.GONE);
        } else { // Type.HISTORY
            date = Format.formatDate(transaction.getCompletedAt().toDate());
            holder.tvTransactionTable.setVisibility(View.GONE);
            holder.ibTransactionRemove.setVisibility(View.GONE);
        }

        holder.tvTransactionTable.setText(String.valueOf(transaction.getTableNo()));
        holder.tvTransactionTimestamp.setText(date);
        holder.tvTransactionSubtotal.setText(Format.formatCurrency(transaction.getSubtotal()));

        holder.ibTransactionRemove.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Hapus Transaksi");
            builder.setMessage("Apakah Anda yakin ingin menghapus transaksi ini?");
            builder.setPositiveButton("Ya", (dialog, which) -> {
                SharedPreferences pref = context.getSharedPreferences("user", Context.MODE_PRIVATE);
                String bid = pref.getString("bid", "");
                db.collection("businesses").document(bid).collection("transactions").document(transaction.getId()).delete()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Transaksi berhasil dihapus", Toast.LENGTH_SHORT).show();

                                // Force refresh
                                ((FragmentActivity)context).getSupportFragmentManager()
                                        .beginTransaction().replace(R.id.flDashboard, new TransactionFragment())
                                        .commit();
                            } else {
                                Toast.makeText(context, "Terjadi kesalahan saat menghapus transaksi" , Toast.LENGTH_SHORT).show();
                            }
                        });
            });

            builder.setNegativeButton("Tidak", (dialog, which) -> dialog.dismiss());
            builder.show();
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TransactionDetailActivity.class);
            intent.putExtra("transaction", transaction);
            intent.putExtra("type", this.type); // enum -> Serializeable
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }


    public static class TransactionHolder extends RecyclerView.ViewHolder {
        TextView tvTransactionTable, tvTransactionTimestamp, tvTransactionSubtotal;
        ImageButton ibTransactionRemove;

        public TransactionHolder(@NonNull View itemView) {
            super(itemView);

            tvTransactionTable = itemView.findViewById(R.id.tvTransactionTable);
            tvTransactionTimestamp = itemView.findViewById(R.id.tvTransactionTimestamp);
            tvTransactionSubtotal = itemView.findViewById(R.id.tvTransactionSubtotal);
            ibTransactionRemove = itemView.findViewById(R.id.ibTransactionRemove);
        }
    }
}
