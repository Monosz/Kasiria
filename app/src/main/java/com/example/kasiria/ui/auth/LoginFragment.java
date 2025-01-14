package com.example.kasiria.ui.auth;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.kasiria.R;
import com.example.kasiria.ui.dashboard.DashboardActivity;
import com.example.kasiria.utils.Auth;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginFragment extends Fragment {
    private FirebaseAuth auth;
    private Context context;

    private EditText etLoginEmail, etLoginPassword;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        context = view.getContext();

        etLoginEmail = view.findViewById(R.id.etLoginEmail);
        etLoginPassword = view.findViewById(R.id.etLoginPassword);

        Button btnLoginSubmit = view.findViewById(R.id.btnLoginSubmit);
        btnLoginSubmit.setOnClickListener(v -> login());

        TextView tvLoginForgotPassword = view.findViewById(R.id.tvLoginForgotPassword);
        tvLoginForgotPassword.setOnClickListener(v -> forgotPassword());

        TextView tvLoginToRegister = view.findViewById(R.id.tvLoginToRegister);
        tvLoginToRegister.setOnClickListener(v -> {
            if (getActivity() instanceof AuthActivity) {
                ((AuthActivity) getActivity()).loadFragment(new RegisterFragment(), "Register");
            }
        });

        return view;
    }

    private void login() {
        String email = etLoginEmail.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "Semua kolom wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(context, "Password harus lebih dari 6 karakter", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        setSharedPref(Auth.getUid());
                    } else {
                        Exception ex = task.getException();
                        if (ex instanceof FirebaseAuthInvalidUserException) {
                            Toast.makeText(context, "Akun tidak ditemukan", Toast.LENGTH_SHORT).show();
                        } else if (ex instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(context, "Email atau kata sandi salah", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Terjadi kesalahan saat login, silahkan coba lagi", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setSharedPref(String uid) {
        SharedPreferences pref = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("uid", uid);
        editor.apply();

        FirebaseFirestore.getInstance().collection("businesses")
                .whereEqualTo("ownerId", uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);
                            String businessId = documentSnapshot.getId();

                            editor.putString("bid", businessId);
                            editor.apply();

                            Intent intent = new Intent(context, DashboardActivity.class);
                            context.startActivity(intent);
                            requireActivity().finish();
                        } else {
                            clearUser();

                            Toast.makeText(context, "Terjadi kesalahan saat mengambil data usaha pengguna", Toast.LENGTH_SHORT).show();
                            Log.e("LoginFragment", "No business found for user: " + uid + ", " + task.getException());
                        }
                    } else {
                        clearUser();

                        Toast.makeText(context, "Terjadi kesalahan saat mengambil data usaha pengguna", Toast.LENGTH_SHORT).show();
                        Log.e("LoginFragment", "Error getting businesses: ", task.getException());
                    }
                });
    }

    private void clearUser() {
        // Signs out the user and clear sharedPref data
        auth.signOut();
        context.getSharedPreferences("user", Context.MODE_PRIVATE).edit().clear().apply();
    }

    private void forgotPassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Atur ulang kata sandi?");
        builder.setMessage("Masukkan email terdaftar");

        final EditText etEmail = new EditText(context);
        etEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        etEmail.setHint("Email");

        final FrameLayout container = new FrameLayout(context);
        container.addView(etEmail);
        container.setPadding(24, 0, 24, 0);

        builder.setView(container);

        builder.setPositiveButton("Kirim", (dialog, which) -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(context, "Email harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Link untuk mengatur ulang kata sandi telah dikirim ke email anda", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(context, "Terjadi kesalahan saat mengirim permintaan, silahkan coba lagi", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());

        builder.show();
    }
}