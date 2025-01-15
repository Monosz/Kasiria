package com.example.kasiria.utils;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class Auth {
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();

    public static boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public static String getUid() {
        if (isLoggedIn()) {
            return Objects.requireNonNull(auth.getCurrentUser()).getUid();
        }
        return "";
    }

    public static void signOut() {
        if (isLoggedIn()) {
            auth.signOut();
        }
    }
}
