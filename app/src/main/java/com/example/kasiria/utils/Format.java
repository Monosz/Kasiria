package com.example.kasiria.utils;

import android.annotation.SuppressLint;

import com.google.firebase.Timestamp;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Format {
    private static final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy - HH:mm");

    public static String formatDate(Date date) {
        return dateFormat.format(date);
    }

    public static String formatDate(Timestamp timestamp) {
        return dateFormat.format(timestamp.toDate());
    }

    public static String formatCurrency(long amount) {
        return currencyFormat.format(amount);
    }

    public static String formatCurrency(double amount) {
        return currencyFormat.format(amount);
    }
}
