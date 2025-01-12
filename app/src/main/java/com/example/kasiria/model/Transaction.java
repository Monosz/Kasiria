package com.example.kasiria.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

// businesses/bId/transactions/tId/

public class Transaction implements Parcelable {
    private String id;
    private int tableNo;
    private List<Product> products;
    private double subtotal;
    private Timestamp createdAt;
    private Timestamp completedAt;

    public Transaction() {}

    public Transaction(int tableNo, List<Product> products) {
        this.tableNo = tableNo;
        this.products = products;
        this.subtotal = this.calculateSubtotal(products);
        this.createdAt = Timestamp.now();
    }

    public Transaction(String id, int tableNo, List<Product> products,
                       double subtotal, Timestamp createdAt, Timestamp completedAt) {
        this.id = id;
        this.products = products;
        this.tableNo = tableNo;
        this.subtotal = subtotal;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    protected Transaction(Parcel in) {
        id = in.readString();
        products = new ArrayList<>();
        in.readTypedList(products, Product.CREATOR);
        tableNo = in.readInt();
        subtotal = in.readDouble();
        createdAt = in.readParcelable(Timestamp.class.getClassLoader());
        completedAt = in.readParcelable(Timestamp.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeTypedList(products);
        dest.writeInt(tableNo);
        dest.writeDouble(subtotal);
        dest.writeParcelable(createdAt, flags);
        dest.writeParcelable(completedAt, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Transaction> CREATOR = new Creator<Transaction>() {
        @Override
        public Transaction createFromParcel(Parcel in) {
            return new Transaction(in);
        }

        @Override
        public Transaction[] newArray(int size) {
            return new Transaction[size];
        }
    };

    private double calculateSubtotal(List<Product> products) {
        double subtotal = 0.0;
        for (Product product : products) {
            subtotal += product.getPrice();
        }
        return subtotal;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public int getTableNo() {
        return tableNo;
    }

    public void setTableNo(int tableNo) {
        this.tableNo = tableNo;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Timestamp completedAt) {
        this.completedAt = completedAt;
    }
}
