package com.example.kasiria.ui.dashboard;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dantsu.escposprinter.connection.DeviceConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection;
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections;
import com.example.kasiria.R;
import com.example.kasiria.adapter.TransactionAdapter;
import com.example.kasiria.adapter.TransactionDetailAdapter;
import com.example.kasiria.async.AsyncBluetoothEscPosPrint;
import com.example.kasiria.async.AsyncEscPosPrint;
import com.example.kasiria.async.AsyncEscPosPrinter;
import com.example.kasiria.data.DocumenteroPostData;
import com.example.kasiria.data.DocumenteroResultData;
import com.example.kasiria.interfaces.DocumenteroAPI;
import com.example.kasiria.model.Business;
import com.example.kasiria.model.Product;
import com.example.kasiria.model.Transaction;
import com.example.kasiria.model.User;
import com.example.kasiria.service.RetrofitClient;
import com.example.kasiria.utils.Format;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// TODO: Clean this mess

public class TransactionDetailActivity extends AppCompatActivity {

    private Transaction transaction;
    private List<Product> products;
    private TransactionDetailAdapter adapter;
    private RecyclerView rvTransactionDetail;
    private BottomNavigationView navTransactionDetail;
    private Toolbar tbTransactionDetail;

    private ImageButton ibTransactionDEdit, ibTransactionDCheckout, ibTransactionDPrint, ibTransactionDPdf;
    private TextView tvTransactionDDetail, tvTransactionDSubtotal;
    private ProgressBar pbTransactionDetail;
    private ConstraintLayout main;

    private User user;
    private Business business;

    private FirebaseFirestore db;

    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy - HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_transaction_detail);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        db = FirebaseFirestore.getInstance();
        currencyFormat.setMaximumFractionDigits(0);
        main = findViewById(R.id.main);

        tbTransactionDetail = findViewById(R.id.tbTransactionDetail);
        setSupportActionBar(tbTransactionDetail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        tbTransactionDetail.setNavigationOnClickListener(v -> finish());

        navTransactionDetail = findViewById(R.id.navTransactionDetail);

        rvTransactionDetail = findViewById(R.id.rvTransactionDetail);
        ibTransactionDEdit = findViewById(R.id.ibTransactionDEdit);
//        ibTransactionDCheckout = findViewById(R.id.ibTransactionDCheckout);
//        ibTransactionDPrint = findViewById(R.id.ibTransactionDPrint);
//        ibTransactionDPdf = findViewById(R.id.ibTransactionDPdf);
        tvTransactionDDetail = findViewById(R.id.tvTransactionDDetail);
        tvTransactionDSubtotal = findViewById(R.id.tvTransactionDSubtotal);
        pbTransactionDetail = findViewById(R.id.pbTransactionDetail);

        Intent i = getIntent();
        TransactionAdapter.Type type = (TransactionAdapter.Type) i.getSerializableExtra("type");
        transaction = i.getParcelableExtra("transaction");

        if (transaction == null) {
            Toast.makeText(this, "Terjadi kesalahan, silahkan coba lagi", Toast.LENGTH_SHORT).show();
            return;
        }

        if (type == TransactionAdapter.Type.HISTORY) {
            ibTransactionDEdit.setVisibility(ImageButton.GONE);
//            ibTransactionDCheckout.setVisibility(ImageButton.GONE);
            navTransactionDetail.getMenu().removeItem(R.id.transactionDCheckout);
        }

        SharedPreferences pref = getSharedPreferences("user", MODE_PRIVATE);
        String uid = pref.getString("uid", null);
        String bid = pref.getString("bid", null);

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    user = documentSnapshot.toObject(User.class);
                });

        db.collection("businesses").document(bid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    business = documentSnapshot.toObject(Business.class);
                });

        navTransactionDetail.getMenu().setGroupCheckable(0, false, true);
        navTransactionDetail.getMenu().setGroupCheckable(0, true, true);

        navTransactionDetail.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int selected = item.getItemId();

                if (selected == R.id.transactionDPdf) {
                    pbTransactionDetail.setVisibility(View.VISIBLE);
                    main.setEnabled(false);
                    pdf();
                    pbTransactionDetail.setVisibility(View.INVISIBLE);
                    main.setEnabled(true);
                } else if (selected == R.id.transactionDPrint) {
                    print();
                } else if (selected == R.id.transactionDCheckout) {
                    checkout(bid);
                }

                // Remove checked item
                navTransactionDetail.getMenu().setGroupCheckable(0, false, true);
                navTransactionDetail.getMenu().setGroupCheckable(0, true, true);

                return true;
            }
        });

        products = transaction.getProducts();
        adapter = new TransactionDetailAdapter(products);

        tvTransactionDDetail.setText(String.format("Meja %d @ %s", transaction.getTableNo(), dateFormat.format(transaction.getCreatedAt().toDate())));
        tvTransactionDSubtotal.setText(Format.formatCurrency(transaction.getSubtotal()));

        rvTransactionDetail.setAdapter(adapter);
        rvTransactionDetail.setLayoutManager(new LinearLayoutManager(this));

        ibTransactionDEdit.setOnClickListener(v -> {
            TransactionDialogFragment dialogFragment = TransactionDialogFragment.newInstance(transaction);
            dialogFragment.show(getSupportFragmentManager(), "transaction_edit_dialog");
        });

//        ibTransactionDCheckout.setOnClickListener(v -> checkout(bid));
//        ibTransactionDPrint.setOnClickListener(v -> print());
//        ibTransactionDPdf.setOnClickListener(v -> pdf());

        Log.d("Transaction Detail", "Success");
    }

    private void pdf() {
        Log.d("PDF", "CLicked!");
//        main.setEnabled(false);
//        pbTransactionDetail.setVisibility(View.VISIBLE);

        Log.d("Documentero", "Clicked!");
        DocumenteroAPI documenteroAPI = RetrofitClient.getRetrofit().create(DocumenteroAPI.class);

        final String DOCUMENT_ID = "";
        final String API_KEY = "";

        if (API_KEY.isEmpty() || DOCUMENT_ID.isEmpty()) {
            Toast.makeText(this, "Tidak ada API_KEY atau DOKUMENT_ID", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumenteroPostData postData = new DocumenteroPostData();

        postData.setDocument(DOCUMENT_ID);
        postData.setApiKey(API_KEY);

//        postData.setFormat("docx");
        postData.setFormat("pdf");

        DocumenteroPostData.Data data = new DocumenteroPostData.Data();
        data.setBusinessName(business.getName());
        data.setBusinessAddress(business.getAddress());
        data.setOrderId(transaction.getId());
        data.setCreatedAt(dateFormat.format(transaction.getCreatedAt().toDate()));

        List<DocumenteroPostData.Product> products = new ArrayList<>();
        for (Product product : this.products) {
            DocumenteroPostData.Product p = new DocumenteroPostData.Product();
            p.setProductName(product.getName());
            p.setProductQuantity(String.valueOf(product.getQuantity()));
            p.setProductPrice(currencyFormat.format(product.getPrice()));
            p.setProductTotal(currencyFormat.format(product.getQuantity() * product.getPrice()));
            products.add(p);
        }
        data.setProducts(products);

        data.setSubtotal(currencyFormat.format(transaction.getSubtotal()));
        data.setPhoneNo(user.getPhone());
        data.setUserName(user.getName());

        postData.setData(data);

        Gson gson = new Gson();
//        Gson gson = new GsonBuilder()
//                .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
//                .create();
        String json = gson.toJson(postData);

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), json);
        Log.d("Documentero", requestBody.toString());

        Call<DocumenteroResultData> postJson = documenteroAPI.postJson(requestBody);

        postJson.enqueue(new Callback<DocumenteroResultData>() {
            @Override
            public void onResponse(Call<DocumenteroResultData> call, Response<DocumenteroResultData> response) {
                if (response.code() == 200) {
                    DocumenteroResultData resultData = response.body();

                    if (resultData.getStatus() == 200) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        String url = resultData.getData();
//                        intent.setData(Uri.parse(url));
                        intent.setDataAndType(Uri.parse(url), "application/pdf");
                        startActivity(intent);
                    }
                } else {
                    Toast.makeText(TransactionDetailActivity.this, "Terjadi kesalahan, silahkan coba lagi", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<DocumenteroResultData> call, Throwable t) {
                Toast.makeText(TransactionDetailActivity.this, "Terjadi kesalahan, silahkan coba lagi", Toast.LENGTH_SHORT).show();
            }
        });

//        main.setEnabled(true);
//        pbTransactionDetail.setVisibility(View.INVISIBLE);
    }

    private void checkout(String bid) {
        db.collection("businesses").document(bid)
                .collection("transactions")
                .whereEqualTo("id", transaction.getId()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                            doc.getReference()
                                    .update("completedAt", Timestamp.now())
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Transaksi berhasil dicheckout", Toast.LENGTH_SHORT).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Terjadi kesalahan, silahkan coba lagi", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Terjadi kesalahan, silahkan coba lagi", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Code below taken from <a href="https://github.com/DantSu/ESCPOS-ThermalPrinter-Android/">ESCPOS-ThermalPrinter-Android</a> example use
     */
    private void print() {
        printBluetooth();
    }

    /*==============================================================================================
    ======================================BLUETOOTH PART============================================
    ==============================================================================================*/

    public interface OnBluetoothPermissionsGranted {
        void onPermissionsGranted();
    }

    public static final int PERMISSION_BLUETOOTH = 1;
    public static final int PERMISSION_BLUETOOTH_ADMIN = 2;
    public static final int PERMISSION_BLUETOOTH_CONNECT = 3;
    public static final int PERMISSION_BLUETOOTH_SCAN = 4;

    public TransactionDetailActivity.OnBluetoothPermissionsGranted onBluetoothPermissionsGranted;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case TransactionDetailActivity.PERMISSION_BLUETOOTH:
                case TransactionDetailActivity.PERMISSION_BLUETOOTH_ADMIN:
                case TransactionDetailActivity.PERMISSION_BLUETOOTH_CONNECT:
                case TransactionDetailActivity.PERMISSION_BLUETOOTH_SCAN:
                    this.checkBluetoothPermissions(this.onBluetoothPermissionsGranted);
                    break;
            }
        }
    }

    public void checkBluetoothPermissions(TransactionDetailActivity.OnBluetoothPermissionsGranted onBluetoothPermissionsGranted) {

        this.onBluetoothPermissionsGranted = onBluetoothPermissionsGranted;
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH}, TransactionDetailActivity.PERMISSION_BLUETOOTH);
        } else if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_ADMIN}, TransactionDetailActivity.PERMISSION_BLUETOOTH_ADMIN);
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, TransactionDetailActivity.PERMISSION_BLUETOOTH_CONNECT);
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, TransactionDetailActivity.PERMISSION_BLUETOOTH_SCAN);
        } else {
            this.onBluetoothPermissionsGranted.onPermissionsGranted();
        }
    }

    private BluetoothConnection selectedDevice;

    @SuppressLint("MissingPermission")
    public void browseBluetoothDevice() {
        this.checkBluetoothPermissions(() -> {
            final BluetoothConnection[] bluetoothDevicesList = (new BluetoothPrintersConnections()).getList();

            if (bluetoothDevicesList != null) {
                final String[] items = new String[bluetoothDevicesList.length + 1];
                items[0] = "Default printer";
                int i = 0;
                for (BluetoothConnection device : bluetoothDevicesList) {
                    items[++i] = device.getDevice().getName();
                }

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(TransactionDetailActivity.this);
                alertDialog.setTitle("Bluetooth printer selection");
                alertDialog.setItems(
                        items,
                        (dialogInterface, i1) -> {
                            int index = i1 - 1;
                            if (index == -1) {
                                selectedDevice = null;
                            } else {
                                selectedDevice = bluetoothDevicesList[index];
                            }
                            Button button = (Button) findViewById(R.id.button_bluetooth_browse);
                            button.setText(items[i1]);
                        }
                );

                AlertDialog alert = alertDialog.create();
                alert.setCanceledOnTouchOutside(false);
                alert.show();
            }
        });

    }

    public void printBluetooth() {
        Log.d("Printer", "Entered printBluetooth()");
        this.checkBluetoothPermissions(() -> {
            new AsyncBluetoothEscPosPrint(
                    this,
                    new AsyncEscPosPrint.OnPrintFinished() {
                        @Override
                        public void onError(AsyncEscPosPrinter asyncEscPosPrinter, int codeException) {
                            Log.e("Async.OnPrintFinished", "AsyncEscPosPrint.OnPrintFinished : An error occurred !");
                        }

                        @Override
                        public void onSuccess(AsyncEscPosPrinter asyncEscPosPrinter) {
                            Log.i("Async.OnPrintFinished", "AsyncEscPosPrint.OnPrintFinished : Print is finished !");
                        }
                    }
            )
                    .execute(this.getAsyncEscPosPrinter(selectedDevice));
        });
    }

    /*==============================================================================================
    ===================================ESC/POS PRINTER PART=========================================
    ==============================================================================================*/

    /**
     * Asynchronous printing
     */
    public AsyncEscPosPrinter getAsyncEscPosPrinter(DeviceConnection printerConnection) {
        AsyncEscPosPrinter printer = new AsyncEscPosPrinter(printerConnection, 203, 48f, 32);

        int subtotal = 0;

        String header = "[C]================================\n" +
                        "[C]<font size='big'>" + business.getName() + "</font>\n" +
                        "[C]" + business.getAddress() + "\n" +
                        "[C]================================\n" +
                        "[L]ID Pemesanan:\n" +
                        "[L]" + transaction.getId() + "\n" +
                        "[L]\n" +
                        "[L]Waktu Pemesanan:\n" +
                        "[L]" + Format.formatDate(this.transaction.getCreatedAt().toDate()) + "\n" +
                        "[C]================================\n";
        String content = "";
        for (Product product : transaction.getProducts()) {
            subtotal += (product.getQuantity() * product.getPrice());
            String  qty = String.valueOf(product.getQuantity()),
                    price = Format.formatCurrency(product.getPrice()),
                    total = Format.formatCurrency((long) product.getQuantity() * product.getPrice());
            content += "[L]<b>" + product.getName() + "</b>\n";
            content += "[L]  " + qty + " x " + price + "[R]" +  total + "\n";
            content += "[L]\n";
        }
        content += "[C]--------------------------------\n";
        content += "[R]Subtotal :[R]" + Format.formatCurrency(subtotal) + "\n";
        content += "[L]\n";
        String footer =
                "[C]================================\n" +
                        "[L]\n" +
                        "[C]<font size='big'>Terima Kasih</font>\n" +
                        "[L]\n" +
                        "[C]WA: " + user.getPhone() + " (" + user.getName() + ")" + "\n";

        return printer.addTextToPrint(header + content + footer);
    }
}