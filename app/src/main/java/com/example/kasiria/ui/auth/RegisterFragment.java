package com.example.kasiria.ui.auth;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kasiria.R;
import com.example.kasiria.model.Business;
import com.example.kasiria.model.User;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterFragment extends Fragment {
    private Context context;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private EditText etRegisterUserEmail, etRegisterUserPassword, etRegisterUserPhone;
    private EditText etRegisterBusinessName, etRegisterBusinessAddress;
    private TextView tvRegisterToLogin;
    private Button btnRegisterSubmit;
    private CheckBox cbRegisterAgree;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        context = view.getContext();

        etRegisterUserEmail = view.findViewById(R.id.etRegisterUserEmail);
        etRegisterUserPassword = view.findViewById(R.id.etRegisterUserPassword);
        etRegisterUserPhone = view.findViewById(R.id.etRegisterUserPhone);

        etRegisterBusinessName = view.findViewById(R.id.etRegisterBusinessName);
        etRegisterBusinessAddress = view.findViewById(R.id.etRegisterBusinessAddress);

        cbRegisterAgree = view.findViewById(R.id.cbRegisterAgree);

        SpannableString spannableString = new SpannableString(cbRegisterAgree.getText());

        int start1 = cbRegisterAgree.getText().toString().indexOf("Syarat dan Ketentuan");
        int end1 = start1 + "Syarat dan Ketentuan".length();
        spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), start1, end1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Change to your desired color

        int start2 = cbRegisterAgree.getText().toString().indexOf("Kebijakan Privasi");
        int end2 = start2 + "Kebijakan Privasi".length();
        spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), start2, end2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // Change to your desired color

        cbRegisterAgree.setText(spannableString);

        tvRegisterToLogin = view.findViewById(R.id.tvRegisterToLogin);
        tvRegisterToLogin.setOnClickListener(v -> {
            toLogin();
        });

        btnRegisterSubmit = view.findViewById(R.id.btnRegisterSubmit);
        btnRegisterSubmit.setOnClickListener(v -> {
            String userEmail = etRegisterUserEmail.getText().toString().trim();
            String userPassword = etRegisterUserPassword.getText().toString().trim();
            String userPhone = etRegisterUserPhone.getText().toString().trim();

            String businessName = etRegisterBusinessName.getText().toString().trim();
            String businessAddress = etRegisterBusinessAddress.getText().toString().trim();

            boolean isChecked = cbRegisterAgree.isChecked();

            if (userEmail.isEmpty() || userPassword.isEmpty() || userPhone.isEmpty() || businessName.isEmpty() || businessAddress.isEmpty()) {
                Toast.makeText(context, "Semua kolom wajib diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isChecked) {
                Toast.makeText(context, "Anda harus menyetujui syarat dan ketentuan terlebih dahulu", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(userEmail, userPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            AuthResult res = task.getResult();
                            if (res != null && res.getUser() != null) {
                                User user = new User(res.getUser().getUid(), userEmail, userPhone);
                                saveUserToFirestore(user);
                                Business business = new Business(businessName, businessAddress, user.getId());
                                saveBusinessToFirestore(business);
                            }
                        } else {
                            Exception ex = task.getException();
                            if (ex instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(context, "Email sudah terdaftar", Toast.LENGTH_SHORT).show();
                                return;
                            } else {
                                Toast.makeText(context, "Terjadi kesalahan saat mendaftarkan pengguna, silahkan coba lagi", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                    });
        });

        return view;
    }

    private void toLogin() {
        if (getActivity() instanceof AuthActivity) {
            ((AuthActivity) getActivity()).loadFragment(new LoginFragment(), "Login");
        }
    }

    private void saveUserToFirestore(User user) {
        db.collection("users")
                .document(user.getId())
                .set(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        toLogin();
                    } else {
                        Toast.makeText(context, "Terjadi kesalahan saat menyimpan data pengguna, silahkan coba lagi", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveBusinessToFirestore(Business business) {
        db.collection("businesses")
                .add(business)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {

                                DocumentReference docRef = task.getResult();
                                String id = docRef.getId();
                                business.setId(id);

                                docRef.set(business)
                                        .addOnCompleteListener(uTask -> {
                                            if (uTask.isSuccessful()) {
                                                toLogin();
                                            } else {
                                                Toast.makeText(context, "Terjadi kesalahan saat menyimpan usaha pengguna, silahkan coba lagi", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Toast.makeText(context, "Terjadi kesalahan saat menyimpan usaha pengguna, silahkan coba lagi", Toast.LENGTH_SHORT).show();
                            }
                        });
    }
}