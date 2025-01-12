package com.example.kasiria.ui.dashboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import com.example.kasiria.MainActivity;
import com.example.kasiria.R;
import com.example.kasiria.model.Business;
import com.example.kasiria.model.User;
import com.example.kasiria.ui.auth.AuthActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {
    private FirebaseAuth auth;
    private Context context;

    private FirebaseFirestore db;

    private Button btnProfileLogout, btnProfileSave, btnProfileDelete;
    private EditText etProfileUserName, etProfileUserEmail, etProfileUserPhone, etProfileUserPasswordOld, etProfileUserPasswordNew;
    private EditText etProfileBusinessName, etProfileBusinessAddress;

    private User user;
    private Business business;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
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
//        etProfileUserPasswordOld = view.findViewById(R.id.etProfileUserPasswordOld);
//        etProfileUserPasswordNew = view.findViewById(R.id.etProfileUserPasswordNew);
        etProfileBusinessName = view.findViewById(R.id.etProfileBusinessName);
        etProfileBusinessAddress = view.findViewById(R.id.etProfileBusinessAddress);

        btnProfileSave = view.findViewById(R.id.btnProfileSave);
        btnProfileDelete = view.findViewById(R.id.btnProfileDelete);
        btnProfileLogout = view.findViewById(R.id.btnProfileLogout);

        SharedPreferences pref = requireActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
        String uid = pref.getString("uid", null);
        String bid = pref.getString("bid", null);



        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    user = documentSnapshot.toObject(User.class);
                    etProfileUserName.setText(user.getName());
                    etProfileUserEmail.setText(user.getEmail());
                    etProfileUserPhone.setText(user.getPhone());
                });

        db.collection("businesses").document(bid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    business = documentSnapshot.toObject(Business.class);
                    etProfileBusinessName.setText(business.getName());
                    etProfileBusinessAddress.setText(business.getAddress());
                });


        btnProfileLogout.setOnClickListener(v -> logout(pref));
        btnProfileDelete.setOnClickListener(v -> {
            db.collection("users").document(uid).delete();
            db.collection("businesses").document(bid).delete();
            auth.getCurrentUser().delete();
            logout(pref);
        });

        btnProfileSave.setOnClickListener(v -> {
            String userName = etProfileUserName.getText().toString().trim();
            String phone = etProfileUserPhone.getText().toString().trim();
//            String userPasswordOld = etProfileUserPasswordOld.getText().toString().trim();
//            String userPasswordNew = etProfileUserPasswordNew.getText().toString().trim();
            String businessName = etProfileBusinessName.getText().toString().trim();
            String businessAddress = etProfileBusinessAddress.getText().toString().trim();

        });

        return view;
    }

    private void logout(SharedPreferences pref) {
        auth.signOut();

        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();

        startActivity(new Intent(context, MainActivity.class));
        getActivity().finish();
    }
}