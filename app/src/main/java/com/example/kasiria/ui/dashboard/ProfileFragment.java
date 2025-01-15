package com.example.kasiria.ui.dashboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.kasiria.MainActivity;
import com.example.kasiria.R;
import com.example.kasiria.model.Business;
import com.example.kasiria.model.User;
import com.example.kasiria.utils.Auth;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {
    private Context context;

    private FirebaseFirestore db;

    private EditText etProfileUserName, etProfileUserEmail, etProfileUserPhone;
    private EditText etProfileBusinessName, etProfileBusinessAddress;

    private User user;
    private Business business;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        context = view.getContext();

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etProfileUserName = view.findViewById(R.id.etProfileUserName);
        etProfileUserEmail = view.findViewById(R.id.etProfileUserEmail);
        etProfileUserPhone = view.findViewById(R.id.etProfileUserPhone);
        etProfileBusinessName = view.findViewById(R.id.etProfileBusinessName);
        etProfileBusinessAddress = view.findViewById(R.id.etProfileBusinessAddress);

        Button btnProfileSave = view.findViewById(R.id.btnProfileSave);
        Button btnProfileLogout = view.findViewById(R.id.btnProfileLogout);

        SharedPreferences pref = requireActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
        String uid = pref.getString("uid", null);
        String bid = pref.getString("bid", null);

        assert uid != null;
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    user = documentSnapshot.toObject(User.class);
                    etProfileUserName.setText(user.getName());
                    etProfileUserEmail.setText(user.getEmail());
                    etProfileUserPhone.setText(user.getPhone());
                });

        assert bid != null;
        db.collection("businesses").document(bid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    business = documentSnapshot.toObject(Business.class);
                    etProfileBusinessName.setText(business.getName());
                    etProfileBusinessAddress.setText(business.getAddress());
                });

        btnProfileLogout.setOnClickListener(v -> logout(pref));

        btnProfileSave.setOnClickListener(v -> {
            if (user == null || business == null) {
                Toast.makeText(context, "Mohon menunggu hingga data terlampir", Toast.LENGTH_SHORT).show();
                return;
            }

            String userName = etProfileUserName.getText().toString().trim();
            String phone = etProfileUserPhone.getText().toString().trim();
            String businessName = etProfileBusinessName.getText().toString().trim();
            String businessAddress = etProfileBusinessAddress.getText().toString().trim();

            if (userName.isEmpty() || phone.isEmpty() || businessName.isEmpty() || businessAddress.isEmpty()) {
                Toast.makeText(context, "Semua kolom wajib diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            user.setName(userName);
            user.setPhone(phone);

            business.setName(businessName);
            business.setAddress(businessAddress);

            db.collection("users").document(uid).set(user)
                    .addOnSuccessListener(aVoid -> {

                        db.collection("businesses").document(bid).set(business)
                                .addOnSuccessListener(bVoid -> {
                                    Toast.makeText(context, "Data pengguna berhasil disimpan", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Terjadi kesalahan saat menyimpan data bisnis", Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Terjadi kesalahan saat menyimpan data pengguna", Toast.LENGTH_SHORT).show();
                    });
        });

        return view;
    }

    private void logout(SharedPreferences pref) {
        Auth.signOut();

        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();

        startActivity(new Intent(context, MainActivity.class));
        getActivity().finish();
    }
}