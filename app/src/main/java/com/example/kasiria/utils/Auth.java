package com.example.kasiria.utils;

import com.google.firebase.auth.FirebaseAuth;

public class Auth {
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();

    public static boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public static String getUid() {
        if (isLoggedIn()) {
            return auth.getUid();
        }
        return "";
    }
}
